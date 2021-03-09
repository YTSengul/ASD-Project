package nl.han.asd.submarine;

import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.models.routing.Node;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.Path;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RouteModuleImplTest {

    @InjectMocks
    private RouteModuleImpl sut;

    private static final String MESSAGE = "Message";
    private final List<Node> nodes = new ArrayList<>();

    @Mock
    private SymmetricEncryption symmetricEncryptionMock;
    @Mock
    private NodeInformation nodeInformation;

    @BeforeEach
    public void setup() {
        sut = new RouteModuleImpl();
        MockitoAnnotations.initMocks(this);

        for (int i = 0; i < 15; i++) {
            nodes.add(new Node(new DestinationRelay("ip" + i, 25010), "key"));
        }
    }

    @Test
    void calculateRouteThrowsExceptionWhenSizeOfActiveNodesIsLargerThanRequiredLength() {
        when(nodeInformation.get()).thenReturn(nodes);

        assertThatThrownBy(() -> sut.calculateRoute(nodes.size() + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Required length of path is longer than size of active nodes");
    }

    @Test
    void calculateRouteGeneratesAPathOfFiveUniqueNodes() {
        when(nodeInformation.get()).thenReturn(nodes);

        final int LENGTH_OF_PATH = 5;
        var result = sut.calculateRoute(LENGTH_OF_PATH);

        var allHostNames = result.getNodes().stream().map(node -> ((DestinationRelay) node.getDestination()).getHostname()).collect(Collectors.toList());
        result.getNodes().forEach(node ->
                assertThat(Collections.frequency(allHostNames, ((DestinationRelay) node.getDestination()).getHostname())).isEqualTo(1)
        );
    }

    @Test
    void calculateRoutesThrowsExceptionWhenSizeOfActiveNodesIsLargerThanRequiredLength() {
        when(nodeInformation.get()).thenReturn(nodes);

        assertThatThrownBy(() -> sut.calculateRoutes(5, nodes.size() + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Required length of path is longer than size of active nodes");
    }

    @Test
    void calculateRoutesGeneratesAPathOfFiveUniqueNodes() {
        when(nodeInformation.get()).thenReturn(nodes);

        final int LENGTH_OF_PATH = 5;
        final int NUMBER_OF_PATHS = 5;
        var result = sut.calculateRoutes(NUMBER_OF_PATHS, LENGTH_OF_PATH);

        result.forEach(path -> {
            var allHostnames = path.getNodes().stream().map(node -> ((DestinationRelay) node.getDestination()).getHostname()).collect(Collectors.toList());
            path.getNodes().forEach(node ->
                    assertThat(Collections.frequency(allHostnames, ((DestinationRelay) node.getDestination()).getHostname())).isEqualTo(1)
            );
        });
    }

    @Test
    void makeMessageOnionReturnsOnion() {
        var onion = mock(Onion.class);
        String mockKey = "testKey";
        when(nodeInformation.get()).thenReturn(nodes);
        when(symmetricEncryptionMock.encryptOnion(any(Path.class), eq("hello_world"), eq(MESSAGE), eq(mockKey), eq(false))).thenReturn(onion);

        assertThat(sut.makeOnion(MESSAGE, "hello_world", 5, mockKey, false)).isEqualTo(onion);

        verify(symmetricEncryptionMock).encryptOnion(any(Path.class), eq("hello_world"), eq(MESSAGE), eq(mockKey), eq(false));
    }

    @Test
    void makeRequestOnionReturnsOnion() {
        var onion = mock(Onion.class);
        String mockKey = "testKey";
        when(nodeInformation.get()).thenReturn(nodes);
        when(symmetricEncryptionMock.encryptOnion(any(Path.class), eq("hello_world"), eq(MESSAGE), eq(mockKey), eq(true))).thenReturn(onion);

        assertThat(sut.makeOnion(MESSAGE, "hello_world", 5, mockKey, true)).isEqualTo(onion);

        verify(symmetricEncryptionMock).encryptOnion(any(Path.class), eq("hello_world"), eq(MESSAGE), eq(mockKey), eq(true));
    }

}
