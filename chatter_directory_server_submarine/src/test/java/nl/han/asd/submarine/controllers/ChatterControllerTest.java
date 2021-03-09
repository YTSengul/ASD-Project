package nl.han.asd.submarine.controllers;

import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.controllers.dto.ChatterAddressDTO;
import nl.han.asd.submarine.controllers.dto.ChatterLoginDTO;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.services.ChatterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatterControllerTest {

    @InjectMocks
    private ChatterController sut;

    @Mock
    private ChatterService chatterServiceMock;

    @Mock
    private ModelMapper modelMapperMock;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createCallsChatterService() {
        // Arrange
        ChatterRegistrationDTO chatterRegistrationDTO = new ChatterRegistrationDTO();
        Chatter chatter = new Chatter();
        when(modelMapperMock.map(chatterRegistrationDTO, Chatter.class)).thenReturn(chatter);

        // Act
        sut.validateAndCreateChatter(chatterRegistrationDTO);

        // Assert
        verify(chatterServiceMock).validateAndCreateChatter(chatterRegistrationDTO);
    }

    @Test
    void loginChatterCallsChatterService() {
        ChatterLoginDTO chatterLoginDTO = new ChatterLoginDTO();
        chatterLoginDTO.setIpAddress("127.0.0.1");
        chatterLoginDTO.setUsername("Testuser");
        chatterLoginDTO.setPassword("foei, plain text wachtwoord");

        Chatter chatter = new Chatter();
        chatter.setIpAddress("127.0.0.1");
        chatter.setUsername("Testuser");
        chatter.setPasswordHash("foei, plain text wachtwoord");

        when(modelMapperMock.map(chatterLoginDTO, Chatter.class)).thenReturn(chatter);

        sut.loginChatter(chatterLoginDTO);

        verify(chatterServiceMock).loginChatter(chatterLoginDTO);
    }

    @Test
    void getIPOfChatterByAlias() {
        // Arrange
        ChatterAddressDTO chatterAddressDTO = new ChatterAddressDTO();
        Chatter chatter = new Chatter();
        when(modelMapperMock.map(chatterAddressDTO, Chatter.class)).thenReturn(chatter);

        // Act
        sut.getChatterIP(chatter.getIpAddress());

        // Assert
        verify(chatterServiceMock).getChatterIpByAlias(chatter.getIpAddress());
    }

}
