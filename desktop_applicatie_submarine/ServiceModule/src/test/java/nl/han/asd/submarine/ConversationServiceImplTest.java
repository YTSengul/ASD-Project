package nl.han.asd.submarine;

import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.message.system.NewChat;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConversationServiceImplTest {

    @Captor
    private ArgumentCaptor<List<Contact>> participantCaptor;

    @Mock
    private PersistenceModule persistenceModuleMock;

    @Mock
    private MessageService messageServiceMock;

    @InjectMocks
    private ConversationService sut;

    @BeforeEach
    void setUp() {
        sut = new ConversationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getConversationCallsPersistence() {
        // Arrange
        when(persistenceModuleMock.getConversations()).thenReturn(null);

        // Act
        sut.getConversations();

        // Assert
        verify(persistenceModuleMock, times(1)).getConversations();
    }

    @Test
    void createConversationSuccessTest() {
        // Setup
        List<Contact> inputParticipants = new ArrayList<>(Arrays.asList(
                new Contact("Key", "Loathing Longneck"),
                new Contact("AnotherKey", "Purple Parrot"),
                new Contact("YetAnotherKey", "Tapping Turtle")));
        Contact inputContact = new Contact("MasterKey", "Masterful Mongoose");
        UserData inputUserData = new UserData(inputContact.getAlias(),
                inputContact.getPublicKey(), "Unused");
        List<Contact> expectedParticipants = new ArrayList<>(inputParticipants);
        expectedParticipants.add(inputContact);
        String expectedTitle = "Cool Boy's Tax Haven";
        Conversation expectedConversation = new Conversation(expectedTitle,
                expectedParticipants);
        NewChat expectedNewChat = new NewChat("ignored", expectedConversation.getTitle(), expectedParticipants);

        ArgumentCaptor<NewChat> newChatCaptor = ArgumentCaptor.forClass(NewChat.class);
        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);

        when(persistenceModuleMock.getUserData()).thenReturn(inputUserData);
        // Run
        sut.createConversation(expectedTitle, inputParticipants);
        // Check
        verify(persistenceModuleMock, times(1)).getUserData();

        verify(persistenceModuleMock, times(1)).insertConversation(conversationCaptor.capture());
        verify(messageServiceMock, times(1)).sendMessage(newChatCaptor.capture(), participantCaptor.capture());

        List<Contact> actualParticipants = participantCaptor.getValue();
        assertThat(actualParticipants.size()).isEqualTo(expectedParticipants.size() - 1);
        for (int i = 0; i < actualParticipants.size(); i++) {
            assertThat(actualParticipants.get(i)).isEqualToComparingFieldByField(expectedParticipants.get(i));
        }

        Conversation actualConversation = conversationCaptor.getValue();
        assertThat(actualConversation.getTitle()).isEqualTo(expectedTitle);
        actualParticipants = actualConversation.getParticipants();
        assertThat(actualParticipants.size()).isEqualTo(expectedParticipants.size());
        for (int i = 0; i < actualParticipants.size(); i++) {
            assertThat(actualParticipants.get(i)).isEqualToComparingFieldByField(expectedParticipants.get(i));
        }

        NewChat actualNewChat = newChatCaptor.getValue();
        assertThat(actualNewChat.getTitle()).isEqualTo(expectedTitle);
        actualParticipants = actualNewChat.getParticipants();
        assertThat(actualParticipants.size()).isEqualTo(expectedParticipants.size());
        for (int i = 0; i < actualParticipants.size(); i++) {
            assertThat(actualParticipants.get(i)).isEqualToComparingFieldByField(expectedParticipants.get(i));
        }
    }
}
