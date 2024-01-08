package com.sap.stepbystep.kmf.store.error;

public class KMFStoreRequestTimeOutException extends Exception {
    public KMFStoreRequestTimeOutException() {
    }

    public KMFStoreRequestTimeOutException(String detailMessage) {
        super(detailMessage);
    }

    public KMFStoreRequestTimeOutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public KMFStoreRequestTimeOutException(Throwable throwable) {
        super(throwable);
    }
}
