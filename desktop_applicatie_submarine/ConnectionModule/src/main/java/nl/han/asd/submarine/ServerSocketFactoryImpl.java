package nl.han.asd.submarine;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketFactoryImpl implements ServerSocketFactory {
    @Override
    public ServerSocket createSocket(int portNumber) throws IOException {
        return new ServerSocket(portNumber);
    }
}
