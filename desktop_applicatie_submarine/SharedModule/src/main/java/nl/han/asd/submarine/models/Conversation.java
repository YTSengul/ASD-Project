package nl.han.asd.submarine.models;

import nl.han.asd.submarine.models.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    private final String id;
    private final String title;
    private final List<Contact> participants;
    private final List<Message> messages;

    public Conversation(String id, String title, List<Contact> participants, List<Message> messages) {
        this.id = id;
        this.title = title;
        this.participants = participants;
        this.messages = messages;
    }

    public Conversation(String title, List<Contact> participants) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.participants = participants;
        this.messages = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public List<Contact> getParticipants() {
        return participants;
    }

    public void addParticipant(Contact participant) {
       this.participants.add(participant);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }


}
