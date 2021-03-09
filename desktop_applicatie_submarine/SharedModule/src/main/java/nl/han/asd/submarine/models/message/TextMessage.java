package nl.han.asd.submarine.models.message;

import java.time.LocalDateTime;

public class TextMessage extends Message {
    private final String message;

    public TextMessage(String sender, String conversationId, String message) {
        super(sender, conversationId);
        this.message = message;
    }

    public TextMessage(String sender, String conversationId, LocalDateTime timestamp, String message) {
        super(sender, timestamp, conversationId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
