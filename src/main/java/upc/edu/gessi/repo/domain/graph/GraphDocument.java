package upc.edu.gessi.repo.domain.graph;

import java.io.Serializable;

public class GraphDocument  extends GraphNode implements Serializable {

    private String text;
    private String disambiguatingDescription;

    public GraphDocument(String nodeId, String text, String disambiguatingDescription) {
        super(nodeId);
        this.text = text;
        this.disambiguatingDescription = disambiguatingDescription;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDisambiguatingDescription() {
        return disambiguatingDescription;
    }

    public void setDisambiguatingDescription(String disambiguatingDescription) {
        this.disambiguatingDescription = disambiguatingDescription;
    }
}
