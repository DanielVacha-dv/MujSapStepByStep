package com.sap.stepbystep.kmf.store.error;

public class KMFRegistrationManagerException extends Exception {
    public KMFRegistrationManagerException() {
    }

    public KMFRegistrationManagerException(String detailMessage) {
        super(detailMessage);
    }

    public KMFRegistrationManagerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public KMFRegistrationManagerException(Throwable throwable) {
        super(throwable);
    }
}
