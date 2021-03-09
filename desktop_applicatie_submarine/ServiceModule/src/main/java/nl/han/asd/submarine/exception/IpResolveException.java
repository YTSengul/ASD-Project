package nl.han.asd.submarine.exception;

public class IpResolveException extends RuntimeException {
    public IpResolveException(String message, Exception e) {
        super(message, e);
    }
}
