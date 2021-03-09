package nl.han.asd.submarine.models.message.system;

import nl.han.asd.submarine.models.Contact;

import java.util.List;

public class NewChat extends SystemMessage {
    private final String title;
    private final List<Contact> participants;

    public NewChat(String conversationId, String title, List<Contact> participants) {
        super(SystemCommand.NEW_CHAT, conversationId);
        this.title = title;
        this.participants = participants;
    }

    public String getTitle() {
        return title;
    }

    public List<Contact> getParticipants() {
        return participants;
    }
}
