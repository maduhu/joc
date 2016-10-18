package com.sos.joc.exceptions;


public class UnknownJobSchedulerAgentException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-408";

    public UnknownJobSchedulerAgentException() {
    }

    public UnknownJobSchedulerAgentException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public UnknownJobSchedulerAgentException(JocError error) {
        super(error);
    }

    public UnknownJobSchedulerAgentException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public UnknownJobSchedulerAgentException(JocError error, Throwable cause) {
        super(error, cause);
    }

    public UnknownJobSchedulerAgentException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public UnknownJobSchedulerAgentException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(error, cause, enableSuppression, writableStackTrace);
    }

}