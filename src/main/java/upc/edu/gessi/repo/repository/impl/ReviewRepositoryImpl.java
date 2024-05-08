package upc.edu.gessi.repo.repository.impl;

import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.exception.MobileApplications.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.Reviews.ReviewNotFoundException;
import upc.edu.gessi.repo.repository.ReviewRepository;

import java.util.List;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {
    @Override
    public ReviewDTO findById(String id) throws ReviewNotFoundException {
        return null;
    }

    @Override
    public List<ReviewDTO> findAll() {
        return null;
    }


    @Override
    public List findAllPaginated(final Integer page, final Integer size) throws NoReviewsFoundException {
        return null;
    }

    @Override
    public ReviewDTO insert(ReviewDTO entity) {
        return null;
    }

    @Override
    public ReviewDTO update(ReviewDTO entity) {
        return null;
    }


    @Override
    public void delete(String id) {

    }

    public List findAllReviewIDs(){
        return null;
    }

}
