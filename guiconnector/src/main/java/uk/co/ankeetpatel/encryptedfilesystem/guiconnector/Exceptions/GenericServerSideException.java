package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GenericServerSideException extends ResponseStatusException {

    public GenericServerSideException(HttpStatus status, String msg, Throwable e) {
        super(status, msg, e);
    }

}
