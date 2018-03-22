package org.rosuda.rengine.rserve.protocol;

public class RConnectionException extends Exception {
    public RConnectionException(String message) {
        super(message);
    }

    public RConnectionException(Throwable cause) {
        super(cause);
    }

    public RConnectionException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
