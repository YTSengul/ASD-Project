package nl.han.asd.submarine.connection;

import nl.han.asd.submarine.models.routing.Onion;

public interface ConnectionModule {
    void connect(String host, int port);
    void listenForIncomingMessages(int port);
    void sendMessage(Onion onion);
}
