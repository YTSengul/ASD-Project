package nl.han.asd.submarine;

import com.google.api.client.http.*;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.gson.Gson;
import nl.han.asd.submarine.models.NodeDTO;
import nl.han.asd.submarine.models.NodesDTO;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodeInformationTest {

    private final String nodeDirectoryServerIp = "127.0.0.1:8081";

    private NodeInformation sut;

    private NodesDTO nodes;

    @BeforeEach
    void setUp() {
        sut = new NodeInformation();
        sut.nodeDirectoryIp = nodeDirectoryServerIp;
        MockitoAnnotations.initMocks(this);

        nodes = new NodesDTO().setNodes(
                List.of(new NodeDTO(new DestinationRelay("localhost", 25020), "oneKey"),
                        new NodeDTO(new DestinationRelay("localhost", 25011), "toRule"),
                        new NodeDTO(new DestinationRelay("localhost", 25012), "themAll")
        ));
    }

    @Test
    void getReturnsAllNodes() throws IOException {
        // Arrange
        sut.transport = getTransportWithContent(new Gson().toJson(nodes));

        // Act
        var result = sut.get();

        // Assert
        String key;
        DestinationRelay destination;
        for (var i=0; i < Math.min(result.size(), nodes.getNodes().size()); i++) {
            key = result.get(i).getKey();
            destination = (DestinationRelay) result.get(i).getDestination();
            assertThat(key).isEqualTo(nodes.getNodes().get(i).getKey());
            assertThat(destination.getHostname()).isEqualTo(nodes.getNodes().get(i).getDestination().getHostname());
            assertThat(destination.getPort()).isEqualTo(nodes.getNodes().get(i).getDestination().getPort());
        }
    }

    @Test
    void getReturnsEmptyList() {
        sut.transport = getTransportWithContent("");

        var result = sut.get();

        assertThat(result.size()).isEqualTo(0);
    }

    /**
     * HttpRequest and HttpResponse are final classes and cant be mocked. According to the googleapis github pages this
     * is the way to create mock requests and responses.
     *
     * @link http://googleapis.github.io/google-http-java-client/unit-testing.html
     */
    private HttpTransport getTransportWithContent(String content) {
        return new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(200);
                        response.setContentType(Json.MEDIA_TYPE);
                        response.setContent(content);
                        return response;
                    }
                };
            }
        };
    }

}
