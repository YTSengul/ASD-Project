package nl.han.asd.submarine.models;

import nl.han.asd.submarine.models.routing.destination.DestinationRelay;

public class NodeDTO {

    public DestinationRelay getDestination() {
        return destination;
    }

    public String getKey() {
        return key;
    }

    private final DestinationRelay destination;
    private final String key;

    public NodeDTO(DestinationRelay destination, String key) {
        this.destination = destination;
        this.key = key;
    }
}