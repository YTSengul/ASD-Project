package nl.han.asd.submarine;

import nl.han.asd.submarine.connection.ConnectionModule;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.exception.SocketConnectionException;
import nl.han.asd.submarine.models.routing.Onion;

import javax.inject.Inject;
import java.io.IOException;
import java.net.Socket;

public class ConnectionModuleImpl implements ConnectionModule {

    @Inject
    private SendMessage sendMessage;

    private Socket socket;

    @Inject
    private MessageListener listener;

    @Override
    public void sendMessage(Onion onion) {
        sendMessage.sendMessage(onion);
    }

    @Override
    public void connect(String host, int port) {
        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            throw new SocketConnectionException("Failed to connect");
        }
    }

    public void listenForIncomingMessages(int localPort) {
        listener.setLocalPort(localPort);
        new Thread(listener).start();
    }
}
