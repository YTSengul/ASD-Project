package nl.han.asd.submarine.connection;

import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;

import java.net.Socket;

public interface SendMessage {
    void sendMessage(Onion onion);
    Socket connectWithDestination(DestinationRelay destination);
}
