package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCredentialsException extends ResponseStatusException {

    public InvalidCredentialsException(HttpStatus status, String msg, Throwable e) {
        super(status, msg, e);
    }

}
