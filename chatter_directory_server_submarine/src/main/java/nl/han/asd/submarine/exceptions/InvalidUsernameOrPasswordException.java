package nl.han.asd.submarine.exceptions;

public class InvalidUsernameOrPasswordException extends RuntimeException {
    public InvalidUsernameOrPasswordException() {
        super("Either the given username or the given password was incorrect.");
    }
}
