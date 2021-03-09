package nl.han.asd.submarine;

import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.persistence.PersistContacts;
import nl.han.asd.submarine.persistence.PersistConversations;
import nl.han.asd.submarine.persistence.PersistMessages;
import nl.han.asd.submarine.persistence.PersistUserData;
import nl.han.asd.submarine.models.*;
import nl.han.asd.submarine.persistence.PersistContacts;
import nl.han.asd.submarine.persistence.PersistConversations;
import nl.han.asd.submarine.persistence.PersistMessages;
import nl.han.asd.submarine.persistence.PersistUserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceModuleImplTest {

    @Mock
    private PersistContacts mockPersistContacts;
    @Mock
    private PersistConversations mockPersistConversations;
    @Mock
    private PersistMessages mockPersistMessages;
    @Mock
    private PersistUserData mockPersistUserData;
    @Mock
    private Contact mockContact;
    @Mock
    private Conversation mockConversation;
    @Mock
    private UserData mockUserData;
    @Mock
    private Message mockMessage;
    @Mock
    private Chatter mockChatter;

    @InjectMocks
    private PersistenceModuleImpl sut;

    @BeforeEach
    void setUp() {
        sut = new PersistenceModuleImpl();
        MockitoAnnotations.initMocks(this);
    }

    
    @Test
    void deleteContactCallsCorrectMethod () {
        // Arrange

        // Act
        sut.deleteContact(mockContact);
        // Assert
        verify(mockPersistContacts).deleteContact(mockContact);
    }

    @Test
    void deleteConversationCallsCorrectMethod () {
        // Arrange

        // Act
        sut.deleteConversation(1);
        // Assert
        verify(mockPersistConversations).deleteConversation(1);
    }

    @Test
    void getAliasCallsCorrectMethod () {
        // Arrange
        when(mockPersistUserData.getAlias()).thenReturn("alias");
        // Act & Assert
        assertThat(sut.getAlias()).isEqualTo("alias");
        verify(mockPersistUserData).getAlias();
    }

    @Test
    void getChatParticipantsCallsCorrectMethod () {
        // Arrange
        var contactList = getMockList(mockContact);
        when(mockPersistConversations.getChatParticipants("test")).thenReturn(contactList);
        // Act & Assert
        assertThat(sut.getChatParticipants("test")).isEqualTo(contactList);
        verify(mockPersistConversations).getChatParticipants("test");
    }
    
    @Test
    void getContactCallsCorrectMethod () {
        // Arrange
        when(mockPersistContacts.getContact(1)).thenReturn(mockContact);
        // Act & Assert
        assertThat(sut.getContact(1)).isEqualTo(mockContact);
        verify(mockPersistContacts).getContact(1);
    }
    
    @Test
    void getContactByAliasCallsCorrectMethod () {
        // Arrange
        when(mockPersistContacts.getContactByAlias("test")).thenReturn(mockContact);
        // Act & Assert
        assertThat(sut.getContactByAlias("test")).isEqualTo(mockContact);
        verify(mockPersistContacts).getContactByAlias("test");
    }
    
    @Test
    void getContactListCallsCorrectMethod () {
        // Arrange
        var contactList = getMockList(mockContact);
        when(mockPersistContacts.getContactList()).thenReturn(contactList);
        // Act & Assert
        assertThat(sut.getContactList()).isEqualTo(contactList);
        verify(mockPersistContacts).getContactList();
    }
    
    @Test
    void getConversationCallsCorrectMethod () {
        // Arrange  
        when(mockPersistConversations.getConversation("test")).thenReturn(mockConversation);
        // Act & Assert
        assertThat(sut.getConversation("test")).isEqualTo(mockConversation);
    }

    @Test
    void getMessageListCallsCorrectMethod () {
        // Arrange
        var list = getMockList(mockMessage);
        when(mockPersistMessages.getMessageList("test")).thenReturn(list);
        // Act & Assert
        assertThat(sut.getMessageList("test")).isEqualTo(list);
        verify(mockPersistMessages).getMessageList("test");
    }
    
    @Test
    void getPrivateKeyOfUserCallsCorrectMethod () {
        // Arrange
        when(mockPersistUserData.getPrivateKeyOfUser()).thenReturn("privateKey");
        // Act & Assert
        assertThat(sut.getPrivateKeyOfUser()).isEqualTo("privateKey");
        verify(mockPersistUserData).getPrivateKeyOfUser();
    }
    
    @Test
    void hasContactWithAliasCallsCorrectMethod () {
        // Arrange
        when(mockPersistContacts.hasContactWithAlias("alias")).thenReturn(true);
        // Act
        assertThat(sut.hasContactWithAlias("alias")).isTrue();
        // Assert
        verify(mockPersistContacts).hasContactWithAlias("alias");
    }

    @Test
    void insertContactCallsCorrectMethod () {
        // Arrange

        // Act
        sut.insertContact(mockContact);
        // Assert
        verify(mockPersistContacts).insertContact(mockContact);
    }

    @Test
    void insertConversationCallsCorrectMethod () {
        // Arrange

        // Act
        sut.insertConversation(mockConversation);
        // Assert
        verify(mockPersistConversations).insertConversation(mockConversation);
    }

    @Test
    void insertMessageCallsCorrectMethod () {
        // Arrange

        // Act
        sut.insertMessage(mockMessage);
        // Assert
        verify(mockPersistMessages).insertMessage(mockMessage);
    }

    @Test
    void saveChatterCallsCorrectMethod () {
        // Arrange

        // Act
        sut.saveChatter(mockUserData);
        // Assert
        verify(mockPersistUserData).saveChatter(mockUserData);
    }

    @Test
    void updateContactCallsCorrectMethod () {
        // Arrange

        // Act
        sut.updateContact(mockContact);
        // Assert
        verify(mockPersistContacts).updateContact(mockContact);
    }

    @Test
    void updateConversationCallsCorrectMethod () {
        // Arrange

        // Act
        sut.updateConversation(mockConversation);
        // Assert
        verify(mockPersistConversations).updateConversation(mockConversation);
    }

    private static <T> List<T> getMockList(T content) {
        List<T> list = new ArrayList<>();
        list.add(content);
        return list;
    }

}
