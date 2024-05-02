package upc.edu.gessi.repo.exception;

public class ApplicationNotFoundException extends ObjectNotFoundException{
    public ApplicationNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
