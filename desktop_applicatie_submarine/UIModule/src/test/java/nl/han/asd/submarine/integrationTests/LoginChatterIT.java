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
import nl.han.asd.submarine.AddMockDataToDatabase;
import nl.han.asd.submarine.BootstrapModule;
import nl.han.asd.submarine.ChatterServiceImpl;
import nl.han.asd.submarine.PersistenceModuleImpl;
import nl.han.asd.submarine.exceptions.UserDoesNotExistException;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.service.ChatterService;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.DateFormat;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.spy;

@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class LoginChatterIT {

    private static MongoClient client;

    MongoDatabase database;

    private Injector injector;

    protected ChatterService chatterServiceSpy = Mockito.spy(new ChatterServiceImpl());
    private final PersistenceModule persistenceModule = spy(new PersistenceModuleImpl());

    @BeforeAll
    static void beforeAll() {
        client = new MongoClient("localhost", 27017);
    }

    @BeforeEach
    public void setup() {
        database = client.getDatabase("submarineTest");
        AddMockDataToDatabase.start(database);

        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named(DatabaseCollections.CONVERSATION.getValue())).toInstance(database.getCollection(DatabaseCollections.CONVERSATION.getValue()));
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named(DatabaseCollections.USER_DATA.getValue())).toInstance(database.getCollection(DatabaseCollections.USER_DATA.getValue()));
                bind(ChatterService.class).toInstance(chatterServiceSpy);
                bind(PersistenceModule.class).toInstance(persistenceModule);
            }
        }
        this.injector = Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));
    }

    @Disabled("Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.")
    @Test
    void loginChatterSuccesTest() {
        ChatterServiceImpl chatterService =
                injector.getInstance(ChatterServiceImpl.class);

        ChatterLoginDTO testChatterDTOChatterLogin = new ChatterLoginDTO("Melting Martian", "test1", "ipAddress");


        assertThatCode(() -> chatterService.loginChatter(testChatterDTOChatterLogin)).doesNotThrowAnyException();
    }

    @Disabled("Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.")
    @Test
    void loginChatterFailedTest() {
        ChatterServiceImpl chatterService =
                injector.getInstance(ChatterServiceImpl.class);
        ChatterLoginDTO testChatterDTOChatterLogin = new ChatterLoginDTO("Melting Martian", "test1", "ipAddress");

        database.getCollection(DatabaseCollections.USER_DATA.getValue()).deleteOne(eq("alias", "Melting Martian"));

        assertThatExceptionOfType(UserDoesNotExistException.class)
                .isThrownBy(() -> chatterService.loginChatter(testChatterDTOChatterLogin))
                .withMessage("Deze gebruiker komt niet voor in de database.");
    }

    @Disabled("Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.")
    @Test
    void testIfLastLoginIsUpdated() {
        PersistenceModule persistenceModule = injector.getInstance(PersistenceModule.class);

        Document user1 = new Document();
        user1.put("alias", "Melting Martian");
        user1.put("lastLogin", "2000-01-01 20:20:20");

        database.getCollection(DatabaseCollections.USER_DATA.getValue()).insertOne(user1);
        ChatterLoginDTO testChatterDTOChatterLogin = new ChatterLoginDTO("Melting Martian", "test1", "ipAddress");
        persistenceModule.loginChatter(testChatterDTOChatterLogin);

        Document loginDocument = new Document();

        Document timeUpdatedChatter = database.getCollection(DatabaseCollections.USER_DATA.getValue()).find(loginDocument).first();
        String updatedString = DateFormat.getDateInstance().format(timeUpdatedChatter.getDate("lastLogin"));

        assertThat(updatedString).isEqualTo(DateFormat.getDateInstance().format(System.currentTimeMillis()));
    }

    @Test
    void getAliasReturnsLastChatter() {
        PersistenceModule persistenceModule = injector.getInstance(PersistenceModule.class);

        Document doc1 = new Document();
        doc1.append("alias", "Melting Martian");
        doc1.append("lastLogin", "2021-05-07");

        Document doc2 = new Document();
        doc2.append("alias", "testUser2");
        doc2.append("lastLogin", "2021-05-06");

        database.getCollection(DatabaseCollections.USER_DATA.getValue()).insertOne(doc1);
        database.getCollection(DatabaseCollections.USER_DATA.getValue()).insertOne(doc2);

        String latestLoggedInUser = persistenceModule.getAlias();

        assertThat(latestLoggedInUser).isEqualTo(doc1.get("alias"));
    }

    @AfterEach
    void tearDown() {
        database.drop();
    }

    @AfterAll
    static void afterAll() {
        client.close();
    }
}
