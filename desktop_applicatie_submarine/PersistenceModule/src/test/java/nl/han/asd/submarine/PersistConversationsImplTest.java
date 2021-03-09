package nl.han.asd.submarine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.message.MessageType;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.persistence.PersistConversations;
import nl.han.asd.submarine.persistence.PersistenceModule;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistConversationsImplTest {

    private PersistConversationsImpl persistConversations;
    private MongoDatabase databaseInstance;
    private MongoCollection<Document> collection;
    private Conversation conversationExample;

    @Mock
    private PersistenceModule persistenceModuleMock;

    @BeforeEach
    void setup() {
        //Init database and inject
        this.databaseInstance =
                new MongoClient("localhost", 27017).getDatabase(
                        "submarineTest");
        databaseInstance.drop();
        this.collection = databaseInstance.getCollection("conversation");

        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named("conversation")).toInstance(collection);
                bind(PersistConversations.class).to(PersistConversationsImpl.class);
                bind(PersistenceModule.class).toInstance(persistenceModuleMock);
            }
        }
        Injector injector =
                Guice.createInjector(new TestBootstrapModule());

        String publicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqwgpx925XEm9jxdfS1XkMCscOlgYxMCM9b+AWzjD54LbnBzmAZFDYlsB8o2x4aPEXMtedWyuL9PN3v2y4g2Pgxb6UfxMyAiNeJ1rNfvRgT/eDuou/VRXTG8Ik61hikiacdpaa26f48XBE0CjcfEqk/e61EQPkBXIV4c2LmoSkLgiNm5p43DD3lfmEmCb3sEsMTokit+3J1XOHFpI0/4dCST35bLuGXwjH+6I4IYVAV9uCSwYznDHCu/V6JFu02L8simBmAjdUcGlWPRObdHDyTrVGjmbiDo6zQgCaKZRAgkRz19o4xidMot9ywSp72/cJ98MmXpMRUmS/EaSqfh6KwIDAQAB";


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

        // Insert the mock into the persistConversation class
        this.persistenceModuleMock = mock(PersistenceModule.class);

        persistConversations = (PersistConversationsImpl) injector.getInstance(PersistConversations.class);
        persistConversations.persistenceModule = persistenceModuleMock;
    }

    @AfterEach
    void cleanUp() {
        databaseInstance.drop();
    }

    @Test
    void getConversationSuccessTest() throws NoSuchFieldException {
        // setup
        addConversationToDatabase(conversationExample);
        when(persistenceModuleMock.getChatParticipants("testConvoId")).thenReturn(conversationExample.getParticipants());
        when(persistenceModuleMock.getMessageList("testConvoId")).thenReturn(conversationExample.getMessages());
        // run
        Conversation actualConversation =
                persistConversations.getConversation(conversationExample.getId());
        // check
        assertThat(actualConversation).isEqualToComparingFieldByField(conversationExample);
    }

    @Test
    void getConversationNonExistentChatTest() {
        // run
        assertThatThrownBy(
                () -> {
                    persistConversations.getConversation("OhNo");
                })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Could not find Chats with chatid [OhNo]"); // check
    }

    @Test
    void getChatParticipantsSuccessTest() {
        // Setup
        addConversationToDatabase(conversationExample);
        List<Contact> expectedParticipants =
                conversationExample.getParticipants();
        // run
        List<Contact> actualParticipants =
                persistConversations.getChatParticipants(conversationExample.getId());
        // check
        assertThat(actualParticipants.size()).isEqualTo(expectedParticipants.size());
        for (int i = 0; i < actualParticipants.size(); i++) {
            assertThat(actualParticipants.get(i)).isEqualToComparingFieldByField(expectedParticipants.get(i));
        }
    }

    @Test
    void getChatParticipantsNonExistandChatTest() {
        // run
        assertThatThrownBy(
                () -> {
                    persistConversations.getChatParticipants("OhNo");
                })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Could not find Chats with chatid [OhNo]"); // check
    }

    @Test
    void getConversationsReturnsConversations() {
        // Arrange
        addConversationToDatabase(conversationExample);
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("testConvoId", "ASD-B Rocks");

        // Act
        Map<String, String> actualResult = persistConversations.getConversations();

        // Assert
        assertThat(actualResult).isEqualTo(expectedResult);
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
                .append("title", conversation.getTitle())
                .append("participants", contacts)
                .append("messages", messages);
        collection.insertOne(conversationDocument) ;
    }
}
