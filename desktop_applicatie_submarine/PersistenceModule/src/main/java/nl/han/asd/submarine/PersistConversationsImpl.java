package nl.han.asd.submarine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import nl.han.asd.submarine.message.MessageType;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistConversations;
import nl.han.asd.submarine.persistence.PersistenceModule;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class PersistConversationsImpl implements PersistConversations {
    private static final Logger LOG =
            Logger.getLogger(PersistConversationsImpl.class.getName());

    @Inject
    PersistenceModule persistenceModule;

    @Inject
    @Named(DatabaseCollections.Constants.CONVERSATION)
    private MongoCollection<Document> collection;

    public void insertConversation(Conversation conversation) {
        String currentAlias = persistenceModule.getAlias();
        List<Document> contacts = new ArrayList<>();
        conversation.getParticipants()
                .stream()
                .filter(x -> !x.getAlias().equals(currentAlias))
                .forEach(x -> contacts.add(new Document("alias", x.getAlias()).append("publicKey", x.getPublicKey())));

        List<Document> messages = new ArrayList<>();
        conversation.getMessages()
                .stream()
                .forEach(x -> messages.add(new Document("messageType", MessageType.TEXT.toString())
                        .append("alias", x.getSender())
                        .append("conversationId", x.getConversationId())
                        .append("timeStamp", x.getTimestamp())
                        .append("message", ((TextMessage) x).getMessage())));

        Document conversationDocument = new Document("_id",
                conversation.getId())
                .append("title", conversation.getTitle())
                .append("participants", contacts)
                .append("messages", messages);
        collection.insertOne(conversationDocument);
    }

    public void updateConversation(Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    public void deleteConversation(int conversationId) {
        throw new UnsupportedOperationException();
    }

    public Conversation getConversation(String chatIdentifier) {
        Document conversationDocument = collection.find(eq("_id",
                chatIdentifier)).first();
        if (conversationDocument != null) {
            return new Conversation(chatIdentifier,
                    conversationDocument.getString("title"),
                    persistenceModule.getChatParticipants(chatIdentifier),
                    persistenceModule.getMessageList(chatIdentifier));
        } else {
            LOG.log(Level.SEVERE, getCouldNotFindMessage(chatIdentifier));
            throw new NullPointerException(getCouldNotFindMessage(chatIdentifier));
        }
    }

    public Map<String, String> getConversations() {
        Map<String, String> conversations = new HashMap<>();

        FindIterable<Document> documentsIterable = collection.find();

        for (Document document : documentsIterable) {
            conversations.put(document.getString("_id"), document.getString(
                    "title"));
        }
        return conversations;
    }

    public List<Contact> getChatParticipants(String chatIdentifier) {
        List<Contact> contacts = new ArrayList<>();
        Document conversationDocument = collection.find(eq("_id",
                chatIdentifier)).first();
        if (conversationDocument != null) {
            conversationDocument.get("participants", List.class).forEach(doc ->
                    contacts.add(new Contact(
                            ((Document) doc).getString("publicKey"),
                            ((Document) doc).getString("alias")
                    )));
        } else {
            LOG.log(Level.SEVERE, getCouldNotFindMessage(chatIdentifier));
            throw new NullPointerException(getCouldNotFindMessage(chatIdentifier));
        }
        return contacts;
    }

    private String getCouldNotFindMessage(String chatIdentifier) {
        return "Could not find Chats with " + "chatid [" + chatIdentifier + "]";
    }

}
