package nl.han.asd.submarine.models.routing;

import nl.han.asd.submarine.models.routing.destination.Destination;

public class Onion {

    public Onion(Destination destination, String command, String data) {
        this.destination = destination;
        this.command = command;
        this.data = data;
    }

    public Onion(Destination destination, String data) {
        this.destination = destination;
        this.data = data;
    }

    private Destination destination;
    private String command;
    private String data;

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
