package upc.edu.gessi.repo.exception.MobileApplications;

import upc.edu.gessi.repo.exception.ObjectNotFoundException;

public class MobileApplicationNotFoundException extends ObjectNotFoundException {
    public MobileApplicationNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
