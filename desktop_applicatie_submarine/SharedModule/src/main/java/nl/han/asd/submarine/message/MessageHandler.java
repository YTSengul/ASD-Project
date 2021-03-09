package nl.han.asd.submarine.message;

import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.routing.Onion;

import java.net.Socket;

public interface MessageHandler {

    void incomingMessage(Onion onion);
    void sendTextMessage(String conversationId, String message);

    Conversation getMessages(String chatIdentifier);
    String getAlias();

    void handleIncomingMessage(Socket socket);
}
