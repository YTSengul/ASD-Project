package nl.han.asd.submarine;

import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.models.routing.Node;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.Path;
import nl.han.asd.submarine.routing.RouteModule;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RouteModuleImpl implements RouteModule {

    @Inject
    NodeInformation nodeInformation;

    @Inject
    SymmetricEncryption symmetricEncryption;

    public RouteModuleImpl() {
//        nodeInformation = new NodeInformation();
    }

    // this constructor is only for testing purposes.
    public RouteModuleImpl(NodeInformation nodeInformation, SymmetricEncryption symmetricEncryption) {
        this.nodeInformation = nodeInformation;
        this.symmetricEncryption = symmetricEncryption;
    }

    protected Path calculateRoute(int lengthOfPath) {
        List<Node> nodes = getAndValidateNodes(lengthOfPath);
        return generatePath(nodes, lengthOfPath);
    }

    protected List<Path> calculateRoutes(int numberOfRoutes, int lengthOfPath) {
        List<Node> nodes = getAndValidateNodes(lengthOfPath);

        return IntStream.range(0, numberOfRoutes - 1).mapToObj(i -> generatePath(nodes, lengthOfPath)).collect(Collectors.toList());
    }

    private List<Node> getAndValidateNodes(int lengthOfPath) {
        List<Node> nodes = getActiveNodes();
        if (lengthOfPath > nodes.size()) {
            throw new IllegalArgumentException("Required length of path is longer than size of active nodes");
        }
        return nodes;
    }

    private Path generatePath(List<Node> nodes, int lengthOfPath) {
        Path path = new Path();
        List<Node> copyOfNodes = new ArrayList<>(nodes);

        for (int i = 0; i < lengthOfPath; i++) {
            int randomIndex = new Random().nextInt(copyOfNodes.size());
            path.addNodeToPath(copyOfNodes.remove(randomIndex));
        }

        return path;
    }

    @Override
    public Onion makeOnion(String content, String receiverAlias, int lengthOfPath, String encryptedSymmetricKey, boolean isHttpRequest) {
        Path calculatedRoute = calculateRoute(lengthOfPath);
        return symmetricEncryption.encryptOnion(calculatedRoute, receiverAlias, content, encryptedSymmetricKey, isHttpRequest);
    }

    private List<Node> getActiveNodes() {
        // temporarily use the NodeFactory. This should be replaced with the ActiveNodeInformation interface
        // when this is implemented with the NodeDirectoryServer
        return nodeInformation.get();
    }
}
