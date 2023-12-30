package com.sap.stepbystep.kmf.store.interfac;

import com.sap.smp.client.odata.ODataPayload;

public interface IKMFEntity {

    String getEditResourcePath();

    ODataPayload toODataEntity();
}

