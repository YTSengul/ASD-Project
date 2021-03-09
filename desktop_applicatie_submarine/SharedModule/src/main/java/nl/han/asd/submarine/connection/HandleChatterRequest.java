package nl.han.asd.submarine.connection;

import nl.han.asd.submarine.models.routing.Onion;

public interface HandleChatterRequest {
    Integer handleChatter(Onion onion);
}
