package upc.edu.gessi.repo.repository.impl;

import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RDFCRUDRepository;

import java.io.Serializable;
import java.util.List;

@Repository
public class ReviewRepository implements RDFCRUDRepository {
    @Override
    public List<ReviewDTO> findAll() {
        return null;
    }


    @Override
    public List findAllPaginated(final Integer page, final Integer size) throws MobileApplicationNotFoundException {
        return null;
    }

    @Override
    public Serializable insert(Serializable entity) {
        return null;
    }

    @Override
    public Serializable update(Serializable entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    public List findAllReviewIDs(){
        return null;
    }

    public List findByApplicationName(final String appName) throws MobileApplicationNotFoundException {
        return null;
    }

}
