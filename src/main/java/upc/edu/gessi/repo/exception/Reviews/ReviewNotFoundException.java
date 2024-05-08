package upc.edu.gessi.repo.exception.Reviews;

import upc.edu.gessi.repo.exception.ObjectNotFoundException;

public class ReviewNotFoundException extends ObjectNotFoundException {
    public ReviewNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
