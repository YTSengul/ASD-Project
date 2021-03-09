package nl.han.asd.submarine;

import nl.han.asd.submarine.message.MessageHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageListener implements Runnable {
    private static final Logger LOG = Logger.getLogger(MessageListener.class.getName());

    @Inject
    private MessageHandler messageHandler;

    @Inject
    private ServerSocketFactory serverSocketFactory;

    private boolean listeningForIncomingMessages = true;
    private int localPort;

    @Override
    public void run() {
        LOG.log(Level.INFO, () -> "Listening on port: " + localPort);
        try (ServerSocket serverSocket = serverSocketFactory.createSocket(localPort)) {
            LOG.log(Level.INFO, "Waiting for clients to connect in separate thread...");
            while (listeningForIncomingMessages) {
                listenForIncomingMessages(serverSocket);
            }
        } catch (IOException | InterruptedException ex) {
            LOG.log(Level.SEVERE, "Error: could not connect to socket");
            throw new IllegalStateException("Could not connect to socket");
        }
    }

    void listenForIncomingMessages(ServerSocket serverSocket) throws IOException, InterruptedException {
        Socket newSocket = serverSocket.accept();
        messageHandler.handleIncomingMessage(newSocket);
        Thread.sleep(10);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void stopListeningForIncomingMessages() {
        listeningForIncomingMessages = false;
    }

}
