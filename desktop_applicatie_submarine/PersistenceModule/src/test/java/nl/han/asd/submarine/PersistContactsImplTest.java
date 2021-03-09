package nl.han.asd.submarine;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import nl.han.asd.submarine.models.Contact;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistContactsImplTest {

    private static final String TEST_ALIAS = "TEST_ALIAS";
    private static final String TEST_PUBLIC_KEY = "TEST_PUBLIC_KEY";
    private static final String TEST_ALIAS2 = "TEST_ALIAS2";
    private static final String TEST_PUBLIC_KEY2 = "TEST_PUBLIC_KEY2";


    @Mock
    private Document mockDocument;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private FindIterable<Document> mockIterable;

    @InjectMocks
    private PersistContactsImpl sut;

    @Captor
    private ArgumentCaptor<Document> insertOneCaptor;

    @BeforeEach
    public void setup() {
        sut = new PersistContactsImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testInsertingContactCallsCorrectly () {
        // Arrange

        // Act
        sut.insertContact(new Contact(TEST_PUBLIC_KEY, TEST_ALIAS));

        // Assert
        verify(mockCollection).insertOne(insertOneCaptor.capture());
        assertThat(insertOneCaptor.getValue().getString("alias")).isEqualTo(TEST_ALIAS);
        assertThat(insertOneCaptor.getValue().getString("publicKey")).isEqualTo(TEST_PUBLIC_KEY) ;
    }

    @Test
    void testGetAliasReturnsCorrectContact () {
        // Arrange
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        when(mockCollection.find(eq(Filters.eq("alias", TEST_ALIAS)))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        when(mockDocument.getString(eq("alias"))).thenReturn(TEST_ALIAS);
        when(mockDocument.getString(eq("publicKey"))).thenReturn(TEST_PUBLIC_KEY);

        // Act
        var result = sut.getContactByAlias(TEST_ALIAS);

        // Assert
        assertThat(result.getAlias()).isEqualTo(TEST_ALIAS);
        assertThat(result.getPublicKey()).isEqualTo(TEST_PUBLIC_KEY);

        verify(mockCollection).find(any(Bson.class));
        verify(mockFindIterable).first();
        verify(mockDocument, times(2)).getString(any());
    }


    @Test
    void testGetAliasReturnsNullWhenNotFound () {
        // Arrange
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        when(mockCollection.find(eq(Filters.eq("alias", TEST_ALIAS)))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Act
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> sut.getContactByAlias(TEST_ALIAS));

        // Assert
        verify(mockCollection).find(any(Bson.class));
        verify(mockFindIterable).first();
    }

    @Test
    void testGetContactListWithOneContacts () {
        // Arrange
        when(mockCollection.find()).thenReturn(mockIterable);
        when(mockIterable.into(anyList())).thenReturn(List.of(mockDocument));
        when(mockDocument.getString("publicKey")).thenReturn(TEST_PUBLIC_KEY);
        when(mockDocument.getString("alias")).thenReturn(TEST_ALIAS);
        // Act
        var result = sut.getContactList();
        // Assert

        assertThat(result.get(0).getPublicKey()).isEqualTo(TEST_PUBLIC_KEY);
        assertThat(result.get(0).getAlias()).isEqualTo(TEST_ALIAS);
        verify(mockDocument).getString("publicKey");
        verify(mockDocument).getString("alias");
        verify(mockIterable).into(anyList());
        verify(mockCollection).find();
    }

    @Test
    void testGetContactListWithMultipleContacts () {
        // Arrange
        when(mockCollection.find()).thenReturn(mockIterable);
        when(mockIterable.into(anyList())).thenReturn(List.of(mockDocument, mockDocument));
        when(mockDocument.getString("publicKey")).thenReturn(TEST_PUBLIC_KEY, TEST_PUBLIC_KEY2);
        when(mockDocument.getString("alias")).thenReturn(TEST_ALIAS, TEST_ALIAS2);
        // Act
        var result = sut.getContactList();
        // Assert
        assertThat(result.get(0).getPublicKey()).isEqualTo(TEST_PUBLIC_KEY);
        assertThat(result.get(0).getAlias()).isEqualTo(TEST_ALIAS);
        assertThat(result.get(1).getPublicKey()).isEqualTo(TEST_PUBLIC_KEY2);
        assertThat(result.get(1).getAlias()).isEqualTo(TEST_ALIAS2);
        verify(mockDocument, times(2)).getString("publicKey");
        verify(mockDocument, times(2)).getString("alias");
        verify(mockIterable).into(anyList());
        verify(mockCollection).find();
    }

    @Test
    void testHasContactWithAliasFindsAlias () {
        // Arrange
        when(mockCollection.find(Filters.eq("alias", TEST_ALIAS))).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(mockDocument);
        // Act & Assert
        assertThat(sut.hasContactWithAlias(TEST_ALIAS)).isTrue();
        verify(mockCollection).find(Filters.eq("alias", TEST_ALIAS));
        verify(mockIterable).first();
    }
    @Test
    void testHasContactWithAliasWhenAliasNotFound () {
        // Arrange
        when(mockCollection.find(Filters.eq("alias", TEST_ALIAS))).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(null);
        // Act & Assert
        assertThat(sut.hasContactWithAlias(TEST_ALIAS)).isFalse();
        verify(mockCollection).find(Filters.eq("alias", TEST_ALIAS));
        verify(mockIterable).first();
    }
}
