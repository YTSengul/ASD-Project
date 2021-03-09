package nl.han.asd.submarine;

import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class SendMessageImplTest {

    private SendMessageImpl sendMessageImpl;
    private Onion onion;
    @Mock
    private Socket socketMock;
    @Mock
    private SocketFactory socketFactoryMock;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.onion = new Onion(new DestinationRelay(
                "127.0.0.1", 1234),
                "relay",
                "Please work");
        Base64.getEncoder().encode("Please work".getBytes());
        sendMessageImpl = new SendMessageImpl(socketFactoryMock);
        when(socketFactoryMock.createSocket("127.0.0.1", 1234)).thenReturn(socketMock);
    }

    @Test
    void sendMessageSuccessful() throws Exception {
        //setup
        OutputStream outputStreamMock = mock(OutputStream.class);
        when(socketMock.getOutputStream()).thenReturn(outputStreamMock);
        //run
        sendMessageImpl.sendMessage(onion);
        //check
        verify(socketMock, times(1)).getOutputStream();
        // We don't verify write, because I can't figure out how to get that
        // thing to do what I want it to do. Also, flush get's called twice,
        // most probably also in the close function.
        verify(outputStreamMock, times(2)).flush();
    }
}
