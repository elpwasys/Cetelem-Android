package br.com.wasys.library.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Created by pascke on 05/09/16.
 */
public class AppException extends Throwable {

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(getRootCause(cause));
    }

    public AppException(String message, Throwable cause) {
        super(message, getRootCause(cause));
    }

    private static Throwable getRootCause(Throwable cause) {
        Throwable root = ExceptionUtils.getRootCause(cause);
        if (root != null) {
            return root;
        }
        return cause;
    }
}
