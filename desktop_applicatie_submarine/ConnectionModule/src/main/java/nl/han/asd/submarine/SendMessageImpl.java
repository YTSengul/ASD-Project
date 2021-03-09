package nl.han.asd.submarine;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.exception.SocketConnectionException;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendMessageImpl implements SendMessage {
    private static final Logger LOG = Logger.getLogger(SendMessageImpl.class.getName());

    private final SocketFactory socketFactory;

    public SendMessageImpl() {
        this.socketFactory = SocketFactory.getDefault();
    }

    // For testing purposes only.
    public SendMessageImpl(@NonNull SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    public void sendMessage(Onion onion) {
        try (Socket socket = this.connectWithDestination((DestinationRelay) onion.getDestination())) {
            ObjectMapper mapper = new ObjectMapper();
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream())) {
                String contentToSend = mapper.writeValueAsString(onion);
                outputStreamWriter.write(contentToSend.length() + contentToSend);
                outputStreamWriter.flush();
            }
        } catch (IOException e) {
            LOG.log(
                    Level.SEVERE,
                    "An exception was thrown while sending the message. The socket will be closed.",
                    e
            );
        }
    }

    @Override
    public Socket connectWithDestination(DestinationRelay destination) {
        try {
            return socketFactory.createSocket(destination.getHostname(), destination.getPort());
        } catch (IOException e) {
            throw new SocketConnectionException("Could not connect to entry node.");
        }
    }
}
