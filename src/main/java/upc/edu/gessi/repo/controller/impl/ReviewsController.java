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
import upc.edu.gessi.repo.service.ReviewService;
import upc.edu.gessi.repo.service.ServiceFactory;

import java.util.List;

@RestController
public class ReviewsController implements ReviewsAPI {
    private final Logger logger = LoggerFactory.getLogger(ReviewsController.class);
    private final ServiceFactory serviceFactory;

    @Autowired
    public ReviewsController(final ServiceFactory servFact) {
        serviceFactory = servFact;
    }

    @Override
    public void ping() {
    }


    @Override
    public ResponseEntity<List<ReviewDTO>> create(List<ReviewDTO> reviewDTOList) {
        return new ResponseEntity<>(((ReviewService) useService(ReviewService.class)).create(reviewDTOList), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ReviewDTO> get(String id) throws ObjectNotFoundException {
        return new ResponseEntity<>(((ReviewService) useService(ReviewService.class)).get(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getListed(List<String> ids) throws NoObjectFoundException {
        return new ResponseEntity<>(((ReviewService) useService(ReviewService.class)).getListed(ids), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getAllPaginated(Integer page, Integer size)
            throws NoObjectFoundException {
        return new ResponseEntity<>(((ReviewService) useService(ReviewService.class)).getAllPaginated(page, size), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ReviewDTO>> getAll() throws NoObjectFoundException {
        return new ResponseEntity<>(((ReviewService) useService(ReviewService.class)).getAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ReviewDTO> update(ReviewDTO entity) {
        ((ReviewService) useService(ReviewService.class)).update(entity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        ((ReviewService) useService(ReviewService.class)).delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Object useService(Class<?> clazz) {
        return serviceFactory.createService(clazz);
    }

}
