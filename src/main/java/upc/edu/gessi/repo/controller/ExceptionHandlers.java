package upc.edu.gessi.repo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.ReviewNotFoundException;


@Component
public class ExceptionHandlers {

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<String> handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFoundException(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoReviewsFoundException.class)
    public ResponseEntity<String> handleNoReviewsFoundException(NoReviewsFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MissingBodyException.class)
    public ResponseEntity<String> handleMissingBodyException(MissingBodyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
