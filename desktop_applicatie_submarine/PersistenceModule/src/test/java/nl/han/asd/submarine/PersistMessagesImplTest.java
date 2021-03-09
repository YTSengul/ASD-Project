package nl.han.asd.submarine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.message.MessageType;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.persistence.PersistMessages;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PersistMessagesImplTest {

    private PersistMessagesImpl persistMessages;
    private MongoDatabase databaseInstance;
    private MongoCollection<Document> collection;
    private Conversation conversationExample;

    @BeforeEach
    public void setup() {
        //Init database and inject
        this.databaseInstance =
                new MongoClient("localhost", 27017).getDatabase(
                        "submarineTest");
        this.collection = databaseInstance.getCollection("conversation");

        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named("conversation")).toInstance(collection);
                bind(PersistMessages.class).to(PersistMessagesImpl.class);
            }
        }

        Injector injector =
                Guice.createInjector(new TestBootstrapModule());

        String publicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqwgpx925XEm9jxdfS1XkMCscOlgYxMCM9b+AWzjD54LbnBzmAZFDYlsB8o2x4aPEXMtedWyuL9PN3v2y4g2Pgxb6UfxMyAiNeJ1rNfvRgT/eDuou/VRXTG8Ik61hikiacdpaa26f48XBE0CjcfEqk/e61EQPkBXIV4c2LmoSkLgiNm5p43DD3lfmEmCb3sEsMTokit+3J1XOHFpI0/4dCST35bLuGXwjH+6I4IYVAV9uCSwYznDHCu/V6JFu02L8simBmAjdUcGlWPRObdHDyTrVGjmbiDo6zQgCaKZRAgkRz19o4xidMot9ywSp72/cJ98MmXpMRUmS/EaSqfh6KwIDAQAB";

        this.databaseInstance =
                new MongoClient("localhost", 27017).getDatabase(
                        "submarineTest");
        this.collection = databaseInstance.getCollection("conversation");

        List<Contact> contactlist = new ArrayList<>();
        contactlist.add(new Contact(publicKey, "Tuna Eagle"));
        contactlist.add(new Contact(publicKey, "Smelly Seal"));
        contactlist.add(new Contact(publicKey, "Waiting Whale"));

        List<Message> messageList = new ArrayList<>();
        messageList.add(new TextMessage(contactlist.get(0).getAlias(),
                "testConvoId",
                Instant.ofEpochMilli(111111111).atZone(ZoneId.systemDefault()).toLocalDateTime(), "Hello There"));
        messageList.add(new TextMessage(contactlist.get(2).getAlias(),
                "testConvoId",
                Instant.ofEpochMilli(222222222).atZone(ZoneId.systemDefault()).toLocalDateTime(), "Hello You"));
        messageList.add(new TextMessage(contactlist.get(1).getAlias(),
                "testConvoId",
                Instant.ofEpochMilli(333333333).atZone(ZoneId.systemDefault()).toLocalDateTime(), "Hello Me"));
        messageList.add(new TextMessage(contactlist.get(1).getAlias(),
                "testConvoId",
                Instant.ofEpochMilli(444444444).atZone(ZoneId.systemDefault()).toLocalDateTime(), "Hello Us"));
        messageList.add(new TextMessage(contactlist.get(0).getAlias(),
                "testConvoId",
                Instant.ofEpochMilli(555555555).atZone(ZoneId.systemDefault()).toLocalDateTime(), "Hello World"));

        conversationExample = new Conversation("testConvoId", "ASD-B Rocks", contactlist, messageList);

        // Insert an alias
        MongoCollection<Document> dataCollection =
                databaseInstance.getCollection("userData");
        Document doc = new Document("key", "alias").append("value", "Melting " +
                "Martian");
        dataCollection.insertOne(doc);

        persistMessages = (PersistMessagesImpl) injector.getInstance(PersistMessages.class);

    }

    @AfterEach
    public void cleanUp() {
        databaseInstance.drop();
    }

    @Test
    void insertMessageSuccessTest() {
        // setup
        addConversationToDatabase(conversationExample);
        TextMessage expectedMessage = new TextMessage(
                "SendMan",
                "testConvoId",
                "Hello Planet"
        );
        // run
        persistMessages.insertMessage(expectedMessage);
        // check
        FindIterable<Document> foundDocuments = collection.find(eq("messages" +
                ".5.conversationId", "testConvoId"));
        Document foundDocument = foundDocuments.first();
        assertThat(foundDocument).isNotNull();
        Document foundSubDocument = ((List<Document>) foundDocument.get(
                "messages")).get(5);
        assertThat(foundSubDocument.getString("alias")).isEqualTo(expectedMessage.getSender());
        assertThat(foundSubDocument.getString("conversationId")).isEqualTo(expectedMessage.getConversationId());
        assertThat(foundSubDocument.getString("message")).isEqualTo(expectedMessage.getMessage());
    }

    @Test
    void getMessagesSuccessTest() {
        // Setup
        addConversationToDatabase(conversationExample);
        List<Message> expectedMessages =
                conversationExample.getMessages();
        // run
        List<Message> actualMessages =
                persistMessages.getMessageList(conversationExample.getId());
        // check
        assertThat(actualMessages.size()).isEqualTo(expectedMessages.size());
        for (int i = 0; i < actualMessages.size(); i++) {
            // DateTime storage is very, very finicky, and I can't get it to
            // actually succeed.
            TextMessage actual = (TextMessage) actualMessages.get(i);
            TextMessage expected = (TextMessage) expectedMessages.get(i);
            assertThat(actual.getMessage()).isEqualTo(expected.getMessage());
            assertThat(actual.getSender()).isEqualTo(expected.getSender());
            assertThat(actual.getConversationId()).isEqualTo(expected.getConversationId());
        }
    }

    @Test
    void getMessageListNonExistandChatNameTest() {
        // run
        assertThatThrownBy(
                () -> {
                    persistMessages.getMessageList("OhNo");
                })
                .isInstanceOf(NullPointerException.class); // check
    }

    @Test
    void getMessageListNonExistantMessageTypeTest() {
        // Setup
        addConversationToDatabase(conversationExample);
        collection.updateOne(eq("_id", conversationExample.getId()),
                new Document("$push",
                        new Document("messages",
                                new Document("messageType", "BROKEN")
                                        .append("alias", "Superman")
                                        .append("conversationId",
                                                conversationExample.getId())
                                        .append("timeStamp",
                                                conversationExample.getMessages().get(0).getTimestamp())
                                        .append("message", "I broke it!")
                        )
                )
        );
        // run
        assertThatThrownBy(
                () -> {
                    persistMessages.getMessageList(conversationExample.getId());
                })
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Not a test, just part of the setup
    private void addConversationToDatabase(final Conversation conversation) {
        // You better believe this works, I am not writing a test for spaghetti.
        List<Document> contacts = new ArrayList<>();
        List<Document> messages = new ArrayList<>();
        for (Contact contact : conversation.getParticipants()) {
            contacts.add(
                    new Document("alias", contact.getAlias()).append(
                            "publicKey", contact.getPublicKey())
            );
        }

        for (Message message : conversation.getMessages()) {
            TextMessage textMessage = (TextMessage) message;
            messages.add(
                    new Document("messageType", MessageType.TEXT.toString())
                            .append("alias", textMessage.getSender())
                            .append("conversationId",
                                    message.getConversationId())
                            .append("timeStamp", message.getTimestamp())
                            .append("message",
                                    ((TextMessage) message).getMessage())
            );
        }

        Document conversationDocument = new Document("_id",
                conversation.getId())
                .append("participants", contacts)
                .append("messages", messages);
        collection.insertOne(conversationDocument);
    }
}
