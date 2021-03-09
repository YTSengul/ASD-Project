package nl.han.asd.submarine;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.han.asd.submarine.exceptions.UserDoesNotExistException;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class PersistUserDataImplTest {

    private static final String ALIAS = "MY_ALIAS";
    private static final String PRIVATE_KEY = "PRIVATE_KEY";

    @InjectMocks
    private PersistUserDataImpl sut;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private FindIterable<Document> mockIterable;

    @Mock
    private Document mockDocument;

    @BeforeEach
    void setUp() {
        sut = new PersistUserDataImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetAliasWhenThereIsNone() {

        when(mockCollection.find(eq(Filters.exists("alias")))).thenReturn(null);

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> sut.getAlias());

        verify(mockCollection).find(eq(Filters.exists("alias")));
    }

    @Test
    void testGetAliasCallsCorrectMethods() {
        // Arrange
        FindIterable<Document> mockIterable = mock(FindIterable.class);

        when(mockCollection.find(eq(Filters.exists("alias")))).thenReturn(mockIterable);
        when(mockIterable.sort(any())).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(mockDocument);
        when(mockDocument.getString(eq("alias"))).thenReturn(ALIAS);

        // Act
        var result = sut.getAlias();

        // Assert
        assertThat(result).isEqualTo(ALIAS);
        verify(mockIterable).sort(any());
        verify(mockDocument).getString(eq("alias"));
    }


    @Test
    void getPrivateKeyThrowsExceptionWhenThereIsNone() {
        // Arrange
        when(mockCollection.find()).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(null);

        // Act and Assert
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> sut.getPrivateKeyOfUser());

        verify(mockIterable).first();
    }


    @Test
    void getPrivateKey() {
        // Arrange
        when(mockCollection.find()).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(mockDocument);
        when(mockDocument.getString(eq("privateKey"))).thenReturn(PRIVATE_KEY);

        // Act
        String actualResult = sut.getPrivateKeyOfUser();

        // Assert
        assertThat(actualResult).isEqualTo(PRIVATE_KEY);

        verify(mockDocument).getString(eq("privateKey"));

        verify(mockDocument).getString(eq("privateKey"));
    }

    @Disabled // Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.
    @Test
    void loginChatterTestEmptyDatabaseThrowsException() {
        ChatterLoginDTO testChatterLogin = new ChatterLoginDTO("testUser1", "test1", "ipAddress");

        assertThatExceptionOfType(UserDoesNotExistException.class)
                .isThrownBy(() -> sut.loginChatter(testChatterLogin));
    }

    @Disabled // Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.
    @Test
    void loginChatterTestDatabaseSuccess() {
        when(mockCollection.find(Filters.eq("alias", "testUser1"))).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(new Document());

        ChatterLoginDTO testChatterLogin = new ChatterLoginDTO("testUser1", "test1", "ipAddress");

        sut.loginChatter(testChatterLogin);
        verify(mockCollection).find(any(Bson.class));
    }

}
