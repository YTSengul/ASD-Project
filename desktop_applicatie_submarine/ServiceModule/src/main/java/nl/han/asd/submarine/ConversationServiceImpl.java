package nl.han.asd.submarine;

import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.message.system.NewChat;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.service.ConversationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversationServiceImpl implements ConversationService {

    @Inject
    private PersistenceModule persistenceModule;

    @Inject
    private MessageService messageService;

    @Override
    public Map<String, String> getConversations() {
        return persistenceModule.getConversations();
    }

    @Override
    public void createConversation(String title, List<Contact> participants) {
        Conversation conversation = new Conversation(title, new ArrayList<>(participants));

        UserData userData = persistenceModule.getUserData();
        Contact currentUser = new Contact(userData.getPublicKey(), userData.getAlias());
        conversation.addParticipant(currentUser);

        persistenceModule.insertConversation(conversation);
        messageService.sendMessage(new NewChat(conversation.getId(), title, conversation.getParticipants()), participants);
    }
}
