package nl.han.asd.submarine.models.routing.destination;

public class DestinationRelay extends Destination {
    private String hostname;
    private int port;

    public DestinationRelay(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void setHostname(String hostname){ this.hostname = hostname;}

    public String getHostname() {
        return hostname;
    }

    public void setPort(int port) { this.port = port;}

    public int getPort() {
        return port;
    }
}
