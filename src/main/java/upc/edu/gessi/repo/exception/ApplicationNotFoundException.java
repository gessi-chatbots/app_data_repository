package upc.edu.gessi.repo.exception;

public class ApplicationNotFoundException extends Exception{
    public ApplicationNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
