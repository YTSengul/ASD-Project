package nl.han.asd.submarine.models.routing;

import nl.han.asd.submarine.models.routing.destination.Destination;

public class Node {
    private final Destination destination;
    private final String key;

    public Node(Destination destination, String key) {
        this.destination = destination;
        this.key = key;
    }

    public Destination getDestination() {
        return destination;
    }

    public String getKey() {
        return key;
    }
}
