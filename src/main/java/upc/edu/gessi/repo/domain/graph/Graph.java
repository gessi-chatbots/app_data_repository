package upc.edu.gessi.repo.domain.graph;

import java.io.Serializable;
import java.util.List;

public class Graph implements Serializable {

    private List<GraphNode> nodes;
    private List<GraphEdge> edges;

    public Graph() {

    }

    public Graph(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }
}
