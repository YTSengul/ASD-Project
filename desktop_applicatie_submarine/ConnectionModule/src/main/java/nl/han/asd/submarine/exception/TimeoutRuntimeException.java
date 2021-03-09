package nl.han.asd.submarine.exception;

public class TimeoutRuntimeException extends RuntimeException {
    public TimeoutRuntimeException() {
        super("Response listener: response timeout of 10 seconds was exceeded");
    }
}
