package nl.han.asd.submarine.persistence;

import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;

import java.util.List;
import java.util.Map;

public interface PersistConversations {
    void insertConversation(Conversation conversation);
    void updateConversation(Conversation conversation);
    void deleteConversation(int conversationId);
    Conversation getConversation(String chatIdentifier);
    Map<String, String> getConversations();
    List<Contact> getChatParticipants(String chatIdentifier);
}
