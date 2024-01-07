package com.sap.stepbystep.smf.repository;

import android.content.Context;
import android.util.Log;

import com.sap.stepbystep.data.constants.SMFStoreOperation;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.odata.KMFODataRequestBuilder2;
import com.sap.stepbystep.kmf.store.constant.KMFStoreOperation;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.interfac.IKMFResponseHandler;
import com.sap.stepbystep.smf.data.entity.SMFCodeList;

public class SMFCodeListRepository extends SMFBaseRepository {
    public static final String TAG = SMFCodeListRepository.class.getCanonicalName();

    public static void read(Context context, IKMFResponseHandler responseHandler) {
        Log.d(TAG, "read(Context context, IKMFResponseHandler responseHandler)");

        try {
            SMFBaseRepository.openOnlineStore(context);
        } catch (KMFOnlineODataStoreException e) {
            responseHandler.onODataRequestError(SMFStoreOperation.SELECT_CODE_LIST, e, null);
            return;
        }

        KMFODataRequestBuilder2 requestBuilder = new KMFODataRequestBuilder2(context, responseHandler, SMFStoreOperation.SELECT_CODE_LIST);
        requestBuilder.prepareReadRequest(SMFCodeList.getEmptyODataEntity2());

        if (KMFApplication.getConfig().getConfigSettings().getUseTechnicalUser()) {
            requestBuilder.addHeader(KMFApplication.getUserData().getRequestHeaders());
        }

        requestBuilder.execute(KMFStoreOperation.GET_ENTITIES_ASYNC);
    }
}
