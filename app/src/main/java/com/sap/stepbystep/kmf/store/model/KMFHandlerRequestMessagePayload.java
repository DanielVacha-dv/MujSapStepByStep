package com.sap.stepbystep.kmf.store.model;
import com.sap.smp.client.odata.store.ODataRequestExecution;

public class KMFHandlerRequestMessagePayload {
    private Object mMessageObj;
    private ODataRequestExecution mRequestExecution;

    public Object getMessageObj() {
        return mMessageObj;
    }

    public ODataRequestExecution getRequestExecution() {
        return mRequestExecution;
    }

    public KMFHandlerRequestMessagePayload(Object messageObj, ODataRequestExecution requestExecution) {
        this.mMessageObj = messageObj;
        this.mRequestExecution = requestExecution;
    }
}
