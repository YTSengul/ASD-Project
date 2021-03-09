package nl.han.asd.submarine.encryption;

public class IncorrectDecryptionKeyException extends Exception {
    public IncorrectDecryptionKeyException(String message) {
        super(message);
    }
}
