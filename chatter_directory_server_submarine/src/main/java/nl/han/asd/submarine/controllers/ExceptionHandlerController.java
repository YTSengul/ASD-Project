package nl.han.asd.submarine.controllers;

import nl.han.asd.submarine.exceptions.AliasOrUsernameAlreadyExistException;
import nl.han.asd.submarine.exceptions.CouldNotFindUserByAliasException;
import nl.han.asd.submarine.exceptions.InvalidUsernameOrPasswordException;
import nl.han.asd.submarine.exceptions.NoSuchMethodRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class ExceptionHandlerController {
    private static final Logger LOG = Logger.getLogger(ExceptionHandlerController.class.getName());

    public ExceptionHandlerController() {
        LOG.setLevel(Level.INFO);
    }

    @ExceptionHandler(value = AliasOrUsernameAlreadyExistException.class)
    public ResponseEntity<Object> handleAliasOrUsernameAlreadyExistException() {
        LOG.log(Level.INFO, "AliasOrUsernameAlreadyExistException: Alias or username already exist");
        return new ResponseEntity<>("Alias or username already exist", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOG.log(Level.INFO, "IllegalArgumentException: {0}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidUsernameOrPasswordException.class)
    public ResponseEntity<Object> handleInvalidUsernameOrPasswordException(InvalidUsernameOrPasswordException ex) {
        LOG.log(Level.INFO, "InvalidUsernameOrPasswordException: {0}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = NoSuchMethodRuntimeException.class)
    public ResponseEntity<Object> handleNoSuchMethodRuntimeException(NoSuchMethodRuntimeException ex) {
        LOG.log(Level.INFO, "NoSuchMethodRuntimeException: {0}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = CouldNotFindUserByAliasException.class)
    public ResponseEntity<Object> handleCouldNotFindUserByAliasException() {
        LOG.log(Level.INFO, "CouldNotFindUserByAliasException: Could not find user by alias");
        return new ResponseEntity<>("Could not find user by alias", HttpStatus.NOT_FOUND);
    }

}
