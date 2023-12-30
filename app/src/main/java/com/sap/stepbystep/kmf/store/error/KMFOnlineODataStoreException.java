package com.sap.stepbystep.kmf.store.error;

public class KMFOnlineODataStoreException extends Exception {
    public KMFOnlineODataStoreException() {
    }

    public KMFOnlineODataStoreException(String detailMessage) {
        super(detailMessage);
    }

    public KMFOnlineODataStoreException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public KMFOnlineODataStoreException(Throwable throwable) {
        super(throwable);
    }
}
