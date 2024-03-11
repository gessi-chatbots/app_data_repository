package upc.edu.gessi.repo.repository.impl;

import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.ReviewDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;

import java.util.List;

@Repository
public class ReviewRepository implements RdfRepository {
    @Override
    public List<ReviewDTO> findAll() {
        return null;
    }

    @Override
    public List findAllSimplified() throws ApplicationNotFoundException {
        return null;
    }

    @Override
    public List findAllSimplifiedPaginated(final Integer page, final Integer size) throws ApplicationNotFoundException {
        return null;
    }

    public List findAllReviewIDs(){
        return null;
    }

    public List findByApplicationName(final String appName) throws ApplicationNotFoundException {
        return null;
    }

}
