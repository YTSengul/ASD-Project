package nl.han.asd.submarine.exception;

public class InvalidHttpStatusCodeException extends RuntimeException {
    public InvalidHttpStatusCodeException() {
        super("This is an invalid HTTP statuscode.");
    }
}
