package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnauthorizedException extends ResponseStatusException {

    public UnauthorizedException(HttpStatus status, String msg, Throwable e) {
        super(status, msg, e);
    }

}
