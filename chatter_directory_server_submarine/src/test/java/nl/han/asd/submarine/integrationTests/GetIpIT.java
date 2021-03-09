package nl.han.asd.submarine.integrationTests;

import nl.han.asd.submarine.models.Chatter;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("integration-test")
@SpringBootTest
@AutoConfigureMockMvc
class GetIpIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ChatterRepository chatterRepository;

    @Test
    void testSuccessfulGetIpEndpointToDatabase() throws Exception {
        // Arrange
        Chatter chatter = new Chatter();
        chatter.setAlias("test");
        chatter.setIpAddress("123");

        when(chatterRepository.findIpAddressByAlias(eq("test"))).thenReturn(chatter);

        // Act
        MvcResult result = mvc.perform(get("/chatter/get-ip/{alias}", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"ipAddress\":\"123\"}");
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(chatterRepository, times(1)).findIpAddressByAlias(eq("test"));
    }

    @Test
    void testNotSuccessfulGetIpEndpointToDatabase() throws Exception {
        // Arrange
        Chatter chatter = new Chatter();
        chatter.setAlias("test");

        when(chatterRepository.findIpAddressByAlias(eq("test"))).thenReturn(chatter);

        // Act
        MvcResult result = mvc.perform(get("/chatter/get-ip/{alias}", "sett")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

}
