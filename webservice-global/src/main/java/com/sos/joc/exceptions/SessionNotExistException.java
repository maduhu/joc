package com.sos.joc.exceptions;


public class SessionNotExistException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-001";

    public SessionNotExistException() {
    }

    public SessionNotExistException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public SessionNotExistException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public SessionNotExistException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public SessionNotExistException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public SessionNotExistException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public SessionNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public SessionNotExistException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
