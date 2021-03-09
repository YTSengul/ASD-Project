package nl.han.asd.submarine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.client.MongoCollection;
import nl.han.asd.submarine.message.MessageType;
import nl.han.asd.submarine.models.message.FileMessage;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistMessages;
import org.bson.Document;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class PersistMessagesImpl implements PersistMessages {
    private static final Logger LOG = Logger.getLogger(PersistMessagesImpl.class.getName());

    @Inject
    @Named(DatabaseCollections.Constants.CONVERSATION)
    private MongoCollection<Document> collection;

    @Override
    public void insertMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            collection.updateOne(eq("_id", message.getConversationId()),
                    new Document("$push",
                            new Document("messages",
                                    new Document("messageType",
                                            MessageType.TEXT.toString()).append("alias", textMessage.getSender())
                                            .append("conversationId", message.getConversationId())
                                            .append("timeStamp", message.getTimestamp())
                                            .append("message", (((TextMessage) message).getMessage()))
                            )
                    )
            );
        }
    }

    public List<Message> getMessageList(String chatIdentifier) {
        List<Message> messages = new ArrayList<>();
        try {
            Document conversationDocument = collection.find(eq("_id", chatIdentifier)).first();
            conversationDocument.get("messages", List.class).forEach(doc -> messages.add(constructMessage(getTypeOfMessage((Document) doc), (Document) doc)));
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, String.format("Could not find Chats with chatid [%s]", chatIdentifier));
            throw e;
        }
        return messages;
    }

    private Message constructMessage(MessageType messageType, Document doc) {
        switch (messageType) {
            case TEXT:
                return constructTextMessage(doc);
            case FILE:
                return constructFileMessage(doc);
            default:
                String message = String.format("Could not parse a message fetched with type [%s]", messageType.toString());
                LOG.log(Level.WARNING, message);
                throw new IllegalArgumentException(message);
        }
    }

    private TextMessage constructTextMessage(Document doc) {
        return new TextMessage(
                doc.getString("alias"),
                doc.getString("conversationId"),
                doc.getDate("timeStamp").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                doc.getString("message")
        );
    }

    private FileMessage constructFileMessage(Document doc) {
        return new FileMessage(
                doc.getString("path"),
                doc.getString("alias"),
                doc.getDate("timeStamp").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                doc.getString("conversationId")
        );
    }

    private MessageType getTypeOfMessage(Document doc) {
        return MessageType.valueOf(doc.getString("messageType"));
    }
}
