package com.sos.joc.exceptions;


public class JocException extends Exception {

    private static final long serialVersionUID = 1L;
    private JocError error;
    
    public JocException() {
        super();
    }

    public JocException(JocError error) {
        super(error.toString());
        this.error = error;
    }
    
    public JocException(Throwable cause) {
        super(cause);
        this.error = new JocError();
        error.setMessage(cause.getMessage());
    }

    public JocException(JocError error, Throwable cause) {
        super(error.toString(), cause);
        this.error = error;
    }

    public JocException(JocError error, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(error.toString(), cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    public JocError getError() {
        return error;
    }

    public void setError(JocError error) {
        this.error = error;
    }
    
    public static JocError updateJocErrorCode(JocError jocError, String code) {
        if (jocError.getCode() == null) {
            jocError.setCode(code);
        }
        return jocError;
    }
    
    public void setErrorMessage(String message) {
        if (this.error == null) {
            this.error = new JocError(); 
        }
        this.error.setMessage(message);
    }
    
    public void addErrorMetaInfo(String ...metaInfos) {
        if (this.error == null) {
            this.error = new JocError(); 
        }
        this.error.addMetaInfoOnTop(metaInfos);
    }
    
    public void appendErrorMetaInfo(String ...metaInfos) {
        if (this.error == null) {
            this.error = new JocError(); 
        }
        this.error.appendMetaInfo(metaInfos);
    }
    
    public void setErrorMessageAndAddMetaInfo(String message, String ...metaInfos) {
        setErrorMessage(message);
        this.error.addMetaInfoOnTop(metaInfos);
    }

}
