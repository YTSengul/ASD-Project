package nl.han.asd.submarine.service;

import nl.han.asd.submarine.models.Contact;

import java.util.List;
import java.util.Map;

public interface ConversationService {
    Map<String, String> getConversations();
    void createConversation(String title, List<Contact> participants);
}
