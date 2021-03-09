package nl.han.asd.submarine.integrationTests;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.han.asd.submarine.BootstrapModule;
import nl.han.asd.submarine.ContactServiceImpl;
import nl.han.asd.submarine.PersistContactsImpl;
import nl.han.asd.submarine.PersistenceModuleImpl;
import nl.han.asd.submarine.exception.DuplicateContactException;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistContacts;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.service.ContactService;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class SaveContactIT {

    private static final String TEST_ALIAS = "TEST_ALIAS";
    private static final String TEST_PUBLIC_KEY = "TEST_PUBLIC_KEY";

    protected static MongoClient mongoClient;

    protected MongoCollection<Document> contactCollectionSpy;
    protected ContactService contactServiceSpy = Mockito.spy(new ContactServiceImpl());
    protected PersistContacts persistContactsSpy = Mockito.spy(new PersistContactsImpl());
    protected PersistenceModule persistenceModuleSpy = Mockito.spy(new PersistenceModuleImpl());

    @Mock
    private MongoCollection<Document> conversationCollectionMock;

    private Injector injector;

    @Captor
    private ArgumentCaptor<Document> insertOneDocumentCaptor;

    @BeforeAll
    static void beforeAll() {
        mongoClient = new MongoClient("localhost", 27017);
    }

    @BeforeEach
    void setUp() {
        contactCollectionSpy = Mockito.spy(mongoClient.getDatabase("integrationTests")
            .getCollection(DatabaseCollections.CONTACT.getValue()));
        class TestBootstrapModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(new TypeLiteral<MongoCollection<Document>>(){}).annotatedWith(Names.named(DatabaseCollections.CONTACT.getValue())).toInstance(contactCollectionSpy);
                bind(new TypeLiteral<MongoCollection<Document>>(){}).annotatedWith(Names.named(DatabaseCollections.CONVERSATION.getValue())).toInstance(conversationCollectionMock);
                bind(ContactService.class).toInstance(contactServiceSpy);
                bind(PersistContacts.class).toInstance(persistContactsSpy);
                bind(PersistenceModule.class).toInstance(persistenceModuleSpy);
            }
        }

        this.injector =
                Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));
    }

    @Test
    void saveTheContactToDatabase() {
        // Arrange
        Contact contact = new Contact(TEST_PUBLIC_KEY, TEST_ALIAS);

        // Act
        contactServiceSpy.addContact(contact);

        // Assert
        verify(persistenceModuleSpy).hasContactWithAlias(eq(TEST_ALIAS));
        verify(persistContactsSpy).hasContactWithAlias(eq(TEST_ALIAS));

        verify(persistenceModuleSpy).insertContact(eq(contact));
        verify(persistContactsSpy).insertContact(eq(contact));

        Document document = contactCollectionSpy.find(Filters.eq("alias", TEST_ALIAS)).first();
        assertThat(document.getString("alias")).isEqualTo(TEST_ALIAS);
        assertThat(document.getString("publicKey")).isEqualTo(TEST_PUBLIC_KEY);
    }

    @Test
    void saveTheContactToFailsWhenContactIsInDatabase() {
        // Arrange
        Contact contact = new Contact(TEST_PUBLIC_KEY, TEST_ALIAS);
        contactCollectionSpy.insertOne(new Document("alias", TEST_ALIAS).append("publicKey", TEST_PUBLIC_KEY));
        // Act
        assertThatExceptionOfType(DuplicateContactException.class)
                .isThrownBy(() -> contactServiceSpy.addContact(contact));

        // Assert
        verify(persistenceModuleSpy).hasContactWithAlias(eq(TEST_ALIAS));
        verify(persistContactsSpy).hasContactWithAlias(eq(TEST_ALIAS));

        verify(persistenceModuleSpy, never()).insertContact(eq(contact));
        verify(persistContactsSpy, never()).insertContact(eq(contact));

        var iterator = contactCollectionSpy.find(Filters.eq("alias", TEST_ALIAS)).iterator();
        assertThat(iterator.hasNext()).isTrue(); // There should be one value
        iterator.next();
        assertThat(iterator.hasNext()).isFalse();
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
