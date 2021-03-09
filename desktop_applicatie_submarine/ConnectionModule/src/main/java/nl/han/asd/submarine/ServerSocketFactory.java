package nl.han.asd.submarine;

import java.io.IOException;
import java.net.ServerSocket;

@FunctionalInterface
public interface ServerSocketFactory {
    ServerSocket createSocket(int portNumber) throws IOException;
}
