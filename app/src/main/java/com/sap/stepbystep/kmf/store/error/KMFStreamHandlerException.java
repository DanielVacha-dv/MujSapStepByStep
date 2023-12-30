package com.sap.stepbystep.kmf.store.error;

public class KMFStreamHandlerException extends Exception {
    public KMFStreamHandlerException() {
    }

    public KMFStreamHandlerException(String detailMessage) {
        super(detailMessage);
    }

    public KMFStreamHandlerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public KMFStreamHandlerException(Throwable throwable) {
        super(throwable);
    }
}
