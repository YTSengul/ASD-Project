package nl.han.asd.submarine.models.routing;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private final List<Node> nodes = new ArrayList<>();

    public List<Node> getNodes() {
        return nodes;
    }

    public void addNodeToPath(Node node) {
        this.nodes.add(node);
    }
}
