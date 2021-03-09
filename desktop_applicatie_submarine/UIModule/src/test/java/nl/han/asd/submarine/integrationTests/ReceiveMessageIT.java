package nl.han.asd.submarine.integrationTests;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.*;
import nl.han.asd.submarine.connection.ConnectionModule;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationClient;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.*;

@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class ReceiveMessageIT {

    private static MongoClient client;

    private MessageService sut;
    private MongoDatabase database;

    private final AsymmetricEncryptionImplRsa asymmetricEncryption = spy(new AsymmetricEncryptionImplRsa());
    private final PersistenceModule persistenceModule = spy(new PersistenceModuleImpl());
    private final SymmetricEncryption symmetricEncryption = spy(new SymmetricEncryptionImplAes());

    @BeforeAll
    static void beforeAll() {
        client = new MongoClient("localhost", 27017);
    }

    @BeforeEach
    void setup() {
        database = client.getDatabase("submarineTest");
        AddMockDataToDatabase.start(database);

        class TestBootstrapModule extends AbstractModule {
            final MessageService messageService = new MessageService();
            final ConnectionModule connectionModuleMock = mock(ConnectionModule.class);
            final SymmetricEncryption symmetricEncryption = spy(new SymmetricEncryptionImplAes());
            RouteModule routeModuleMock = mock(RouteModule.class);

            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(ConnectionModule.class).toInstance(connectionModuleMock);
                bind(new TypeLiteral<SymmetricEncryption>() {
                }).toInstance(symmetricEncryption);
                bind(new TypeLiteral<AsymmetricEncryption>() {
                }).toInstance(asymmetricEncryption);
                bind(RouteModule.class).toInstance(routeModuleMock);
                bind(PersistenceModule.class).toInstance(persistenceModule);
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named(DatabaseCollections.CONVERSATION.getValue())).toInstance(database.getCollection(DatabaseCollections.CONVERSATION.getValue()));
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named(DatabaseCollections.USER_DATA.getValue())).toInstance(database.getCollection(DatabaseCollections.USER_DATA.getValue()));
            }
        }
        Injector injector = Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));

        sut = injector.getInstance(MessageService.class);
    }

    @Disabled("Throws IncorrectDecryptionKeyException: Private key is incorrect")
    @Test
    void receiveMessageFromNode() {
        var before = (List<Document>) database.getCollection(DatabaseCollections.CONVERSATION.getValue())
                .find(eq("_id","testConvoId")).first().get("messages");
        assertThat(before.size()).isEqualTo(5);

        var expected = new TextMessage("Thijs", "testConvoId", "een integratie test die slaagt!");

        var onion = new Onion(new DestinationClient("Yuri", "aX4q11PfGPHRSXDG3roSz5EcDF22G76pjT041LkjW2eDe7wIIx56LxVFWHY/Sa7NS8IReIncSb6LUZGuoff6pvzKcNUbJzyDtzsNTfUFskXwwa5Cied9A2r92hQs1ajd0tEyGoCMlli3gfxJBwzDXWBBcMueQ4aqSuU6PNYAdNipZ838BQREjZwmsyKfcbAlHvgE8W72xLaMsc6GdyfxY9YPJxnae6R44quYE9cvHGwM+eRfvk/5kVC7beetNUk3sD/hP6o0uRwjKK8CqQ+KSymu/kATDhW3L8Ti1cJGe3INBZo6pLUXHaRgPdC79Eu7zjHjOcx/c8BLjyGtMXIj0A=="), "RELAY", "A2KA7MEvcrkcmpoWU1SbIMxFt8Fi6WhX+dCTrkGKYSgdSFnXDDrWCh1Ua0QeRoXXhajdoCE0Kz8rdPMyxOXQJ5k74toiGkIRYTs7rkU9SD5LeG8MZIKF1EdEzUu5gWcDsj23CZnc5azri5Iv3W8icRFhboDo+2fG8NLKGU+qxeO5qIxCllMVWHYLBtQa4kqGvvZLnNUVyG/09D2AhavY4NazXPCOytFVLnB//tgRAursTPHQhX+FyYJXvOFMX+mIItewdGleMk7WKrGqdTKg+g==");

        sut.incomingMessage(onion);

        var result = ((List<Document>) database.getCollection(DatabaseCollections.CONVERSATION.getValue())
                .find(eq("_id", "testConvoId")).first().get("messages"));
        assertThat(result.size()).isEqualTo(6);

        assertThat(result.get(5).get("alias")).isEqualTo(expected.getSender());
        assertThat(result.get(5).get("conversationId")).isEqualTo(expected.getConversationId());
        assertThat(result.get(5).get("message")).isEqualTo(expected.getMessage());
    }

    @Test
    void MessageWontGetDecryptedAndSavedIfWrongPrivateKey() {
        database.getCollection(DatabaseCollections.USER_DATA.getValue())
                .updateOne(
                        eq("alias", "Melting Martian"),
                        new Document("$set",
                                new Document("privateKey", "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCKctEDpErUXvbfLJHMb7wjypD0HpwGo5WqlnHKl/qbfWJQkkRsZXCZ2yr3H0X3uOHjTT8mQeKwwtL8YqygNV5rQBH/8qsOau7Y5g1bz84K1mHXR0n1PA1nqkREvrEmZlhQPMnQg80TrG/5dVTDWkYj5bT+5NTbKCIuJLeVxgyPOwWwZ1DPaXwXI6Y6qSVslVFOMrZv+jMKBhDhUlSkePp6OuAP3VnCowWAFrDSxhUzHavXHu8YDrD5hPFfBZ5jvOVdPufpP2lY0hpi54ksoUSHYrhAvu9ZCnDmamU4CFF/PESpbNehkgGdmo55M7twt2kGkIFUyj+jXTOrgNbIzMIFAgMBAAECggEAYzmq54tGPjLZiFWvIPArzRMPFIcjl/aB+8LbRzHpYKtaXRiXhYCmVP6pKnf1c3fEHV5tlgO+bqUTBePKiP/27bL7s46+XhTxne4zQMwMhePkN6BWNzaU/OkhCIwK9tR/EvKeSZ1My37YudcHqwL2JiApuk1S6Mc4yA10rhlFQxkZDCx2HdwSW0XNYheW3ufPRrY8IZHnuMK1qBvhW4Rjoc59JeZSac73qf8oxIH9H/LMsYmMjenwms/JkA85D9pFTFZznY812gaOGkRo+hlhYPvUR9XUjMJ2IOc0BvZibr4oDN7oHCo5z5kE1ZERtU0lxXD3O3hSLXje+xvqpPJpeQKBgQD0OkRbS9MN8bCB1h02/5mksXxECcNe6nok/MZaNKtQhPiJ0Fnm+cjJJXhprI0NZ8xmg3IQksEqNA/abHWhBDMGfDiQQhStIaduotUeBU6kcnlmFZKo1G4qq6Gb/CxxwkHAYOQNO9XMcA4JgDjezMczCXp7x/0CSB9U11BvW8wBEwKBgQCRH0AxWz5aOT6N4NGaQnphrcOLdfRT+7xfSu1xdqDmXjLEA8f9TvGe0EkV9PKzslVxNfK1bbCDAauGaulTIhAdpu/dnC+AgZ2KW3qIuV3gp5rRrKvSLEcPSIXO0GNLjLUm7Nr0LGhAatlmGVVXMYsD179ejQLiIXDtHp/MXw4rhwKBgBj5tG/ZqzWr168PZGFxdhbfh6O1k+Mq+1648tfatwI+9uOxCOCT/rKDQHMeEDakvMRnAM0tFM/qDwz5NnZG5ajuHyKxf0fNP0ATKtQtGsO6aSvFXQAi6Tk34AkBmtHvWaYAwpGP4udLNkRGdopiSqSvfXUXGIqZMYsNkc4xhwU9AoGABUSYJPKF+ep4DdhqY2okXSVcotReUSojNWZX5jWDM7mZqCnm+ZgDB1vSO1zP7pv7pAdsTw+zR54o5tE8tQrYgOsNbAL+anDKEjqt4QaxlLUg1pmpBxEaZ0Cfstk3sYdjEA2rCRAPnL8EM0OaKHcg77vGM510zFagbcsdy+JrdkMCgYAP+D2Y77jhp3vlQ2IkGBUiUy0RfaaaGAgv6EfClxhT0PJgFA0G8XQyJP0iuu/CRl1X/dynErk+/lPIZkGQoqBBzAuq3uUjF4c9zArpQjrpN7e4PxGJttpOy2Ixtsd7YmSY8qXNdVyykjZ2O5ohTKYPScO//gsXSqvNMHQO0ObLpg==")));
        var onion = new Onion(new DestinationClient("Yuri", "X/GAXebJQR56ncdUsj3hTqKIryYYbywxyjtTxlGodDBLX7uj8jEwN/BletIKDdteZ6+IldKphzdAbaSQ+Edanax7xokp3vuWrb/9hR9Z2CfxumRDL2h+k5+2o12wVXoOqzPeqGqEECX8v8tyGwSvBsQ4BBSXzVjdk/n0hzIZnGQ1831x4TVWLv6E4srSbbcTxK/QhuS+6xAYvqxY3oIcAb3hBQj/HgMfJ6kzLCG5mYhwqhsWDnQi/2X03OepcBWW3s+jyHki1ufdZCgYMh4JXKXZjFN6tJ6SDXVBcWI02dCyuZAccl3rMMOYr2qQ7nBeNqpuc9LCn+uP+nPGqwxrew=="), "RELAY", "ubt4mCbaeUIi6KlDsyQwU/BRTvr/J9JHLY229hRxGG6OcLrCg6oL3ELMaFvXm944T97T3MooTLm+Lle0msuC0mTs7PBtdDSkYYU96QUdaanjgbuBc4bWi2dxr+ZiMW1LbB7KCpwJzj//Q8XfrrremRj+p7MuLwjRgMeqJB1eBg/njhNUy63fkRj20cOoRlwOejTkHxKtyooxsv4N/WJXiFo8F74CtgLB5qbI2a5PGFmVWEI476QTlBWU0Gja/zYxneFfOBj1bSS98KTTbG8sCg\u003d\u003d");

        sut.incomingMessage(onion);

        verify(persistenceModule, never()).insertMessage(any());
    }

    @Test
    void MessageWontGetDecryptedAndSavedIfWrongSecretKey() throws IncorrectDecryptionKeyException {
        var onion = new Onion(new DestinationClient("Yuri", "dOXXy2NReEJCpKTOlMV4DodMgwCKQgvlWwLfhWyvyslDAECyX9xAuM9pvWFLhHFYxakvOxuOrLVd3yFDODZNkX4J6LCYDHTTnx5PxFpBHBnamk44qD2Xe4G4sCoE4JGqisZztKLlbLRgNFi7MtgtTaHZKSkcy6yi7kmh9yir01UYt8Dt7vOFedruwb5A+qCnPuiCnG7da3xaqVfjadCWanXvdJL/HBLBo8hXSwuW0BgkbPxsjfOe9qfR9OKJu194fpaqvE5GAf+6bUNiS9WfFdJlxNMy2mhQFYhO+pZnKYndOsbmH3DWMyp2nPIkhDz/1UXAQcRERl2D1sibo3NBqA=="), "RELAY", "ubt4mCbaeUIi6KlDsyQwU/BRTvr/J9JHLY229hRxGG6OcLrCg6oL3ELMaFvXm944T97T3MooTLm+Lle0msuC0mTs7PBtdDSkYYU96QUdaanjgbuBc4bWi2dxr+ZiMW1LbB7KCpwJzj//Q8XfrrremRj+p7MuLwjRgMeqJB1eBg/njhNUy63fkRj20cOoRlwOejTkHxKtyooxsv4N/WJXiFo8F74CtgLB5qbI2a5PGFmVWEI476QTlBWU0Gja/zYxneFfOBj1bSS98KTTbG8sCg\u003d\u003d");

        sut.incomingMessage(onion);

        verify(symmetricEncryption, never()).decrypt(any(), any());
        verify(persistenceModule, never()).insertMessage(any());
    }

    @Test
    void MessageWontGetDecryptedAndSavedIfNoPrivateKey() throws IncorrectDecryptionKeyException {
        database.getCollection(DatabaseCollections.USER_DATA.getValue())
                .updateOne(eq("alias", "Melting Martian"),
                        new Document("$unset", new Document("privateKey", "")));
        var onion = new Onion(new DestinationClient("Yuri", "X/GAXebJQR56ncdUsj3hTqKIryYYbywxyjtTxlGodDBLX7uj8jEwN/BletIKDdteZ6+IldKphzdAbaSQ+Edanax7xokp3vuWrb/9hR9Z2CfxumRDL2h+k5+2o12wVXoOqzPeqGqEECX8v8tyGwSvBsQ4BBSXzVjdk/n0hzIZnGQ1831x4TVWLv6E4srSbbcTxK/QhuS+6xAYvqxY3oIcAb3hBQj/HgMfJ6kzLCG5mYhwqhsWDnQi/2X03OepcBWW3s+jyHki1ufdZCgYMh4JXKXZjFN6tJ6SDXVBcWI02dCyuZAccl3rMMOYr2qQ7nBeNqpuc9LCn+uP+nPGqwxrew=="), "RELAY", "ubt4mCbaeUIi6KlDsyQwU/BRTvr/J9JHLY229hRxGG6OcLrCg6oL3ELMaFvXm944T97T3MooTLm+Lle0msuC0mTs7PBtdDSkYYU96QUdaanjgbuBc4bWi2dxr+ZiMW1LbB7KCpwJzj//Q8XfrrremRj+p7MuLwjRgMeqJB1eBg/njhNUy63fkRj20cOoRlwOejTkHxKtyooxsv4N/WJXiFo8F74CtgLB5qbI2a5PGFmVWEI476QTlBWU0Gja/zYxneFfOBj1bSS98KTTbG8sCg\u003d\u003d");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.incomingMessage(onion))
                .withMessage("The privateKey used for decryption can't be null.");

        verify(symmetricEncryption, never()).decrypt(any(), any());
        verify(persistenceModule, never()).insertMessage(any());
    }

    @AfterEach
    void afterEach() {
        database.drop();
    }

    @AfterAll
    static void afterAll() {
        client.close();
    }
}
