package upc.edu.gessi.repo.controller.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.controller.ReviewsAPI;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.exception.*;
import upc.edu.gessi.repo.service.impl.ReviewService;

import java.util.List;

@RestController
public class ReviewsController implements ReviewsAPI {
    private final Logger logger = LoggerFactory.getLogger(ReviewsController.class);

    private final ReviewService reviewService;

    @Autowired
    public ReviewsController(final ReviewService reviewSv) {
        reviewService = reviewSv;
    }

    @Override
    public void ping() {
    }


    @Override
    public ResponseEntity<ReviewDTO> update(ReviewDTO entity) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete() {
        return null;
    }


    @Override
    public ResponseEntity<ReviewDTO> create(List<ReviewDTO> entity) {
        return null;
    }

    @Override
    public ResponseEntity<ReviewDTO> get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getListed(List<String> id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getAllPaginated(boolean paginated, Integer page, Integer size, boolean simplified)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return null;
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getAll() {
        return null;
    }


    @Override
    public ResponseEntity<String> insertJSONReviewData(List<ReviewDTO> reviewResponseDTOList) {
        reviewService.addReviews(reviewResponseDTOList);
        return new ResponseEntity<>("Reviews inserted successfully", HttpStatus.CREATED);
    }

}
