package nl.han.asd.submarine.exceptions;

@SuppressWarnings("WeakerAccess") //For warnings that mention access can be package-private, when it cannot.
public class UnhandledEncryptionError extends RuntimeException {

    public UnhandledEncryptionError(String message, Throwable cause) {
        super(message, cause);
    }

    public UnhandledEncryptionError(String message) {
        super(message);
    }

}
