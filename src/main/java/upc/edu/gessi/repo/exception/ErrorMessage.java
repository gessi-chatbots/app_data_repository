package upc.edu.gessi.repo.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class ErrorMessage {
    private final HttpStatus statusCode;
    private final Date timestamp;
    private final String message;

    public ErrorMessage(HttpStatus status,
                        Date timest,
                        String mess) {
        statusCode = status;
        timestamp = timest;
        message = mess;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
