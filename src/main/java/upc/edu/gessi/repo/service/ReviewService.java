package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorResponseDTO;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;

public interface ReviewService extends CrudService<ReviewDTO> {
    List<ReviewDTO> getBatched(final int batch, final int offset);

    List<ReviewDTO> getAllSimplified();

    Integer getReviewCount();

    List<ReviewDTO> getByFeature(String feature);

    List<ReviewDescriptorResponseDTO> getByDescriptors(
            ReviewDescriptorRequestDTO requestDTO,
            int page,
            int size
    ) throws NoReviewsFoundException;

    Long getReviewCountByDescriptors(ReviewDescriptorRequestDTO requestDTO) throws NoReviewsFoundException;
}
