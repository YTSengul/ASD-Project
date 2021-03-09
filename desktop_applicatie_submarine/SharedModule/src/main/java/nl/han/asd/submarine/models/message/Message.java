package nl.han.asd.submarine.models.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {

    private String sender;
    private LocalDateTime timestamp;



    private final String conversationId;

    public Message(String sender, LocalDateTime timestamp,
                   String conversationId) {
        this.sender = sender;
        this.conversationId = conversationId;
        this.timestamp = timestamp;
    }

    public Message(String sender, String conversationId) {
        this.sender = sender;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
