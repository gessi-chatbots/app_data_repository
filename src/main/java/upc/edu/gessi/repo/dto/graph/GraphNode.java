package upc.edu.gessi.repo.dto.graph;

import java.io.Serializable;

public abstract class GraphNode implements Serializable {

    private String nodeId;

    public GraphNode(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
