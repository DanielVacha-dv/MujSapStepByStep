package com.sap.stepbystep.kmf.store.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.smp.client.httpc.HttpConversationManager;
import com.sap.smp.client.httpc.authflows.CommonAuthFlowsConfigurator;
import com.sap.smp.client.httpc.authflows.UsernamePasswordProvider;
import com.sap.smp.client.httpc.authflows.UsernamePasswordToken;
import com.sap.smp.client.httpc.events.IReceiveEvent;
import com.sap.smp.client.httpc.events.ISendEvent;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataEntitySet;
import com.sap.smp.client.odata.ODataError;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.exception.ODataContractViolationException;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.exception.ODataNetworkException;
import com.sap.smp.client.odata.exception.ODataParserException;
import com.sap.smp.client.odata.impl.ODataEntityDefaultImpl;
import com.sap.smp.client.odata.impl.ODataEntitySetDefaultImpl;
import com.sap.smp.client.odata.impl.ODataUploadMediaDefaultImpl;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.odata.store.ODataRequestChangeSet;
import com.sap.smp.client.odata.store.ODataRequestParamBatch;
import com.sap.smp.client.odata.store.ODataRequestParamSingle;
import com.sap.smp.client.odata.store.ODataResponse;
import com.sap.smp.client.odata.store.ODataResponseBatch;
import com.sap.smp.client.odata.store.ODataResponseBatchItem;
import com.sap.smp.client.odata.store.ODataResponseSingle;
import com.sap.smp.client.odata.store.impl.ODataRequestChangeSetDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataRequestParamBatchDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataRequestParamSingleDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataResponseBatchDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataResponseChangeSetDefaultImpl;
import com.sap.smp.client.odata.store.impl.ODataResponseSingleDefaultImpl;
import com.sap.stepbystep.kmf.store.KMFCredentialsProviderGW;
import com.sap.stepbystep.kmf.store.KMFStreamHandlersBuffer;
import com.sap.stepbystep.kmf.store.KMFXCSRFTokenRequestFilter;
import com.sap.stepbystep.kmf.store.KMFXCSRFTokenResponseFilter;
import com.sap.stepbystep.kmf.store.enu.KMFFormat;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.error.KMFStreamHandlerException;
import com.sap.stepbystep.kmf.store.interfac.IKMFEntity;
import com.sap.stepbystep.kmf.store.interfac.IKMFResponseHandler;
import com.sap.stepbystep.kmf.store.interfac.IKMFStreamHandler;
import com.sap.stepbystep.kmf.store.listener.KMFOnlineStoreOpenListener;
import com.sap.stepbystep.kmf.store.listener.KMFStoreRequestListener;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sap.stepbystep.MainActivity.*;

public class KMFOnlineManager extends KMFManager {
    public static final String TAG = KMFOnlineManager.class.getSimpleName() + ":";
    private static int queue = 0;

    private static boolean directGW = false;
    private static boolean json = false;
    private static URL gwUrl;
    private static String cacheEncryptionKey;
    private static ODataRequestParamBatch requestParamBatch = null;
    private static ODataRequestChangeSet requestChangeSet = null;
    private static KMFStreamHandlersBuffer streamHandlersBuffer;

    public static OnlineODataStore getStore() {
        return KMFOnlineStoreOpenListener.getInstance().getStore();
    }

    public static boolean isJson() {
        return KMFOnlineManager.json;
    }

    public static void setJson(boolean json) {
        KMFOnlineManager.json = json;
    }

    public static boolean isDirectGW() {
        return KMFOnlineManager.directGW;
    }

    public static void setDirectGW(boolean directGW) {
        KMFOnlineManager.directGW = directGW;
    }

    public static void riseQueue() {
        KMFOnlineManager.queue++;
    }

    public static void dropQueue() {
        KMFOnlineManager.queue--;
    }

    public static boolean isQueueEmpty() {
        return KMFOnlineManager.queue == 0;
    }

    public static void initDirectGW(String username, String password, String url) throws MalformedURLException {
        KMFOnlineManager.initDirectGW(username, password, new URL(url));
    }

    public static void initDirectGW(String username, String password, URL url) {
        KMFCredentialsProviderGW.getInstance(username, password);
        KMFOnlineManager.setDirectGW(true);
        KMFOnlineManager.gwUrl = url;
    }

    public static void initTechnicalCache(String cacheEncryptionKey) {
        KMFOnlineManager.cacheEncryptionKey = cacheEncryptionKey;
    }

    public static boolean openOnlineStore(Context context) throws KMFOnlineODataStoreException {
        return openOnlineStoreAsync(context, null);
    }

    public static boolean openOnlineStoreAsync(Context context, OnlineODataStore.OpenListener listener) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();

        if (onlineStoreOpenListener.getStore() == null) {
            KMFOnlineManager.streamHandlersBuffer = new KMFStreamHandlersBuffer();

            LogonCoreContext lgContext = LogonCore.getInstance().getLogonContext();

            UsernamePasswordProvider credentialsProvider = new UsernamePasswordProvider() {
                @Override
                public Object onCredentialsNeededUpfront(ISendEvent iSendEvent) {
                    return new UsernamePasswordToken(USER, PASS);
                }

                @Override
                public Object onCredentialsNeededForChallenge(IReceiveEvent iReceiveEvent) {
                    return new UsernamePasswordToken(USER, PASS);
                }
            };

            HttpConversationManager manager = new CommonAuthFlowsConfigurator(context)
                    .supportBasicAuthUsing(credentialsProvider)
                    .configure(new HttpConversationManager(context));

            KMFXCSRFTokenRequestFilter requestFilter = KMFXCSRFTokenRequestFilter.getInstance(lgContext);
            KMFXCSRFTokenResponseFilter responseFilter = KMFXCSRFTokenResponseFilter.getInstance(context, requestFilter);
            responseFilter.setStreamHandlersBuffer(KMFOnlineManager.streamHandlersBuffer);
            manager.addFilter(requestFilter);
            manager.addFilter(responseFilter);

            try {
                URL url = new URL(
                        SERVICE_PROTOCOL +
                                "://" +
                                SERVER +
                                ":" +
                                PORT +
                                SERVICE
                );
                Log.d(TAG, "Opening URL: " + url);

                OnlineODataStore.OnlineStoreOptions options = new OnlineODataStore.OnlineStoreOptions();
                if (KMFOnlineManager.isJson()) {
                    options.format = OnlineODataStore.PayloadFormatEnum.JSON;
                } else {
                    options.format = OnlineODataStore.PayloadFormatEnum.XML;
                }
                options.useCache = true;
                OnlineODataStore.resetCache(context, url);
                if (listener == null) {
                    OnlineODataStore.open(context, url, manager, onlineStoreOpenListener, options);
                    onlineStoreOpenListener.waitForCompletion();
                    if (onlineStoreOpenListener.getError() != null) {
                        throw onlineStoreOpenListener.getError();
                    }
                } else {
                    OnlineODataStore.open(context, url, manager, listener, options);
                }
            } catch (Exception e) {
                throw new KMFOnlineODataStoreException(e);
            }

            OnlineODataStore store = onlineStoreOpenListener.getStore();
            return store != null;
        } else {
            return true;
        }
    }

    public static List<ODataEntity> getEntities(String collection) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.getEntities(collection, null);
    }

    public static List<ODataEntity> getEntities(String collection, String parameters) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.getEntities(collection, null, null);
    }

    public static List<ODataEntity> getEntities(String collection, String parameters, Map<String, String> options) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();
        List<ODataEntity> entities = new ArrayList<>();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG + "STORE", "OnlineODataStore is null (closed?).");
            return entities;
        }


        if (KMFOnlineManager.isJson()) {
            if (!TextUtils.isEmpty(parameters)) {
                parameters += "&";
            } else {
                parameters = "";
            }

            parameters += KMFManager.FORMAT + KMFFormat.JSON;
        }
        String query = collection;
        if (!TextUtils.isEmpty(parameters)) {
            query += "?" + parameters;
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", query);

        try {
            ODataResponseSingle response = store.executeReadEntitySet(query, options);
            ODataEntitySet set = (ODataEntitySet) response.getPayload();
            entities = set.getEntities();
        } catch (Exception e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        }

        return entities;
    }

    public static ODataEntity getEntity(String collection) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.getEntity(collection, null);
    }

    public static ODataEntity getEntity(String collection, String parameters) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.getEntity(collection, parameters, null);
    }

    public static ODataEntity getEntity(String collection, String parameters, Map<String, String> options) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();
        ODataEntity entity = null;

        if (store == null) {
            Log.w(KMFOnlineManager.TAG + "STORE", "OnlineODataStore is null (closed?).");
            return entity;
        }

        if (KMFOnlineManager.isJson()) {
            if (!TextUtils.isEmpty(parameters)) {
                parameters += "&";
            } else {
                parameters = "";
            }

            parameters += KMFManager.FORMAT + KMFFormat.JSON;
        }

        String query = collection;
        if (!TextUtils.isEmpty(parameters)) {
            query += "?" + parameters;
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", query);

        try {
            ODataResponseSingle resp = store.executeReadEntity(query, options);
            entity = (ODataEntityDefaultImpl) resp.getPayload();
        } catch (Exception e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        }

        return entity;
    }

    public static void getEntityAsync(String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.getEntityAsync(url, responseHandler, operation, new HashMap<String, String>());
    }

    public static void getEntityAsync(String url, IKMFResponseHandler responseHandler, String operation, Map<String, String> headers) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            ODataRequestParamSingle param = new ODataRequestParamSingleDefaultImpl();
            param.getCustomHeaders().putAll(headers);
            param.setResourcePath(url);
            param.setMode(ODataRequestParamSingle.Mode.Read);

            store.scheduleRequest(param, requestListener);
        } catch (ODataException e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static void updateEntityAsync(ODataEntity entity, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.updateEntityAsync(entity, responseHandler, operation, new HashMap<String, String>());
    }

    public static void updateEntityAsync(ODataEntity entity, IKMFResponseHandler responseHandler, String operation, Map<String, String> headers) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + entity.getResourcePath());

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            ODataRequestParamSingle param = new ODataRequestParamSingleDefaultImpl();
            param.setPayload(entity);
            param.getCustomHeaders().putAll(headers);
            param.setMode(ODataRequestParamSingle.Mode.Update);

            store.scheduleRequest(param, requestListener);
//            store.scheduleUpdateEntity(entity, requestListener, headers);
        } catch (ODataException e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static void createEntityAsync(ODataEntity entity, String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.createEntityAsync(entity, url, responseHandler, operation, new HashMap<String, String>());
    }

    public static void createEntityAsync(ODataEntity entity, String url, IKMFResponseHandler responseHandler, String operation, Map<String, String> mHeaders) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            ODataRequestParamSingle param = new ODataRequestParamSingleDefaultImpl();
            param.getCustomHeaders().putAll(mHeaders);
            param.setResourcePath(url);
            param.setMode(ODataRequestParamSingle.Mode.Create);
            param.setPayload(entity);

            store.scheduleRequest(param, requestListener);

//            store.scheduleCreateEntity(entity, url, requestListener, mHeaders);
        } catch (Exception e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static void deleteEntityAsync(String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.deleteEntityAsync(url, responseHandler, operation, new HashMap<String, String>());
    }

    public static void deleteEntityAsync(String url, IKMFResponseHandler responseHandler, String operation, Map<String, String> headers) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            ODataRequestParamSingle param = new ODataRequestParamSingleDefaultImpl();
            param.getCustomHeaders().putAll(headers);
            param.setResourcePath(url);
            param.setMode(ODataRequestParamSingle.Mode.Delete);

            store.scheduleRequest(param, requestListener);
        } catch (Exception e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static void close() throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store.isOpen()) {
            try {
                store.close();
            } catch (ODataContractViolationException e) {
                Log.e(KMFOnlineManager.TAG, e.getMessage());
                e.printStackTrace();
                throw new KMFOnlineODataStoreException(e);
            }
        }
    }

    public static void resetCache() throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        try {
            if (store.isOpenCache()) {
                store.resetCache();
            }
        } catch (Exception e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static void getEntitiesAsync(String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.getEntitiesAsync(url, responseHandler, operation, new HashMap<String, String>());
    }

    public static void getEntitiesAsync(String url, IKMFResponseHandler responseHandler, String operation, Map<String, String> headers) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            ODataRequestParamSingle param = new ODataRequestParamSingleDefaultImpl();
            param.getCustomHeaders().putAll(headers);
            param.setResourcePath(url);
            param.setMode(ODataRequestParamSingle.Mode.Read);

            store.scheduleRequest(param, requestListener);
        } catch (ODataException e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }

    public static List<ODataEntity> executeFunction(String functionName) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.executeFunction(functionName, null);
    }

    public static List<ODataEntity> executeFunction(String functionName, Map<String, String> parameters) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.executeFunction(functionName, parameters, null);
    }

    public static List<ODataEntity> executeFunction(String functionName, Map<String, String> parameters, Map<String, String> options) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();
        List<ODataEntity> entities = null;

        if (store == null) {
            Log.w(KMFOnlineManager.TAG + "STORE", "OnlineODataStore is null (closed?).");
            return entities;
        }

        StringBuilder stringBuilder = new StringBuilder(functionName);
        String query;

        if (parameters != null && !parameters.isEmpty()) {
            stringBuilder.append("?");
            int i = 0;
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {

                stringBuilder.append(parameter.getKey());
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode("'"));
                stringBuilder.append(URLEncoder.encode(parameter.getValue()));
                stringBuilder.append(URLEncoder.encode("'"));

                i++;
                if (i != parameters.size()) {
                    stringBuilder.append("&");
                }
            }
        }

        if (KMFOnlineManager.isJson()) {
            if (parameters != null && !parameters.isEmpty()) {
                stringBuilder.append("&");
            } else if (parameters == null || parameters.isEmpty()) {
                stringBuilder.append("?");
            }

            stringBuilder.append(KMFManager.FORMAT + KMFFormat.JSON);
        }

        query = stringBuilder.toString();
        Log.d(KMFOnlineManager.TAG + ":QUERY", query);


        try {
            ODataResponseSingle response = store.executeFunction(query, new HashMap<String, String>());
            ODataEntitySet set = (ODataEntitySet) response.getPayload();
            entities = set.getEntities();
        } catch (Exception e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        }

        return entities;
    }

    public static void executeFunctionAsync(String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineManager.executeFunctionAsync(url, responseHandler, operation, null);
    }

    public static void executeFunctionAsync(String url, IKMFResponseHandler responseHandler, String operation, Map<String, String> options) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
        Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

        try {
            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);

            // TODO: 02.07.2019 v případě potřeby doplnit technického uživatele headers

            store.scheduleFunction(url, requestListener, options);
        } catch (ODataException e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }


    public static ODataRequestParamBatch initBatchRequest() {
        KMFOnlineManager.requestParamBatch = new ODataRequestParamBatchDefaultImpl();
        KMFOnlineManager.requestChangeSet = null;
        return KMFOnlineManager.requestParamBatch;
    }

    public static ODataRequestChangeSet addBatchItemCreate(IKMFEntity entity, String customTag) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItemCreate(entity, customTag, false);
    }

    public static ODataRequestChangeSet addBatchItemCreate(IKMFEntity entity, String customTag, boolean createNewChangeSet) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItem(entity, customTag, createNewChangeSet, ODataRequestParamSingle.Mode.Create);
    }

    public static ODataRequestChangeSet addBatchItemUpdate(IKMFEntity entity, String customTag) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItemUpdate(entity, customTag, false);
    }

    public static ODataRequestChangeSet addBatchItemUpdate(IKMFEntity entity, String customTag, boolean createNewChangeSet) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItem(entity, customTag, createNewChangeSet, ODataRequestParamSingle.Mode.Update);
    }

    public static ODataRequestChangeSet addBatchItemDelete(IKMFEntity entity, String customTag) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItemDelete(entity, customTag, false);
    }

    public static ODataRequestChangeSet addBatchItemDelete(IKMFEntity entity, String customTag, boolean createNewChangeSet) throws KMFOnlineODataStoreException {
        return KMFOnlineManager.addBatchItem(entity, customTag, createNewChangeSet, ODataRequestParamSingle.Mode.Delete);
    }

    public static ODataRequestParamSingle addBatchItemRead(final String resourcePath, String customTag) throws KMFOnlineODataStoreException {
        return addBatchItem(resourcePath, customTag, ODataRequestParamSingle.Mode.Read);
    }

    private static ODataRequestParamSingle addBatchItem(String resourcePath, String customTag, ODataRequestParamSingle.Mode mode) throws KMFOnlineODataStoreException {
        ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();

        batchItem.setResourcePath(resourcePath);
        batchItem.setMode(mode);
        batchItem.setCustomTag(customTag);

        try {
            requestParamBatch.add(batchItem);
        } catch (Exception e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        }

        return batchItem;
    }

    private static ODataRequestChangeSet addBatchItem(IKMFEntity entity, String customTag, boolean createNewChangeSet, ODataRequestParamSingle.Mode mode) throws KMFOnlineODataStoreException {
        if (KMFOnlineManager.requestChangeSet == null || createNewChangeSet) {
            KMFOnlineManager.requestChangeSet = new ODataRequestChangeSetDefaultImpl();
            try {
                KMFOnlineManager.requestParamBatch.add(KMFOnlineManager.requestChangeSet);
            } catch (Exception e) {
                Log.e(KMFOnlineManager.TAG, e.getMessage());
                e.printStackTrace();
                throw new KMFOnlineODataStoreException(e);
            }
        }

        ODataRequestParamSingle batchItem = new ODataRequestParamSingleDefaultImpl();
        batchItem.setMode(mode);
        batchItem.setCustomTag(customTag);
        batchItem.setResourcePath(entity.getEditResourcePath());
        batchItem.setPayload(entity.toODataEntity());

        //http headers
        Map<String, String> createHeaders = new HashMap<String, String>();
        if (KMFOnlineManager.isJson()) {
            createHeaders.put("accept", "application/json");
            createHeaders.put("content-type", "application/json");
        } else {
            createHeaders.put("accept", "application/atom+xml");
            createHeaders.put("content-type", "application/atom+xml");
        }
        batchItem.setOptions(createHeaders);

        try {
            KMFOnlineManager.requestChangeSet.add(batchItem);
        } catch (ODataException e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        }

        return KMFOnlineManager.requestChangeSet;
    }


    public static Object readPropertyRaw(String collection, IKMFStreamHandler streamHandler) throws KMFOnlineODataStoreException, KMFStreamHandlerException {
        return KMFOnlineManager.readPropertyRaw(collection, null, streamHandler);
    }

    public static Object readPropertyRaw(String collection, String parameters, IKMFStreamHandler streamHandler) throws KMFOnlineODataStoreException, KMFStreamHandlerException {
        return KMFOnlineManager.readPropertyRaw(collection, parameters, streamHandler, null);
    }

    public static Object readPropertyRaw(String collection, String parameters, IKMFStreamHandler streamHandler, Map<String, String> options) throws KMFOnlineODataStoreException, KMFStreamHandlerException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();
        InputStream stream = null;

        if (store == null) {
            Log.w(KMFOnlineManager.TAG + "STORE", "OnlineODataStore is null (closed?).");
            return stream;
        }

        String query = collection;
        if (!TextUtils.isEmpty(parameters)) {
            query += "?" + parameters;
        }

        Log.d(KMFOnlineManager.TAG + ":QUERY", query);

        String fullPath = store.getBaseUrl().toString();
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        fullPath += query;

        KMFOnlineManager.streamHandlersBuffer.add(fullPath, streamHandler);

        try {
            store.executeReadPropertyRaw(query, options);
        } catch (ODataNetworkException | ODataContractViolationException e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
            throw new KMFOnlineODataStoreException(e);
        } catch (ODataParserException e) {
            Log.e(KMFOnlineManager.TAG, e.getMessage());
            e.printStackTrace();
        }

        if (streamHandler.getException() != null) {
            throw new KMFStreamHandlerException(streamHandler.getException());
        }
        return streamHandler.getResult();
    }

    public static void createDataStreamAsync(byte[] fileContent, String mimeType, Map<String, String> headers, String url, IKMFResponseHandler responseHandler, String operation) throws KMFOnlineODataStoreException {
        KMFOnlineStoreOpenListener onlineStoreOpenListener = KMFOnlineStoreOpenListener.getInstance();
        OnlineODataStore store = onlineStoreOpenListener.getStore();

        if (store == null) {
            Log.w(KMFOnlineManager.TAG, "OnlineODataStore is null (closed?).");
        }

        try {
            ODataRequestParamSingleDefaultImpl param = new ODataRequestParamSingleDefaultImpl();
            ODataPayload payload = new ODataUploadMediaDefaultImpl(fileContent, mimeType);

            param.getCustomHeaders().putAll(headers);
            param.setPayload(payload);
            param.setResourcePath(url);
            param.setMode(ODataRequestParamSingle.Mode.Create);

            Log.d(KMFOnlineManager.TAG + ":QUERY", "Operation: " + operation);
            Log.d(KMFOnlineManager.TAG + ":QUERY", "url: " + store.getBaseUrl().toString() + "/" + url);

            for (Map.Entry<String, String> header :
                    headers.entrySet()) {
                param.getCustomHeaders().put(header.getKey(), header.getValue());
                Log.d(KMFOnlineManager.TAG + ":HEADER", header.getKey() + ": " + header.getValue());
            }

            KMFStoreRequestListener requestListener = new KMFStoreRequestListener(operation, responseHandler);
            store.scheduleRequest(param, requestListener);
        } catch (ODataContractViolationException e) {
            throw new KMFOnlineODataStoreException(e);
        }
    }
}