package nl.han.asd.submarine;

import nl.han.asd.submarine.message.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MessageListenerTest {

    @InjectMocks
    private MessageListener sut;

    @Mock
    private MessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        sut = new MessageListener();
        sut.setLocalPort(10);
        initMocks(this);
    }

    @Test
    void listenToMessagesSuccessfully() throws IOException, InterruptedException {
        final Socket SOCKET = mock(Socket.class);
        final ServerSocket SERVER_SOCKET = mock(ServerSocket.class);
        when(SERVER_SOCKET.accept()).thenReturn(SOCKET);
        sut.listenForIncomingMessages(SERVER_SOCKET);
        verify(messageHandler).handleIncomingMessage(SOCKET);
    }
}