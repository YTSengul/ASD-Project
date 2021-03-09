package nl.han.asd.submarine.services;

import nl.han.asd.submarine.exceptions.AliasOrUsernameAlreadyExistException;
import nl.han.asd.submarine.exceptions.CouldNotFindUserByAliasException;
import nl.han.asd.submarine.exceptions.InvalidUsernameOrPasswordException;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.controllers.dto.ChatterLoginDTO;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.repositories.ChatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//Suppress SonarLint warnings for the getters' return values which are not used in the test
@SuppressWarnings("ResultOfMethodCallIgnored")
class ChatterServiceImplTest {

    @InjectMocks
    private ChatterServiceImpl sut;

    @Mock
    private ChatterRepository chatterRepositoryMock;

    @Mock
    private ChatterAuthenticationService chatterAuthenticationServiceMock;

    @Mock
    private ModelMapper mapperMock;

    private Chatter chatter;
    private ChatterRegistrationDTO chatterRegistrationDTO;
    private ChatterLoginDTO chatterLoginDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        chatterRegistrationDTO = new ChatterRegistrationDTO();
        chatterRegistrationDTO.setUsername("TEST");
        chatterRegistrationDTO.setAlias("TEST");
        chatterRegistrationDTO.setPassword("TEST");
        chatterRegistrationDTO.setIpAddress("192.1.0.1");
        chatterRegistrationDTO.setPublicKey("347u2htu4ihgjo2j9gtu8hj-2u4ijng");

        chatterLoginDTO = new ChatterLoginDTO();
        chatterLoginDTO.setUsername("TEST");
        chatterLoginDTO.setPassword("TEST");
        chatterLoginDTO.setIpAddress("192.1.0.1");


        chatter = new Chatter();
    }

    @Test
    void validateChatterThrowsIllegalArgumentExceptionWhenFieldIsNull() {
        // Arrange
        chatterRegistrationDTO.setUsername(null);
        when(mapperMock.map(any(), any())).thenReturn(chatter);
        // Act and Assert
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.validateAndCreateChatter(chatterRegistrationDTO))
                .withMessage("Not all fields are filled in. Expected:\n" +
                        "{\n" +
                        "\t\"username\": (String),\n" +
                        "\t\"password\": (String),\n" +
                        "\t\"ipAddress\": (String),\n" +
                        "\t\"alias\": (String),\n" +
                        "\t\"publicKey\": (String)\n" +
                        "}");
    }

    @Test
    void validateExistingAliasOrUsernameThrowsAliasOrUsernameAlreadyExistsException() {
        // Arrange
        chatter = mock(Chatter.class);
        when(chatter.isValid()).thenReturn(true);
        when(chatterRepositoryMock.existsByAliasOrUsername(any(), any())).thenReturn(true);
        when(mapperMock.map(any(), any())).thenReturn(chatter);
        // Act and Assert
        assertThatExceptionOfType(AliasOrUsernameAlreadyExistException.class)
                .isThrownBy(() -> sut.validateAndCreateChatter(chatterRegistrationDTO));
    }

    @Test
    void validateChatterIsOkAndCallsDataseFunction() {
        // Arrange
        chatter = mock(Chatter.class);
        when(chatter.isValid()).thenReturn(true);
        when(chatterRepositoryMock.existsByAliasOrUsername(any(), any())).thenReturn(false);
        when(mapperMock.map(any(), any())).thenReturn(chatter);
        // Act
        sut.validateAndCreateChatter(chatterRegistrationDTO);

        // Assert
        verify(chatterRepositoryMock, times(1)).save(any());
    }

    @Test
    void loginChatterThrowsInvalidUsernameOrPasswordException() {
        // Arrange
        when(chatterRepositoryMock.findDistinctByUsername(any())).thenReturn(null);

        // Act and Assert
        assertThatExceptionOfType(InvalidUsernameOrPasswordException.class)
                .isThrownBy(() -> sut.loginChatter(chatterLoginDTO))
                .withMessage("Either the given username or the given password was incorrect.");
        verify(chatterRepositoryMock, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello World", "01.10.0.1", "127.0.0.1", "192.168.0.1", "172.16.0.0", "172.31.0.0", "10.0.0.0"})
    void loginChatterThrowsIllegalArgumentExceptionWithInvalidIPv4(String ipAddress) {
        // Arrange
        when(chatterRepositoryMock.findDistinctByUsername(any())).thenReturn(chatter);
        when(chatterAuthenticationServiceMock.chatterIsAuthentic(any(), any())).thenReturn(true);
        chatterLoginDTO.setIpAddress(ipAddress);

        // Act and Assert
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.loginChatter(chatterLoginDTO))
                .withMessage("The given IP address is neither a valid IPv4 nor a IPv6 address");
        verify(chatterRepositoryMock, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello World", "::1", "fd"})
    void loginChatterThrowsIllegalArgumentExceptionWithInvalidIPv6(String ipAddress) {
        // Arrange
        when(chatterRepositoryMock.findDistinctByUsername(any())).thenReturn(chatter);
        when(chatterAuthenticationServiceMock.chatterIsAuthentic(any(), any())).thenReturn(true);
        chatterLoginDTO.setIpAddress(ipAddress);

        // Act and Assert
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.loginChatter(chatterLoginDTO))
                .withMessage("The given IP address is neither a valid IPv4 nor a IPv6 address");
        verify(chatterRepositoryMock, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"165.0.2.245", "1200:0000:AB00:1234:0000:2552:7777:1313"})
    void loginChatterIsOkAndCallsDatabase(String ipAddress) {
        // Arrange
        chatterLoginDTO.setIpAddress(ipAddress);
        Chatter storedChatterMock = mock(Chatter.class);
        when(chatterAuthenticationServiceMock.chatterIsAuthentic(any(), any())).thenReturn(true);
        when(chatterRepositoryMock.findDistinctByUsername(any())).thenReturn(storedChatterMock);
        when(storedChatterMock.getIpAddress()).thenReturn("192.0.1.210");
        // Act
        sut.loginChatter(chatterLoginDTO);
        // Assert
        verify(storedChatterMock).setIpAddress(eq(ipAddress));
        verify(storedChatterMock).getIpAddress();
        verify(chatterAuthenticationServiceMock).chatterIsAuthentic(any(), any());
        verify(chatterRepositoryMock).findDistinctByUsername(any());
        verify(chatterRepositoryMock).save(storedChatterMock);
    }

    @Test
    void loginChatterFailsWhenArgumentMissing() {
        // Arrange
        chatterLoginDTO = mock(ChatterLoginDTO.class);
        when(chatterLoginDTO.getUsername()).thenReturn(null);
        when(chatterLoginDTO.getPassword()).thenReturn("eiugfhjeuwh");
        when(chatterLoginDTO.getIpAddress()).thenReturn("129.1294019");

        // Act & Assert
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.loginChatter(chatterLoginDTO))
                .withMessage("Not all fields are filled in. Expected:\n{\n\t\"username\": (String),\n\t\"password\": (String),\n\t\"ipAddress\": (String)\n}");
        verify(chatterLoginDTO).getUsername();
    }

    @Test
    void getIPOfChatterByAliasThrowsException() {
        // Arrange
        chatter = mock(Chatter.class);
        when(chatterRepositoryMock.findIpAddressByAlias(chatter.getAlias())).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> sut.getChatterIpByAlias(chatter.getAlias())).isInstanceOf(CouldNotFindUserByAliasException.class);
    }

    @Test
    void getIPOfChatterByAliasDoesNotThrowException() {
        // Arrange
        chatter = mock(Chatter.class);
        when(chatterRepositoryMock.findIpAddressByAlias(chatter.getAlias())).thenReturn(chatter);

        // Act & Assert
        assertThatCode(() -> sut.getChatterIpByAlias(chatter.getAlias())).doesNotThrowAnyException();
    }

}
