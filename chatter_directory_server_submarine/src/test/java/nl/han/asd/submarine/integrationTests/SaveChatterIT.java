package nl.han.asd.submarine.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.repositories.ChatterRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@Tag("integration-test")
@SpringBootTest
@AutoConfigureMockMvc
class SaveChatterIT {

    private final static String ALIAS = "TEST_ALIAS";
    private final static String PUBLIC_KEY = "TEST_PUBLIC_KEY";
    private final static String USERNAME = "TEST_USERNAME";
    private final static String PASSWORD = "TEST_PASSWORD";
    private final static String IP_ADDRESS = "TEST_IP";

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ChatterRepository chatterRepository;

    @Test
    void testSuccessfulFlowEndpointToDatabase() throws Exception {
        // Arrange
        when(chatterRepository.existsByAliasOrUsername(any(), any())).thenReturn(false);
        ChatterRegistrationDTO chatterRegistrationDTO = new ChatterRegistrationDTO(ALIAS,
                PUBLIC_KEY,
                USERNAME,
                PASSWORD,
                IP_ADDRESS);

        // Act
        MvcResult result = mvc.perform(post("/chatter/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(201); // created
        verify(chatterRepository, times(1)).existsByAliasOrUsername(any(), any());
        verify(chatterRepository, times(1)).save(any());
    }

    @Test
    void duplicateAliasReturnsBadRequest() throws Exception {
        // Arrange
        when(chatterRepository.existsByAliasOrUsername(any(), any())).thenReturn(true);
        ChatterRegistrationDTO chatterRegistrationDTO = new ChatterRegistrationDTO(ALIAS,
                PUBLIC_KEY,
                USERNAME,
                PASSWORD,
                IP_ADDRESS);

        // Act
        MvcResult result = mvc.perform(post("/chatter/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(400); // bad request
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Alias or username already exist");
        verify(chatterRepository, times(1)).existsByAliasOrUsername(any(), any());
        verify(chatterRepository, times(0)).save(any());
    }

    @Test
    void incompleteChatterDTOReturnsBadRequest() throws Exception {
        // Arrange
        ChatterRegistrationDTO chatterRegistrationDTO = new ChatterRegistrationDTO(ALIAS,
                PUBLIC_KEY,
                null,
                PASSWORD,
                IP_ADDRESS);

        // Act
        MvcResult result = mvc.perform(post("/chatter/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(chatterRegistrationDTO)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(400); // bad request
        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                "Not all fields are filled in. Expected:\n" +
                "{\n" +
                "\t\"username\": (String),\n" +
                "\t\"password\": (String),\n" +
                "\t\"ipAddress\": (String),\n" +
                "\t\"alias\": (String),\n" +
                "\t\"publicKey\": (String)\n" +
                "}");
        verify(chatterRepository, times(0)).existsByAliasOrUsername(any(), any());
        verify(chatterRepository, times(0)).save(any());
    }

}
