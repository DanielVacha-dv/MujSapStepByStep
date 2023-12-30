package com.sap.stepbystep.kmf.store.listener;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataError;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.impl.ODataEntitySetDefaultImpl;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.smp.client.odata.store.ODataRequestListener;
import com.sap.smp.client.odata.store.ODataResponse;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.interfac.IKMFResponseHandler;
import com.sap.stepbystep.kmf.store.manager.KMFOnlineManager;
import com.sap.stepbystep.kmf.store.model.KMFHandlerRequestMessagePayload;

import java.util.Map;

public class KMFStoreRequestListener implements ODataRequestListener {
    private static final String TAG = KMFStoreRequestListener.class
            .getSimpleName();

    private final int SUCCESS = 0;
    private final int ERROR = -1;

    private final IKMFResponseHandler mResponseHandler;
    private final String mOperation;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            KMFHandlerRequestMessagePayload obj = (KMFHandlerRequestMessagePayload)msg.obj;
            if (msg.what == SUCCESS) {
                if (obj.getMessageObj() == null) {
                    ODataEntity entity = null;
                    mResponseHandler.onODataRequestSuccess(mOperation, entity, null, obj.getRequestExecution());
                } else if (obj.getMessageObj() instanceof ODataEntity) {
                    ODataEntity entity = (ODataEntity) obj.getMessageObj();
                    mResponseHandler.onODataRequestSuccess(mOperation, entity, null, obj.getRequestExecution());
                } else {
                    ODataEntitySetDefaultImpl entities = (ODataEntitySetDefaultImpl) obj.getMessageObj();
                    mResponseHandler.onODataRequestSuccess(mOperation, entities.getEntities(), null, obj.getRequestExecution());
                }
            } else if (msg.what == ERROR) {
                Exception e = (Exception) obj.getMessageObj();
                mResponseHandler.onODataRequestError(mOperation, e, obj.getRequestExecution());
            }
        }
    };

    public KMFStoreRequestListener(String operation, IKMFResponseHandler responseHandler) {
        super();
        this.mOperation = operation;
        this.mResponseHandler = responseHandler;
    }

    /*****************
     * Methods that implements ODataRequestListener interface
     *****************/

    @Override
    public void requestCacheResponse(ODataRequestExecution request) {
        Log.v(KMFStoreRequestListener.TAG + ":TRACK", "requestCacheResponse");
        if (request != null && request.getResponse() != null) {
            ODataResponseSingle response = (ODataResponseSingle) request.getResponse();

            Map<ODataResponse.Headers, String> headerMap = response.getHeaders();
            String code = headerMap.get(ODataResponse.Headers.Code);
            //TODO: logging code
            ODataPayload payload = ((ODataResponseSingle) request.getResponse()).getPayload();

            if (payload instanceof ODataEntity) {
                ODataEntity entity = (ODataEntity) payload;
                notifySuccessToListener(entity, request);
                return;
            } else if (payload instanceof ODataEntitySetDefaultImpl) {
                ODataEntitySetDefaultImpl entities = (ODataEntitySetDefaultImpl) payload;
                notifySuccessToListener(entities, request);
                return;
            }
        }

        notifySuccessToListener((ODataEntity) null, request);
    }

    @Override
    public void requestFailed(ODataRequestExecution request, ODataException e) {
//        TODO: logging
        if (request != null && request.getResponse() != null) {
            ODataPayload payload = ((ODataResponseSingle) request.getResponse()).getPayload();

            if (payload != null && payload instanceof ODataError) {
                ODataError oError = (ODataError) payload;
                //TODO: logging
                notifyErrorToListener(new KMFOnlineODataStoreException(oError.getMessage()), request);
                return;
            }
        }

        notifyErrorToListener(e, request);
    }

    @Override
    public void requestFinished(ODataRequestExecution request) {
//		TraceLog.scoped(this).d("requestFinished");
//TODO: logging
        KMFOnlineManager.dropQueue();
    }

    @Override
    public void requestServerResponse(ODataRequestExecution request) {
        Log.d(TAG, "requestServerResponse(ODataRequestExecution request)");

        if (request != null && request.getResponse() != null) {
            ODataResponseSingle response = (ODataResponseSingle) request.getResponse();

            Map<ODataResponse.Headers, String> headerMap = response.getHeaders();
            String code = headerMap.get(ODataResponse.Headers.Code);
            //TODO: logging code
            ODataPayload payload = ((ODataResponseSingle) request.getResponse()).getPayload();

            if (payload != null && payload instanceof ODataEntity) {
                ODataEntity entity = (ODataEntity) payload;
                notifySuccessToListener(entity, request);
                return;
            } else if (payload != null && payload instanceof ODataEntitySetDefaultImpl) {
                ODataEntitySetDefaultImpl entities = (ODataEntitySetDefaultImpl) payload;
                notifySuccessToListener(entities, request);
                return;
            }
        }

        notifySuccessToListener((ODataEntity) null, request);
    }

    @Override
    public void requestStarted(ODataRequestExecution request) {
//		TraceLog.scoped(this).d("requestStarted");
//TODO: logging
        KMFOnlineManager.riseQueue();
    }


    /**
     * Notify the OnlineResponseHandler that the request was successful.
     */
    protected void notifySuccessToListener(ODataEntity entity, ODataRequestExecution request) {
        Message msg = mUIHandler.obtainMessage();
        msg.what = SUCCESS;
        msg.obj = new KMFHandlerRequestMessagePayload(entity, request);
        mUIHandler.sendMessage(msg);
    }

    private void notifySuccessToListener(ODataEntitySetDefaultImpl entities, ODataRequestExecution request) {
        Message msg = mUIHandler.obtainMessage();
        msg.what = SUCCESS;
        msg.obj = new KMFHandlerRequestMessagePayload(entities, request);
        mUIHandler.sendMessage(msg);
    }

    /**
     * Notify the OnlineResponseHandler that the request has an error.
     *
     * @param exception an Exception that denotes the error that occurred.
     * @param request request
     */
    protected void notifyErrorToListener(Exception exception, ODataRequestExecution request) {
        Message msg = mUIHandler.obtainMessage();
        msg.what = ERROR;
        msg.obj = new KMFHandlerRequestMessagePayload(exception, request);
        mUIHandler.sendMessage(msg);
//TODO: logging
    }
}

