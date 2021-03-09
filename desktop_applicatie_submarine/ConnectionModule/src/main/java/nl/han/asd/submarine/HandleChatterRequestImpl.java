package nl.han.asd.submarine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import nl.han.asd.submarine.connection.HandleChatterRequest;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.exception.TimeoutRuntimeException;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandleChatterRequestImpl implements HandleChatterRequest {
    private static final Logger LOG = Logger.getLogger(
            HandleChatterRequestImpl.class.getName());

    @Inject
    private SendMessage sendMessage;

    public Integer handleChatter(Onion onion) {
        try (Socket socket = sendMessage.connectWithDestination((DestinationRelay) onion.getDestination())) {
            ObjectMapper mapper = new ObjectMapper();
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream())) {
                String contentToSend = mapper.writeValueAsString(onion);
                outputStreamWriter.write(contentToSend.length() + contentToSend);
                outputStreamWriter.flush();

                ResponseListener responseListener = new ResponseListener(socket, outputStreamWriter);
                //TODO: PoolSize is how many threads can be created per pool. A pool is a group of threads.
                int poolSize = 3;
                ExecutorService pool = Executors.newFixedThreadPool(poolSize);
                FutureTask<Integer> future = new FutureTask<>(responseListener);
                pool.execute(future);

                return future.get(10, TimeUnit.SECONDS);
            }
        } catch (IOException | InterruptedException | TimeoutRuntimeException | ExecutionException | TimeoutException e) { // NOSONAR
            LOG.log(Level.SEVERE, "Something went wrong while handling a chatter action: {0}", e.getMessage());
        }
        return null;
    }

}
