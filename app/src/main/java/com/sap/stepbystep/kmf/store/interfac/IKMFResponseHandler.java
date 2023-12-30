package com.sap.stepbystep.kmf.store.interfac;

import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.store.ODataRequestExecution;

import java.util.List;

public interface IKMFResponseHandler {
    void onODataRequestError(String operation, Exception e, ODataRequestExecution requestExecution);
    void onODataRequestSuccess(String operation, ODataEntity entity, String message, ODataRequestExecution requestExecution);
    void onODataRequestSuccess(String operation, List<ODataEntity> entities, String message, ODataRequestExecution requestExecution);
}
