package upc.edu.gessi.repo.exception.MobileApplications;

import upc.edu.gessi.repo.exception.NoObjectFoundException;

public class NoMobileApplicationsFoundException extends NoObjectFoundException {
    public NoMobileApplicationsFoundException(String errorMessage) {
        super(errorMessage);
    }
}
