package br.com.wasys.library.exception;

import br.com.wasys.library.http.Error;

/**
 * Created by pascke on 05/09/16.
 */
public class EndpointException extends Exception {
    public Error error;
    public EndpointException(Error error) {
        if (error == null) {
            throw new IllegalArgumentException("error is null");
        }
        this.error = error;
    }
    @Override
    public String getMessage() {
        return error.getMessage();
    }
}
