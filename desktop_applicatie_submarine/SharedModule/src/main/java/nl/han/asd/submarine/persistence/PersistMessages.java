package nl.han.asd.submarine.persistence;

import nl.han.asd.submarine.models.message.Message;

import java.util.List;

public interface PersistMessages {
    void insertMessage(Message message);
    List<Message> getMessageList(String chatIdentifier);
}
