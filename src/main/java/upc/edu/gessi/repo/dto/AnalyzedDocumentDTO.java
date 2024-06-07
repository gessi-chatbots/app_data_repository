package upc.edu.gessi.repo.dto;

import java.io.Serializable;
import java.util.List;

public class AnalyzedDocumentDTO implements Serializable {

    private String id;
    private String text;
    private List<String> features;

    public AnalyzedDocumentDTO(String id, List<String> features) {
        this.id = id;
        this.features = features;
    }

    public AnalyzedDocumentDTO(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
