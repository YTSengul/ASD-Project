package nl.han.asd.submarine;

import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.persistence.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class PersistenceModuleImpl implements PersistenceModule {

    @Inject
    private PersistContacts persistContacts;

    @Inject
    private PersistConversations persistConversations;

    @Inject
    private PersistMessages persistMessages;

    @Inject
    private PersistUserData persistUserData;

    @Override
    public void insertContact(Contact contact) {
        persistContacts.insertContact(contact);
    }

    @Override
    public void updateContact(Contact contact) {
        persistContacts.updateContact(contact);
    }

    @Override
    public void deleteContact(Contact contact) {
        persistContacts.deleteContact(contact);
    }

    @Override
    public Contact getContact(int contactId) {
        return persistContacts.getContact(contactId);
    }

    @Override
    public boolean hasContactWithAlias(String alias) {
        return persistContacts.hasContactWithAlias(alias);
    }

    @Override
    public Contact getContactByAlias(String alias) {
        return persistContacts.getContactByAlias(alias);
    }

    @Override
    public List<Contact> getContactList() {
        return persistContacts.getContactList();
    }

    @Override
    public void insertContacts(List<Contact> contacts) {
        persistContacts.insertContacts(contacts);
    }

    @Override
    public void insertConversation(Conversation conversation) {
        persistConversations.insertConversation(conversation);
    }

    @Override
    public void updateConversation(Conversation conversation) {
        persistConversations.updateConversation(conversation);
    }

    @Override
    public void deleteConversation(int conversationId) {
        persistConversations.deleteConversation(conversationId);
    }

    @Override
    public Conversation getConversation(String chatIdentifier) {
        return persistConversations.getConversation(chatIdentifier);
    }

    @Override
    public Map<String, String> getConversations() {
        return persistConversations.getConversations();
    }

    @Override
    public void insertMessage(Message message) {
        persistMessages.insertMessage(message);
    }

    @Override
    public List<Message> getMessageList(String chatIdentifier) {
        return persistMessages.getMessageList(chatIdentifier);
    }

    @Override
    public String getAlias() {
        return persistUserData.getAlias();
    }

    @Override
    public String getPublicKey() {
        return persistUserData.getPublicKey();
    }

    @Override
    public String getPrivateKeyOfUser() {
        return persistUserData.getPrivateKeyOfUser();
    }

    @Override
    public UserData getUserData() {
        return persistUserData.getUserData();
    }

    @Override
    public void saveChatter(UserData userData) {
        persistUserData.saveChatter(userData);
    }

    @Override
    public void loginChatter(ChatterLoginDTO chatterLoginDTO) {
        persistUserData.loginChatter(chatterLoginDTO);
    }

    @Override
    public List<Contact> getChatParticipants(String chatIdentifier) {
        return persistConversations.getChatParticipants(chatIdentifier);
    }

}
