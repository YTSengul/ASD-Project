package nl.han.asd.submarine.models;

import java.util.ArrayList;
import java.util.List;

public class NodesDTO {
    private List<NodeDTO> nodes;

    public NodesDTO(List<NodeDTO> nodes) {
        this.nodes = nodes;
    }

    public NodesDTO() {
        nodes = new ArrayList<>();
    }

    public List<NodeDTO> getNodes() {
        return nodes;
    }

    public NodesDTO setNodes(List<NodeDTO> nodes) {
        this.nodes = nodes;
        return this;
    }
}
