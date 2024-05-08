package upc.edu.gessi.repo.exception.Reviews;

import upc.edu.gessi.repo.exception.NoObjectFoundException;

public class NoReviewsFoundException extends NoObjectFoundException {
    public NoReviewsFoundException(String errorMessage) {
        super(errorMessage);
    }
}
