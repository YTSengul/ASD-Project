package nl.han.asd.submarine.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.repositories.ChatterRepository;
import nl.han.asd.submarine.services.ChatterAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Tag("integration-test")
@SpringBootTest
@AutoConfigureMockMvc
class LoginChatterIT {

    private final static String USERNAME = "TEST_USERNAME_1";
    private final static String PASSWORD = "TEST_PASSWORD_1";
    private final static String IPV4_ADDRESS = "165.0.2.245";
    private final static String IPV6_ADDRESS = "1200:0000:AB00:1234:0000:2552:7777:1313";
    private final static String PUBLIC_KEY = "TEST_PUBLIC_KEY";
    private final static String PASSWORD_HASH = "$2a$10$vPZ3EKgL8zt4M2TrlcptA.CYzDD9DzGlgk1U8y9Dh/sgWd3Kh8UmC";
    private final static String PASSWORD_SALT = "$2a$10$vPZ3EKgL8zt4M2TrlcptA.";

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ChatterRepository chatterRepository;

    private MockHttpServletRequestBuilder mockRequest;
    private Chatter storedChatter;
    private ChatterRegistrationDTO chatterRegistrationDTO;

    @Mock
    private ChatterAuthenticationService chatterAuthenticationServiceMock;

    @BeforeEach
    void setup() {
        mockRequest = put("/chatter/login").contentType(MediaType.APPLICATION_JSON);
        storedChatter = new Chatter();
        storedChatter.setUsername(USERNAME);
        storedChatter.setPasswordHash(PASSWORD_HASH);
        storedChatter.setPasswordSalt(PASSWORD_SALT);
        storedChatter.setIpAddress("8.8.8.8");
        storedChatter.setPublicKey(PUBLIC_KEY);

        chatterRegistrationDTO = new ChatterRegistrationDTO();
        chatterRegistrationDTO.setIpAddress(IPV4_ADDRESS);
        chatterRegistrationDTO.setUsername(USERNAME);
        chatterRegistrationDTO.setPassword(PASSWORD);
        chatterRegistrationDTO.setPublicKey(PUBLIC_KEY);
    }

    @ParameterizedTest
    @ValueSource(strings = {IPV4_ADDRESS, IPV6_ADDRESS})
    void testSuccessfulFlowEndpointToDatabase(String ipAddres) throws Exception {
        // Arrange
        when(chatterRepository.findDistinctByUsername(any())).thenReturn(storedChatter);

        chatterRegistrationDTO.setIpAddress(ipAddres);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(200); // Ok
        verify(chatterRepository, times(1)).findDistinctByUsername(any());
        ArgumentCaptor<Chatter> saveArgumentCaptor = ArgumentCaptor.forClass(Chatter.class);
        verify(chatterRepository, times(1)).save(saveArgumentCaptor.capture());

        assertThat(saveArgumentCaptor.getValue().getIpAddress()).isEqualTo(ipAddres);
    }

    @Test
    void userDoesntExistReturnsForbidden() throws Exception {
        // Arrange
        when(chatterRepository.findDistinctByUsername(any())).thenReturn(null);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(403); // forbidden
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Either the given username or the given password was incorrect.");
        verify(chatterRepository, times(1)).findDistinctByUsername(any());
        verify(chatterRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello World",
            "01.10.0.1",
            "127.0.0.1",
            "192.168.0.1",
            "172.16.0.0",
            "172.31.0.0",
            "10.0.0.0",
            "2001:2f:ffff:ffff:ffff:ffff:ffff:gggg",
            "2001:::1"
    })
    void incorrectIpReturnsBadRequest(String ipAdress) throws Exception {
        // Arrange
        when(chatterRepository.findDistinctByUsername(any())).thenReturn(storedChatter);

        chatterRegistrationDTO.setIpAddress(ipAdress);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(400); // bad request
        assertThat(result.getResponse().getContentAsString()).isEqualTo("The given IP address is neither a valid IPv4 nor a IPv6 address");
        verify(chatterRepository, times(1)).findDistinctByUsername(any());
        verify(chatterRepository, times(0)).save(any());
    }

    @Test
    void testSuccessfulFlowEndpointWithSameIp() throws Exception {
        // Arrange
        storedChatter.setIpAddress(IPV4_ADDRESS);
        when(chatterRepository.findDistinctByUsername(any())).thenReturn(storedChatter);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(200); // Ok
        verify(chatterRepository, times(1)).findDistinctByUsername(any());
        verify(chatterRepository, times(0)).save(any());
    }

    @Test
    void testNotAllFieldsAreSetThrowsIllegalArgumentException() throws Exception {
        // Arrange
        chatterRegistrationDTO.setPassword(null);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(400); // Bad Request
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Not all fields are filled in. Expected:\n" +
                "{\n" +
                "\t\"username\": (String),\n" +
                "\t\"password\": (String),\n" +
                "\t\"ipAddress\": (String)\n" +
                "}");
        verify(chatterRepository, never()).findDistinctByUsername(any());
        verify(chatterRepository, never()).save(any());
    }

    @Test
    void testIncorrectPasswordReturnsForbiddenResponse() throws Exception {
        // Arrange
        when(chatterAuthenticationServiceMock.chatterIsAuthentic(any(), any())).thenReturn(false);

        // Act
        MvcResult result = mvc.perform(mockRequest
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(403); //Forbidden
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Either the given username or the given password was incorrect.");
        verify(chatterRepository, atLeastOnce()).findDistinctByUsername(any());
        verify(chatterRepository, never()).save(any());
    }


}
