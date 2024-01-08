package com.sap.stepbystep.kmf.odata;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sap.mobile.lib.parser.ParserException;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.stepbystep.R;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.data.KMFConfigPreferences;
import com.sap.stepbystep.kmf.data.KMFPreferences;
import com.sap.stepbystep.kmf.helpers.KMFHelperFile;
import com.sap.stepbystep.kmf.helpers.KMFHelperNetworkInfo;
import com.sap.stepbystep.kmf.store.constant.KMFStoreOperation;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.error.KMFStoreRequestTimeOutException;
import com.sap.stepbystep.kmf.store.interfac.IKMFResponseHandler;
import com.sap.stepbystep.kmf.store.manager.KMFOnlineManager;
import com.sap.stepbystep.kmf.store.model.KMFMapper;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class KMFODataRequestBuilder2 extends KMFODataRequestBuilder implements IKMFResponseHandler {
    protected IKMFResponseHandler mResponseHandler;
    KMFConfigPreferences mSettings = KMFApplication.getConfig().getConfigPreferences();
    protected String mOperation;
    private String mStoreOperation;
    private boolean mIsTimeOuted = false;
    private boolean mIsResponded = false;
    private boolean mIsUsedCount = false;
    private List<ODataEntity> mODataEntities = new ArrayList<>();
    Handler mTimeOutHandler = new Handler();
    Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            mIsTimeOuted = true;

            String message = String.format(
                    mContext.getString(R.string.timeout),
                    mOperation,
                    mSettings.getConnectivitySconnTimeOut()
            );
            onODataRequestError(mOperation, new KMFOnlineODataStoreException(message, new KMFStoreRequestTimeOutException(message)), null);
        }
    };


    public KMFODataRequestBuilder2(Context context, IKMFResponseHandler responseHandler, String operation) {
        super(context);
        mResponseHandler = responseHandler;
        mOperation = operation;
    }


    public void execute(String storeOperation) {
        if (!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            super.execute();
            return;
        }

        if (KMFApplication.getConfig().getConfigSettings().getCheckNetworkBeforeMakeRequest()) {
            if (!KMFHelperNetworkInfo.isConnected(mContext)) {
                mResponseHandler.onODataRequestError(
                        mOperation,
                        new KMFOnlineODataStoreException(mContext.getString(R.string.No_connections_are_available)),
                        null
                );
                return;
            }
        }

        try {
            prepareForExecute();
        } catch (Exception e) {
            mResponseHandler.onODataRequestError(mOperation, new KMFOnlineODataStoreException(e), null);
            return;
        }

        mStoreOperation = storeOperation;

        try {
            ODataEntity entity;
            switch (storeOperation) {
                case KMFStoreOperation.GET_ENTITIES_ASYNC:
                    KMFOnlineManager.getEntitiesAsync(this.getRequestURL(), this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.GET_ENTITY_ASYNC:
                    KMFOnlineManager.getEntityAsync(this.getRequestURL(), this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.DELETE_ENTITY_ASYNC:
                    KMFOnlineManager.deleteEntityAsync(this.getRequestURL(), this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.CREATE_ENTITY_ASYNC:
                    entity = KMFMapper.oDataEntry2oDataEntity(mEntry);
                    KMFOnlineManager.createEntityAsync(entity, this.getRequestURL(), this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.UPDATE_ENTITY_ASYNC:
                    entity = KMFMapper.oDataEntry2oDataEntity(mEntry);
                    entity.setResourcePath(this.getRequestURL(), this.getRequestURL());
                    KMFOnlineManager.updateEntityAsync(entity, this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.IMPORT_FUNCTION:
                    KMFOnlineManager.executeFunctionAsync(this.getRequestURL(), this, mOperation, mHeaders);
                    break;
                case KMFStoreOperation.CREATE_DATA_STREAM:
                    FileInputStream fileInputStream = new FileInputStream(mFile);
                    byte fileContent[] = new byte[(int) mFile.length()];
                    fileInputStream.read(fileContent);
                    KMFOnlineManager.createDataStreamAsync(
                            fileContent,
                            KMFHelperFile.getMimeType(mFile),
                            mHeaders,
                            this.getRequestURL(),
                            this,
                            mOperation
                    );
                    break;
            }

            startCountingTimeout();
        } catch (Exception e) {
            mResponseHandler.onODataRequestError(mOperation, new KMFOnlineODataStoreException(e), null);
        }
    }

    private void startCountingTimeout() {
        final int socketTimeout = mSettings.getConnectivitySconnTimeOut();

        mTimeOutHandler.removeCallbacks(mTimeOutRunnable);
        mTimeOutHandler.postDelayed(mTimeOutRunnable, socketTimeout);
    }

    @Override
    public String getRequestURL() {
        if (!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            return super.getRequestURL();
        }

        String url = super.getRequestURL();
        url = url.replace(" ", "%20");

        if (mPagingUse) {
            String pagingParams =
                    KMFODataConstants.SKIP
                            .concat(KMFAppConstants.EQUATE)
                            .concat(String.valueOf(mODataEntities.size()))
                            .concat(KMFAppConstants.AMPERSAND)
                            .concat(KMFODataConstants.TOP)
                            .concat(KMFAppConstants.EQUATE)
                            .concat(String.valueOf(mPagingSize));

            url += (url.contains("?") ? "&" : "?") + pagingParams;
        }

        KMFPreferences preferences = new KMFPreferences();

        if (url.contains("?")) {
            url += "&";
        } else {
            url += "?";
        }

        url += KMFODataConstants.SAP_LANGUAGE + preferences.getLanguage();

        if (url.startsWith("/")) {
            url = url.substring(1);
        }

        if (mIsUsedCount) {
            int i = url.indexOf("?");
            if (i > 0) {
                String urlPath = url.substring(0, i);
                String urlParams = url.substring(i + 1);

                url = urlPath
                        .concat(KMFAppConstants.SLASH)
                        .concat(KMFODataConstants.COUNT);
            } else {
                url = url
                        .concat(KMFAppConstants.SLASH)
                        .concat(KMFODataConstants.COUNT);
            }
        }

        return url;
    }

    public boolean isUsedCount() {
        return mIsUsedCount;
    }

    public void setUsedCount(boolean usedCount) {
        mIsUsedCount = usedCount;
    }

    @Override
    protected byte[] getRequestData() throws ParserException, OutOfMemoryError {
        if (!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            return super.getRequestData();
        }

        return "".getBytes();
    }

    /**
     * Old KMF
     *
     * @param list data
     */
    @Override
    public void onRequestSuccess(List<?> list) {
        if (list == null) {
            onRequestFailure("Return list is null");
            return;
        }

        if (list.size() == 1) {
            mResponseHandler.onODataRequestSuccess(mOperation, (ODataEntity) null, null, null);
        } else {
            mResponseHandler.onODataRequestSuccess(mOperation, (List) null, null, null);
        }
    }

    /**
     * Old KMF
     *
     * @param errorMessage error message
     */
    @Override
    public void onRequestFailure(String errorMessage) {
        mResponseHandler.onODataRequestError(mOperation, new Exception(errorMessage), null);
    }

    /**
     * OnlineStore
     *
     * @param operation
     * @param e
     * @param requestExecution
     */
    @Override
    public void onODataRequestError(String operation, Exception e, ODataRequestExecution requestExecution) {
        if (mIsTimeOuted && mIsResponded) {
            return;
        }

        mIsResponded = true;

        mTimeOutHandler.removeCallbacks(mTimeOutRunnable);

        mResponseHandler.onODataRequestError(operation, e, requestExecution);
    }

    /**
     * OnlineStore
     *
     * @param operation
     * @param entity
     * @param message
     */
    @Override
    public void onODataRequestSuccess(String operation, ODataEntity entity, String message, ODataRequestExecution requestExecution) {
        if (mIsTimeOuted) {
            return;
        }
        mTimeOutHandler.removeCallbacks(mTimeOutRunnable);

        mResponseHandler.onODataRequestSuccess(operation, entity, message, requestExecution);
    }

    /**
     * OnlineStore
     *
     * @param operation
     * @param entities
     * @param message
     * @param requestExecution
     */
    @Override
    public void onODataRequestSuccess(String operation, List<ODataEntity> entities, String message, ODataRequestExecution requestExecution) {
        if (mIsTimeOuted) {
            return;
        }
        if (mUseDeltaToken) {
            ODataPayload payload = ((ODataResponseSingle) requestExecution.getResponse()).getPayload();
//            KMFODataDeltaToken.saveDeltaToken(mURLNoDeltaToken, payload);
        }
        mTimeOutHandler.removeCallbacks(mTimeOutRunnable);
        mODataEntities.addAll(entities);
        if (!mPagingUse || entities.size() < mPagingSize) {
            mResponseHandler.onODataRequestSuccess(operation, mODataEntities, message, requestExecution);
        } else {
            Log.d(TAG, "So far getted: " + mODataEntities.size());
            execute(mStoreOperation);
        }
    }
}

