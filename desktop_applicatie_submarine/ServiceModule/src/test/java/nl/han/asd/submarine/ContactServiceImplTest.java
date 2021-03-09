package nl.han.asd.submarine;

import nl.han.asd.submarine.exception.DuplicateContactException;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.persistence.PersistenceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ContactServiceImplTest {

    private static final String TEST_ALIAS = "TEST_ALIAS";
    private static final String TEST_KEY = "TEST_KEY";

    @Mock
    private PersistenceModule persistenceModuleMock;

    @InjectMocks
    private ContactServiceImpl sut;

    @Mock
    private Contact contactMock;

    @BeforeEach
    void setUp() {
        sut = new ContactServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testIfAddContactThrowsWhenAliasFound () {
        // Arrange
        when(contactMock.getAlias()).thenReturn(TEST_ALIAS);

        when(persistenceModuleMock.hasContactWithAlias(eq(TEST_ALIAS))).thenReturn(true);
        // Act
        assertThatExceptionOfType(DuplicateContactException.class).isThrownBy(() -> sut.addContact(contactMock));
        // Assert
        verify(persistenceModuleMock).hasContactWithAlias(eq(TEST_ALIAS));
        verify(persistenceModuleMock, never()).insertContact(eq(contactMock));
    }

    @Test
    void testIfAddContactSucceeds () {
        // Arrange
        when(contactMock.getAlias()).thenReturn(TEST_ALIAS);

        when(persistenceModuleMock.hasContactWithAlias(eq(TEST_ALIAS))).thenReturn(false);
        // Act
        sut.addContact(contactMock);
        // Assert
        verify(persistenceModuleMock).hasContactWithAlias(eq(TEST_ALIAS));
        verify(persistenceModuleMock).insertContact(eq(contactMock));
    }
}