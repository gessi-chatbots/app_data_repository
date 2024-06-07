package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.Review.ReviewDTO;

import java.util.List;

public interface ReviewService extends CrudService<ReviewDTO> {
    List<ReviewDTO> getBatched(final int batch, final int offset);
}
