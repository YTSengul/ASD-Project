package nl.han.asd.submarine.integrationTests;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.gson.Gson;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.*;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.models.NodeDTO;
import nl.han.asd.submarine.models.NodesDTO;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.ConnectException;
import java.security.KeyPair;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

//@Disabled("Only works on local machines, when a Node Directory Server is running, three different nodes are running and registered in the Node Directory Server")
@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class SendMessageIT {

    private Injector injector;

    protected static MongoClient mongoClient;
    protected SocketFactory socketFactoryMock = mock(SocketFactory.class);

    protected MongoCollection<Document> messageCollectionSpy;
    protected MongoCollection<Document> userCollectionSpy;
    protected MongoCollection<Document> conversationCollectionSpy;

    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final int DEFAULT_PORT = 25010;

    protected final PersistenceModule persistenceModuleSpy = spy(new PersistenceModuleImpl());
    protected final SymmetricEncryption symmetricEncryptionSpy = spy(new SymmetricEncryptionImplAes());
    protected final NodeInformation nodeInformationSpy = spy(new NodeInformation());
    protected final RouteModule routeModuleSpy = spy(new RouteModuleImpl(nodeInformationSpy, symmetricEncryptionSpy));
    protected final SendMessage sendMessageSpy = spy(new SendMessageImpl());

    @BeforeAll
    static void beforeAll() {
        mongoClient = new MongoClient("localhost", 27017);
    }

    @BeforeEach
    public void setup() {
        MongoDatabase database = mongoClient.getDatabase("integrationTests");
        AddMockDataToDatabase.start(database);
        userCollectionSpy = spy(database.getCollection(DatabaseCollections.USER_DATA.getValue()));
        conversationCollectionSpy = spy(database.getCollection(DatabaseCollections.CONVERSATION.getValue()));
        // TODO implement keys
        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(SendMessage.class).toInstance(sendMessageSpy);
                bind(RouteModule.class).toInstance(routeModuleSpy);
                bind(PersistenceModule.class).toInstance(persistenceModuleSpy);
                bind(new TypeLiteral<MongoCollection<Document>>() {
                })
                        .annotatedWith(Names.named(DatabaseCollections.CONVERSATION.getValue()))
                        .toInstance(conversationCollectionSpy);
                bind(new TypeLiteral<MongoCollection<Document>>() {
                })
                        .annotatedWith(Names.named(DatabaseCollections.USER_DATA.getValue()))
                        .toInstance(userCollectionSpy);

                bind(String.class).annotatedWith(Names.named("nodeDirectoryServerIp")).toInstance("127.0.0.1:8081");
            }
        }
        this.injector = Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));
        nodeInformationSpy.setNodeDirectoryIp(injector.getInstance(Key.get(String.class, Names.named("nodeDirectoryServerIp"))));

        NodesDTO nodes = new NodesDTO().setNodes(
                List.of(new NodeDTO(new DestinationRelay("127.0.0.1", 25010), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="),
                        new NodeDTO(new DestinationRelay("127.0.0.1", 25010), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="),
                        new NodeDTO(new DestinationRelay("127.0.0.1", 25010), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q=")
                )
        );
        nodeInformationSpy.setTransport(getTransportWithContent(new Gson().toJson(nodes)));
    }

//    @Disabled("Pipeline can't connect to the socket")
    @Test
    void connectAndSendMessageTest() {
        // Arrange
        MessageService messageService = injector.getInstance(MessageService.class);
        SendMessage sendMessage = injector.getInstance(SendMessage.class);
        ArgumentCaptor<Onion> argument = ArgumentCaptor.forClass(Onion.class);
        AsymmetricEncryption aSymmetricEncryption =
                injector.getInstance(new Key<>() {
                });
        KeyPair keyPair = aSymmetricEncryption.generateRandomKeyPair();

        try (Socket socket = SocketFactory.getDefault().createSocket(DEFAULT_HOSTNAME, DEFAULT_PORT)) {
            when(socketFactoryMock.createSocket(DEFAULT_HOSTNAME, DEFAULT_PORT)).thenReturn(socket);

            // Act
            messageService.sendTextMessage("testConvoId", "TestMessageContent");

            // Assert
            verify(sendMessage, times(3)).sendMessage(argument.capture());
            verify(nodeInformationSpy, times(3)).get();

            FindIterable<Document> foundDocuments = conversationCollectionSpy.find(eq("messages.5.conversationId", "testConvoId"));
            Document foundDocument = foundDocuments.first();
            assertThat(foundDocument).isNotNull();
            Document foundSubDocument = ((List<Document>) foundDocument.get("messages")).get(5);
            assertThat(foundSubDocument.getString("alias")).isEqualTo("Melting Martian");
            assertThat(foundSubDocument.getString("conversationId")).isEqualTo("testConvoId");
            assertThat(foundSubDocument.getString("message")).isEqualTo("TestMessageContent");
        } catch (ConnectException e) {
            throw new RuntimeException("Can't connect to node. Required to have a local node running.", e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Disabled("Currently not working in buildpipe. Needs fix")
    @Test
    @DisplayName("Try to send a message, but can't connect to node. Expected to throw a exception.")
    void canNotConnectToNode() throws IOException {
        MessageService messageService = injector.getInstance(MessageService.class);

        when(socketFactoryMock.createSocket(anyString(), anyInt())).thenThrow(new IOException());

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> messageService.sendTextMessage("testConvoId", "TestMesageContent"))
                .withMessage("Could not connect to entry node.");

        verify(persistenceModuleSpy, never()).insertMessage(any());
    }

    @AfterEach
    void tearDown() {
        mongoClient.dropDatabase("integrationTests");
    }

    @AfterAll
    static void afterAll() {
        mongoClient.close();
    }

    /**
     * HttpRequest and HttpResponse are final classes and cant be mocked. According to the googleapis github pages this
     * is the way to create mock requests and responses.
     *
     * @link http://googleapis.github.io/google-http-java-client/unit-testing.html
     */
    private HttpTransport getTransportWithContent(String content) {
        return new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(200);
                        response.setContentType(Json.MEDIA_TYPE);
                        response.setContent(content);
                        return response;
                    }
                };
            }
        };
    }
}
