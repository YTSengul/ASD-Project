package nl.han.asd.submarine.integrationTests;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.han.asd.submarine.*;
import nl.han.asd.submarine.connection.HandleChatterRequest;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.exception.ChatterServerException;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.routing.HTTPRequest;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import nl.han.asd.submarine.service.ChatterService;
import nl.han.asd.submarine.util.IpResolver;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Disabled
@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class RegisterChatterIT {
    private Injector injector;

    protected static MongoClient mongoClient;

    private static final String CHATTER_SERVER_IP = "94.124.143.166";
    private static final String NODE_SERVER_IP = "94.124.143.165";

    protected MongoCollection<Document> userCollectionSpy;
    protected PersistenceModule persistenceModuleSpy = Mockito.spy(new PersistenceModuleImpl());
    protected NodeInformation nodeInformationSpy = Mockito.spy(new NodeInformation());
    protected HandleChatterRequest handleRequestSpy = Mockito.spy(new HandleChatterRequestImpl());
    protected SendMessage sendMessageSpy = Mockito.spy(new SendMessageImpl());
    protected SymmetricEncryption symmetricEncryptionSpy = Mockito.spy(new SymmetricEncryptionImplAes());
    protected RouteModule routeModuleSpy = Mockito.spy(new RouteModuleImpl(nodeInformationSpy, symmetricEncryptionSpy));

    @Captor
    private ArgumentCaptor<String> httpCaptor;

    @Captor
    private ArgumentCaptor<UserData> userDataCaptor;

    @BeforeAll
    static void beforeAll() {
        mongoClient = new MongoClient("localhost", 27017);
    }

    @BeforeEach
    void setup() {
        var database = mongoClient.getDatabase("integrationTests");
        AddMockDataToDatabase.start(database);
        userCollectionSpy = Mockito.spy(database.getCollection(DatabaseCollections.USER_DATA.getValue()));

        nodeInformationSpy.setNodeDirectoryIp(NODE_SERVER_IP);

        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named(DatabaseCollections.USER_DATA.getValue())).toInstance(userCollectionSpy);
                bind(PersistenceModule.class).toInstance(persistenceModuleSpy);
                bind(RouteModule.class).toInstance(routeModuleSpy);
                bind(HandleChatterRequest.class).toInstance(handleRequestSpy);
                bind(SendMessage.class).toInstance(sendMessageSpy);
                bind(SymmetricEncryption.class).toInstance(symmetricEncryptionSpy);

                bind(Integer.class).annotatedWith(Names.named("clientPort")).toInstance(25010);
                bind(String.class).annotatedWith(Names.named("encryptedSymmetricKey")).toInstance("9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q=");
                bind(String.class).annotatedWith(Names.named("chatterServerIp")).toInstance(CHATTER_SERVER_IP);
            }
        }
        this.injector = Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));

    }

    @ParameterizedTest
    @MethodSource
    void registerChatterSendToServer(Chatter chatter) {
        // Arrange
        ChatterService chatterService = injector.getInstance(ChatterService.class);

        // Act
        chatterService.registerChatter(chatter);

        // Assert
        verify(routeModuleSpy).makeOnion(httpCaptor.capture(), eq(chatter.getAlias()), eq(3), eq("9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="), eq(true));
        verify(handleRequestSpy).handleChatter(any(Onion.class));
        verify(persistenceModuleSpy).saveChatter(userDataCaptor.capture());

        HTTPRequest<LinkedTreeMap<String, Object>> request = new Gson().fromJson(httpCaptor.getValue(), HTTPRequest.class);

        var body = request.getBody();
        assertThat(request.getEndpoint()).isEqualTo("http://"+ CHATTER_SERVER_IP +"/chatter/create");
        assertThat(request.getRequest_type()).isEqualTo("POST");
        assertThat(body.get("username")).isEqualTo(chatter.getUsername());
        assertThat(body.get("password")).isEqualTo(chatter.getPassword());
        assertThat(body.get("ipAddress")).isEqualTo(IpResolver.getHostIp());
        assertThat(Math.round((Double) body.get("port"))).isEqualTo(25010);
        assertThat(body.get("alias")).isEqualTo(chatter.getAlias());

        Document document = userCollectionSpy.find(Filters.eq("alias", chatter.getAlias())).first();
        assertThat(document.getString("alias")).isEqualTo(chatter.getAlias());
        assertThat(document.getString("username")).isEqualTo(chatter.getUsername());
        assertThat(document.getString("privateKey")).isEqualTo(userDataCaptor.getValue().getPrivateKey());
        assertThat(document.getString("publicKey")).isEqualTo(userDataCaptor.getValue().getPublicKey());
    }

    static Stream<Arguments> registerChatterSendToServer() {
        return Stream.of(
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "testPassword", "integrationtest" + UUID.randomUUID().toString())),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "testPassword","integrationtest" + UUID.randomUUID().toString()+"#¯6øñ)blh×ÜOBíCUÈ<~NZXÕ¾eQ×É`:_ô9nÜNOñ°ÔgvÕû|Lµ1hÎ¨QHÎìÛqÔVTúõLd}þüTf!4ò11ú7¦Ä¯Ø- ÔÃõB¯îÝ)Û¼tD´Þ²ø[nåO¥¬ÀðUã¡ò°Ô@ÄÿXØôiq{÷s0Mñ¨-Tæ¹EªüÌà¼ik-3")),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "testPassword","integrationtest" + UUID.randomUUID().toString()+"是青少年考试遇见中文"))
            );
    }

    @ParameterizedTest
    @MethodSource
    void registerChatterSendToServerWithEmptyField(Chatter chatter) {
        // Arrange
        ChatterService chatterService = injector.getInstance(ChatterService.class);

        assertThatExceptionOfType(ChatterServerException.class).isThrownBy(() -> chatterService.registerChatter(chatter));

        verify(routeModuleSpy).makeOnion(httpCaptor.capture(), eq(chatter.getAlias()), eq(3), eq("9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="), eq(true));
        verify(handleRequestSpy).handleChatter(any(Onion.class));
        verify(persistenceModuleSpy, never()).saveChatter(userDataCaptor.capture());

        HTTPRequest<LinkedTreeMap<String, Object>> request = new Gson().fromJson(httpCaptor.getValue(), HTTPRequest.class);

        var body = request.getBody();
        assertThat(request.getEndpoint()).isEqualTo("http://"+ CHATTER_SERVER_IP +"/chatter/create");
        assertThat(request.getRequest_type()).isEqualTo("POST");
        assertThat(body.get("username")).isEqualTo(chatter.getUsername());
        assertThat(body.get("password")).isEqualTo(chatter.getPassword());
        assertThat(body.get("ipAddress")).isEqualTo(IpResolver.getHostIp());
        assertThat(Math.round((Double) body.get("port"))).isEqualTo(25010);
        assertThat(body.get("alias")).isEqualTo(chatter.getAlias());
    }

    static Stream<Arguments> registerChatterSendToServerWithEmptyField() {
        return Stream.of(
                Arguments.of(new Chatter("", "testPassword", "integrationtest" + UUID.randomUUID().toString())),
                Arguments.of(new Chatter(null, "testPassword", "integrationtest" + UUID.randomUUID().toString())),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "", "integrationtest" + UUID.randomUUID().toString())),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), null, "integrationtest" + UUID.randomUUID().toString())),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "testPassword", "")),
                Arguments.of(new Chatter("integrationtest" + UUID.randomUUID().toString(), "testPassword", null)),
                Arguments.of(new Chatter()),
                Arguments.of(new Chatter("", "", ""))
            );
    }

    @Test
    void registerChatterSendToServerWithDuplicateAlias() {
        // Arrange
        String username = "integrationtest" + UUID.randomUUID().toString();
        String alias = "integrationtest" + UUID.randomUUID().toString();

        ChatterService chatterService = injector.getInstance(ChatterService.class);
        Chatter chatter = new Chatter();
        chatter.setUsername(username);
        chatter.setPassword("testPassword");
        chatter.setAlias(alias);

        chatterService.registerChatter(chatter);

        // Resetting to make sure that the spies don't
        reset(routeModuleSpy, handleRequestSpy, persistenceModuleSpy);

        assertThatExceptionOfType(ChatterServerException.class).isThrownBy(() -> chatterService.registerChatter(chatter));

        verify(routeModuleSpy).makeOnion(httpCaptor.capture(), eq(chatter.getAlias()), eq(3), eq("9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="), eq(true));
        verify(handleRequestSpy).handleChatter(any(Onion.class));
        verify(persistenceModuleSpy, never()).saveChatter(userDataCaptor.capture());

        HTTPRequest<LinkedTreeMap<String, Object>> request = new Gson().fromJson(httpCaptor.getValue(), HTTPRequest.class);

        var body = request.getBody();
        assertThat(request.getEndpoint()).isEqualTo("http://"+ CHATTER_SERVER_IP +"/chatter/create");
        assertThat(request.getRequest_type()).isEqualTo("POST");
        assertThat(body.get("username")).isEqualTo(username);
        assertThat(body.get("password")).isEqualTo("testPassword");
        assertThat(body.get("ipAddress")).isEqualTo(IpResolver.getHostIp());
        assertThat(Math.round((Double) body.get("port"))).isEqualTo(25010);
        assertThat(body.get("alias")).isEqualTo(alias);
    }


    @AfterEach
    void tearDown() {
        mongoClient.dropDatabase("integrationTests");
    }

    @AfterAll
    static void afterAll() {
        mongoClient.close();
    }
}
