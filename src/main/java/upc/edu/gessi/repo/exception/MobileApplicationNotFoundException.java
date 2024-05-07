package upc.edu.gessi.repo.exception;

public class MobileApplicationNotFoundException extends ObjectNotFoundException{
    public MobileApplicationNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
