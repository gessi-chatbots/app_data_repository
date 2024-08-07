package upc.edu.gessi.repo.dto.graph;

import java.io.Serializable;

public class GraphEdge implements Serializable {

    private String from;
    private String to;

    public GraphEdge(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
