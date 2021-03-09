package nl.han.asd.submarine.exceptions;

public class UserDoesNotExistException extends RuntimeException {

    public UserDoesNotExistException(String msg) {
        super(msg);
    }

}
