package nl.han.asd.submarine.models.message;

import java.time.LocalDateTime;

public class FileMessage extends Message {

    private String path;

    public FileMessage(String path, String sender, LocalDateTime timeStamp, String conversationId) {
        super(sender, timeStamp, conversationId);
        this.path = path;
    }

    public FileMessage(String path, String sender, String conversationId) {
        super(sender, conversationId);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
