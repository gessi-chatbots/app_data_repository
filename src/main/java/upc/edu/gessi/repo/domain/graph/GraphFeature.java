package upc.edu.gessi.repo.domain.graph;

import java.io.Serializable;

public class GraphFeature  extends GraphNode implements Serializable {

    private String name;

    public GraphFeature(String nodeId, String name) {
        super(nodeId);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
