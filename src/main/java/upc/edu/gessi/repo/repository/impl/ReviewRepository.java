package upc.edu.gessi.repo.repository.impl;

import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;

import java.util.List;

@Repository
public class ReviewRepository implements RdfRepository {
    @Override
    public List<ReviewDTO> findAll() {
        return null;
    }


    @Override
    public List findAllPaginated(final Integer page, final Integer size) throws MobileApplicationNotFoundException {
        return null;
    }

    public List findAllReviewIDs(){
        return null;
    }

    public List findByApplicationName(final String appName) throws MobileApplicationNotFoundException {
        return null;
    }

}
