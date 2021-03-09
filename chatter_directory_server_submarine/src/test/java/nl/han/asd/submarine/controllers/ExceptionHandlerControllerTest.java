package nl.han.asd.submarine.controllers;

import nl.han.asd.submarine.exceptions.InvalidUsernameOrPasswordException;
import nl.han.asd.submarine.exceptions.NoSuchMethodRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


class ExceptionHandlerControllerTest {

    private final static String TEST_MESSAGE = "TESTMESSAGE";
    private ExceptionHandlerController sut;

    @BeforeEach
    void setup() {
        sut = new ExceptionHandlerController();
    }

    @Test
    void handleAliasOrUsernameAlreadyExistExceptionReturnsCorrectResponseEntity() {
        // Act
        var actualResult = sut.handleAliasOrUsernameAlreadyExistException();

        // Assert
        assertThat(actualResult).isInstanceOf(ResponseEntity.class);
        assertThat(actualResult.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResult.getBody().toString()).isEqualTo("Alias or username already exist");
    }

    @Test
    void handleCouldNotFindUserByAliasExceptionReturnsCorrectResponseEntity() {
        // Act
        var actualResult = sut.handleCouldNotFindUserByAliasException();

        // Assert
        assertThat(actualResult).isInstanceOf(ResponseEntity.class);
        assertThat(actualResult.getStatusCode()).isEqualByComparingTo(HttpStatus.NOT_FOUND);
        assertThat(actualResult.getBody().toString()).isEqualTo("Could not find user by alias");
        assertThat(actualResult.getBody().toString()).isEqualTo("Could not find user by alias");
    }

    @Test
    void handleIllegalArgumentExceptionReturnsCorrectResponseEntity() {
        // Arrange
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException(TEST_MESSAGE);

        // Act
        var actualResult = sut.handleIllegalArgumentException(illegalArgumentException);

        // Assert
        assertThat(actualResult).isInstanceOf(ResponseEntity.class);
        assertThat(actualResult.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResult.getBody().toString()).isEqualTo(TEST_MESSAGE);
    }

    @Test
    void handleInvalidUsernameOrPasswordExceptionReturnsCorrectResponseEntity() {
        InvalidUsernameOrPasswordException invalidUsernameOrPasswordException = new InvalidUsernameOrPasswordException();

        // Act
        var actualResult = sut.handleInvalidUsernameOrPasswordException(invalidUsernameOrPasswordException);

        // Assert
        assertThat(actualResult).isInstanceOf(ResponseEntity.class);
        assertThat(actualResult.getStatusCode()).isEqualByComparingTo(HttpStatus.FORBIDDEN);
        assertThat(actualResult.getBody().toString()).isEqualTo("Either the given username or the given password was incorrect.");
    }

    @Test
    void handleNoSuchMethodRuntimeExceptionReturnsCorrectResponseEntity() {
        NoSuchMethodRuntimeException noSuchMethodRuntimeException = new NoSuchMethodRuntimeException("Something went wrong. The developer has been notified", new Exception());

        // Act
        var actualResult = sut.handleNoSuchMethodRuntimeException(noSuchMethodRuntimeException);

        // Assert
        assertThat(actualResult).isInstanceOf(ResponseEntity.class);
        assertThat(actualResult.getStatusCode()).isEqualByComparingTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actualResult.getBody().toString()).isEqualTo("Something went wrong. The developer has been notified");
    }

}
