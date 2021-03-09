package nl.han.asd.submarine;

import nl.han.asd.submarine.exception.InvalidHttpStatusCodeException;
import nl.han.asd.submarine.exception.TimeoutRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseListener implements Callable<Integer> {
    private static final Logger LOG = Logger.getLogger(
            ResponseListener.class.getName());

    private Socket socket;
    private OutputStreamWriter outputStreamWriter;

    private boolean waitingForResponse;

    public ResponseListener(Socket socket, OutputStreamWriter outputStreamWriter) {
        this.socket = socket;
        this.outputStreamWriter = outputStreamWriter;
        this.waitingForResponse = true;
    }

    @Override
    public Integer call() {
        Instant timeOut = Instant.now().plusSeconds(10);
        while (waitingForResponse && Instant.now().isBefore(timeOut)) {
            try {
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] array = inputStream.readAllBytes();
                Integer statusCode = parseByteArrayToStringStatusCode(array);
                closeConnections();
                return statusCode;
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage());
            }
        }
        throw new TimeoutRuntimeException();
    }

    Integer parseByteArrayToStringStatusCode(byte[] array) {
        if (array.length == 0) throw new TimeoutRuntimeException();

        waitingForResponse = false;
        String statusCodeAsString = new String(array, StandardCharsets.UTF_8);

        int statusCode = Integer.parseInt(statusCodeAsString);

        if (statusCode < 100 || statusCode >= 600) throw new InvalidHttpStatusCodeException();

        return statusCode;
    }

    private void closeConnections() throws IOException {
        if (outputStreamWriter != null) {
            outputStreamWriter.close();
        }
        socket.close();
    }
}
