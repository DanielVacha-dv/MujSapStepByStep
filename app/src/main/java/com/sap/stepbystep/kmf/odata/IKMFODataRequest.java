package com.sap.stepbystep.kmf.odata;

import com.sap.mobile.lib.request.IRequest;
import com.sap.mobile.lib.request.IResponse;

public interface IKMFODataRequest {
    IRequest getRequest();

    void processResponseSuccess(IResponse response);

    void processResponseError(IResponse response, String errorMessage);
}
