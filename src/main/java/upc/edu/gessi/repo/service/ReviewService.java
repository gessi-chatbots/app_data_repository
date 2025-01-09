package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewFeatureResponseDTO;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;

public interface ReviewService extends CrudService<ReviewDTO> {
    List<ReviewDTO> getBatched(final int batch, final int offset);

    List<ReviewDTO> getAllSimplified();

    Integer getReviewCount();

    List<ReviewDTO> getByFeature(String feature);

    List<ReviewFeatureResponseDTO> getByAppIdAndFeatures(String appId, List<String> features) throws NoReviewsFoundException;

}
