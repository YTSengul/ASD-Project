package nl.han.asd.submarine;

import nl.han.asd.submarine.exception.InvalidHttpStatusCodeException;
import nl.han.asd.submarine.exception.TimeoutRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponseListenerTest {
    private ResponseListener sut;

    @Mock
    private Socket socketMock;

    private OutputStreamWriter outputStreamWriter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new ResponseListener(socketMock, outputStreamWriter);
    }

    @Test
    void parseByteArrayToStringStatusCodeReturns201() {
        //Arrange
        byte[] array = new byte[]{50,48,49};

        //Act & assert
        assertThat(sut.parseByteArrayToStringStatusCode(array)).isEqualTo(201);
    }

    @Test
    void parseByteArrayToStringStatusCodeReturns400(){
        //Arrange
        byte[] array = new byte[]{52,48,48};

        //Act & assert
        assertThat(sut.parseByteArrayToStringStatusCode(array)).isEqualTo(400);
    }

    @Test
    void parseByteArrayToStringStatusCodeReturns405(){
        //Arrange
        byte[] array = new byte[]{52,48,53};

        //Act & assert
        assertThat(sut.parseByteArrayToStringStatusCode(array)).isEqualTo(405);
    }

    @Test
    void parseByteArrayToStringStatusCodeReturns200(){
        //Arrange
        byte[] array = new byte[]{50,48,48};

        //Act & assert
        assertThat(sut.parseByteArrayToStringStatusCode(array)).isEqualTo(200);
    }

    @Test
    void parseByteArrayToStringStatusCodeReturnsInvalidStatusCode(){
        //Arrange
        byte[] array = new byte[]{54,48,48};

        //Act & assert
        assertThatExceptionOfType(InvalidHttpStatusCodeException.class).isThrownBy( () -> sut.parseByteArrayToStringStatusCode(array));
    }

    @Test
    void parseByteArrayToStringStatusCodeByteArrayIsEmpty(){
        //Arrange
        byte[] array = new byte[]{};

        //Act & assert
        assertThatExceptionOfType(TimeoutRuntimeException.class).isThrownBy( () -> sut.parseByteArrayToStringStatusCode(array));
    }

    @Test
    void getCallableExpectTimeoutException() throws IOException {
        //Arrange
        InputStream inputStreamMock = mock(InputStream.class);

        when(socketMock.getInputStream()).thenReturn(inputStreamMock);
        when(inputStreamMock.readAllBytes()).thenReturn(new byte[]{});

        //Act & assert
        assertThatExceptionOfType(TimeoutRuntimeException.class)
                .isThrownBy( () -> sut.call())
                .withMessage("Response listener: response timeout of 10 seconds was exceeded");
    }

}
