package upc.edu.gessi.repo.domain;

import java.io.Serializable;
import java.util.List;

public class SimilarityApp implements Serializable {

    private String[] category;
    private String documentID;
    private double score;

    public SimilarityApp(String[] category, String documentID, double score) {
        this.category = category;
        this.documentID = documentID;
        this.score = score;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
