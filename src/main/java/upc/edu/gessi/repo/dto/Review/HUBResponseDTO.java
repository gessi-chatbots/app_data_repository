package upc.edu.gessi.repo.dto.Review;

import java.util.List;

public class HUBResponseDTO {
    private List<ReviewDTO> analyzed_reviews;

    public List<ReviewDTO> getAnalyzed_reviews() {
        return analyzed_reviews;
    }

    public void setAnalyzed_reviews(List<ReviewDTO> analyzed_reviews) {
        this.analyzed_reviews = analyzed_reviews;
    }
}
