package com.sap.stepbystep.kmf.odata;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sap.mobile.lib.cache.CacheException;
import com.sap.mobile.lib.cache.ICache;
import com.sap.mobile.lib.parser.IODataEntry;
import com.sap.mobile.lib.parser.IODataFeed;
import com.sap.mobile.lib.parser.IODataSchema;
import com.sap.mobile.lib.parser.IODataServiceDocument;
import com.sap.mobile.lib.parser.IODataSubscriptionEntry;
import com.sap.mobile.lib.parser.IParser;
import com.sap.mobile.lib.parser.ODataEntry;
import com.sap.mobile.lib.parser.ODataFeed;
import com.sap.mobile.lib.parser.ParserException;
import com.sap.mobile.lib.request.BaseRequest;
import com.sap.mobile.lib.request.IRequest;
import com.sap.mobile.lib.request.IResponse;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.data.KMFPreferences;
import com.sap.stepbystep.kmf.helpers.KMFHelperNetworkInfo;
import com.sap.stepbystep.kmf.ui.listeners.KMFDialogProgress;

//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class KMFODataRequestBuilder
        implements IKMFODataRequest
        , Handler.Callback {

    protected final static String TAG = KMFODataRequestBuilder.class.getName();
    protected static final int REQUEST_FAILURE = 0;
    protected static final int REQUEST_SUCCESS = 1;
    protected static final int REQUEST_NEXT_PAGE = 2;
    protected static int PAGING_GET_COUNT = -1;
    protected int mPagingCurrentPage = PAGING_GET_COUNT;
    protected Context mContext;
    protected String mService;
    protected String mClient;
    protected String mEndPoint;
    protected boolean mUseJSONFormat;
    protected boolean mUseDeltaToken = true;
    protected String mURLNoDeltaToken;
    protected String mApplicationConnectionId;
    protected boolean mCacheSave = false;
    protected String mCacheCollectionId;
    protected boolean mCacheClear;
    protected Map<String, String> mHeaders = new HashMap<String, String>();
    protected IRequest mRequest;
    protected List<KMFODataRequest> requests = new ArrayList<KMFODataRequest>();
    protected KMFODataEntity mKMFODataEntity;
    protected ODataEntry mEntry;
    protected boolean mPagingUse;
    protected int mPagingSize;
    protected String mPagingBaseURL;
    protected String mPagingURLPath, mPagingURLParams;
    protected List<KMFODataEntity> mPagingResultList;

    protected String mRequestExpand;
    protected String mRequestURL;

    protected KMFDialogProgress mProgressDialog;
    protected String mProgressDialogMessage;
    protected boolean mProgressDialogShow = true;

    protected boolean mIsDataStream = false;
    protected File mFileDirectory;
    protected File mFile;
    protected String mFileName;

    protected Handler mHandler;

    public KMFODataRequestBuilder(Context context) {
        mContext = context;
        initKMFOdataRequestBuilder();
    }

    /**
     * Check whether x-csrf-token is already set/fetched?
     *
     * @return true if headers contains key {@link KMFODataConstants#X_CSRF_TOKEN}
     * and value is not "fetch".
     */
    public static boolean isXcsrfTokenSet() {
        IRequest request = new BaseRequest();
        request.setRequestMethod(IRequest.REQUEST_METHOD_POST);
        if (request.getHeaders().containsKey(KMFODataConstants.X_CSRF_TOKEN)) {
            return ((String) request.getHeaders().get(KMFODataConstants.X_CSRF_TOKEN))
                    .compareToIgnoreCase("Fetch") != 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case REQUEST_SUCCESS:
                onRequestSuccess((List<?>) message.obj);
                break;
            case REQUEST_FAILURE:
                onRequestFailure((String) message.obj);
                break;
            case REQUEST_NEXT_PAGE:
                KMFODataRequestAsync requestAsyncTask = new KMFODataRequestAsync(mContext, this);
                requestAsyncTask.execute();
                break;
        }
        return false;
    }

    /**
     * Set necesery members used in KMF request builed.
     */
    protected void initKMFOdataRequestBuilder() {
        KMFPreferences preferences = new KMFPreferences();
        mService = preferences.getService();
        mClient = preferences.getClient();
        mEndPoint = getEndPoint(preferences);

        if (KMFApplication.isUseSMP()) {
            mApplicationConnectionId = preferences.getApplicationConnectionId();
        }
        mRequest = initRequest(mRequest, mEndPoint);
        mUseJSONFormat = KMFApplication.getConfig().getConfigOData().useJsonFormat();
        mPagingUse = KMFApplication.getConfig().getConfigOData().usePaging();
        mPagingSize = KMFApplication.getConfig().getConfigOData().getPagingSize();
    }

    /**
     * Return end point.
     *
     * @param preferences
     */
    protected String getEndPoint(KMFPreferences preferences) {
        if (KMFApplication.isUseSMP()) {
            return preferences.getProtocol()
                    .concat(KMFAppConstants.COLON)
                    .concat(KMFAppConstants.SLASH)
                    .concat(KMFAppConstants.SLASH)
                    .concat(preferences.getServer())
                    .concat(KMFAppConstants.COLON)
                    .concat(preferences.getPort())
                    .concat(KMFAppConstants.SLASH)
                    .concat(mService);
        } else {
            return preferences.getProtocol()
                    .concat(KMFAppConstants.COLON)
                    .concat(KMFAppConstants.SLASH)
                    .concat(KMFAppConstants.SLASH)
                    .concat(preferences.getServer())
                    .concat(KMFAppConstants.COLON)
                    .concat(preferences.getPort())
                    .concat(mService);
        }
    }

    /**
     * Init request.
     *
     * @param request
     * @param endPoint
     * @return
     */
    protected IRequest initRequest(IRequest request, String endPoint) {
        request = new BaseRequest();
        request.setPriority(IRequest.PRIORITY_HIGH);
        request.setRequestUrl(endPoint);
        return request;
    }

    /**
     * Show progress dialog. Progress dialog is always shown by default.
     *
     * @param show use false to disable progress dialog
     */
    public void showProgressDialog(boolean show) {
        mProgressDialogShow = show;
    }

    /**
     * Show progress dialog.
     *
     * @return false if progress dialog is disabled otherwise true
     */
    public boolean showProgressDialog() {
        return mProgressDialogShow;
    }

    /**
     * Set progress dialog message.
     *
     * @param resources
     */
    public void setProgressDialogMessage(int resources) {
        setProgressDialogMessage(mContext.getString(resources));
    }

    public String getProgressDialogMessage() {
        return mProgressDialogMessage;
    }

    /**
     * Set progress dialog message.
     *
     * @param progressDialogMessage
     */
    public void setProgressDialogMessage(String progressDialogMessage) {
        mProgressDialogMessage = progressDialogMessage;
    }

    public void useJSONFormat(boolean useJSONFormat) {
        mUseJSONFormat = useJSONFormat;
    }

    public boolean isUseJSONFormat() {
        return mUseJSONFormat;
    }

    public boolean isUseDeltaToken() {
        return mUseDeltaToken;
    }

    public void setUseDeltaToken(boolean useDeltaToken) {
        mUseDeltaToken = useDeltaToken;
    }

    /**
     * Set member{@code mCacheSave} of request builder to save data to server cache.
     * Data will be saved only if request successfully returns collection set. Can't be
     * use for function imports returning complex types.
     *
     * @param collectionId <ul>
     *                     <li>name of collection which will be used for saving data to server cache</li>
     *                     <li>if {@code collectionId} is {@code null} entity collection ID will be used({@link KMFODataEntity#getCollectionId()})</li>
     *                     </ul>
     * @param cacheClear   <ul>
     *                     <li>set member {@code mCacheClear}</li>
     *                     <li>if cacheClear is {@code true} server cache will be cleared before saving
     *                     the request data, otherwise data will be merged</li>
     *                     </ul>
     */
    public void setCacheSave(String collectionId, boolean cacheClear) {
        mCacheSave = true;

//        if (collectionId == null)
//            mCacheCollectionId = mKMFODataEntity.getCollectionId();
//        else
        mCacheCollectionId = collectionId;

        mCacheClear = cacheClear;
    }

    public void setPaging(boolean use, int size) {
        mPagingUse = use;
        mPagingSize = size;
    }

    protected void pagingFirstPage(int count) {
        if (count == 0) {
            handleRequestSuccess(new ArrayList<KMFODataEntity>());
            return;
        }

        mPagingResultList = new ArrayList<KMFODataEntity>();
        mPagingCurrentPage = -1; // => next page = 0
        pagingNextPage();
    }

    protected void pagingNextPage() {
        mPagingCurrentPage++;
        if ((mPagingCurrentPage > 0//nejedna se o prvni page
                && mPagingResultList.isEmpty()) //neexistuji zadne polozky
                || mPagingResultList.size() != mPagingSize * mPagingCurrentPage) { //posledni stranka nebyla plne zaplnena
            handleRequestSuccess(mPagingResultList);
            return;
        }

        String pagingParams =
                KMFODataConstants.SKIP
                        .concat(KMFAppConstants.EQUATE)
                        .concat(String.valueOf((mPagingCurrentPage * mPagingSize)))
                        .concat(KMFAppConstants.AMPERSAND)
                        .concat(KMFODataConstants.TOP)
                        .concat(KMFAppConstants.EQUATE)
                        .concat(String.valueOf(mPagingSize));

        String url;
        if (mPagingURLParams == null) {
            url = mPagingURLPath
                    .concat(KMFAppConstants.QUESTION_MARK)
                    .concat(pagingParams);
        } else {
            url = mPagingURLPath
                    .concat(KMFAppConstants.QUESTION_MARK)
                    .concat(mPagingURLParams)
                    .concat(KMFAppConstants.AMPERSAND)
                    .concat(pagingParams);
        }
        mRequest.setRequestUrl(url);

        dismissProgressDialog();

        mHandler.sendMessage(
                mHandler.obtainMessage(
                        REQUEST_NEXT_PAGE
                        , null
                )
        );
    }

    /**
     * Return request URL for service document or meta data.
     *
     * @param requestClass class defining service document or meta data
     */
    protected String getRequestURL(Class requestClass) {
        String requestURL = new String("");
        if (requestClass.getSimpleName().equals(IODataServiceDocument.class.getSimpleName())) {
            requestURL = requestURL.concat(KMFAppConstants.SLASH);
        } else if (requestClass.getSimpleName().equals(IODataSchema.class.getSimpleName())) {
            requestURL = requestURL.concat(KMFAppConstants.SLASH).concat(KMFODataConstants.METADATA);
        } else if (requestClass.getSimpleName().equals(IODataSubscriptionEntry.class.getSimpleName())) {
            requestURL = requestURL.concat(KMFAppConstants.SLASH).concat(KMFODataConstants.SUBSCRIPTION);
        }

        return requestURL;
    }

    /**
     * Return request URL.
     *
     * @return request URL.
     */
    public String getRequestURL() {
        if (mRequestURL != null) {
            return mRequestURL;
        }

        String requestURL = new String("");
        for (KMFODataRequest request : requests) {
            requestURL = requestURL.concat(request.getRequestURL());
        }

        if (mRequestExpand != null && !mRequestExpand.isEmpty()) {
            if (requestURL.contains(KMFAppConstants.QUESTION_MARK)) {
                requestURL = requestURL.concat(KMFAppConstants.AMPERSAND)
                        .concat(KMFODataConstants.EXPAND)
                        .concat(mRequestExpand);
            } else {
                requestURL = requestURL.concat(KMFAppConstants.QUESTION_MARK)
                        .concat(KMFODataConstants.EXPAND)
                        .concat(mRequestExpand);
            }
        }

        if (mUseJSONFormat && mRequest.getRequestMethod() == IRequest.REQUEST_METHOD_GET) {
            if (requestURL.contains(KMFAppConstants.QUESTION_MARK)) {
                requestURL = requestURL.concat(KMFAppConstants.AMPERSAND).concat(KMFODataConstants.JSON_FORMAT);
            } else {
                requestURL = requestURL.concat(KMFAppConstants.QUESTION_MARK).concat(KMFODataConstants.JSON_FORMAT);
            }
        }

        if (mIsDataStream) {
            requestURL = requestURL.concat(KMFAppConstants.SLASH).concat(KMFODataConstants.VALUE);
        }

        if (mUseDeltaToken && !mPagingUse && mRequest.getRequestMethod() == IRequest.REQUEST_METHOD_GET) {
            mURLNoDeltaToken = requestURL;
            String deltaToken = KMFODataDeltaToken.getDeltaToken(mURLNoDeltaToken);
            if (!TextUtils.isEmpty(deltaToken)) {
                if (requestURL.contains(KMFAppConstants.QUESTION_MARK)) {
                    requestURL = requestURL
                            .concat(KMFAppConstants.AMPERSAND)
                            .concat(KMFAppConstants.EXCLAMATION_MARK)
                            .concat(KMFODataConstants.DELTATOKEN)
                            .concat(KMFAppConstants.SINGLE_QUOTATION_MARK)
                            .concat(deltaToken)
                            .concat(KMFAppConstants.SINGLE_QUOTATION_MARK);
                } else {
                    requestURL = requestURL
                            .concat(KMFAppConstants.QUESTION_MARK)
                            .concat(KMFAppConstants.EXCLAMATION_MARK)
                            .concat(KMFODataConstants.DELTATOKEN)
                            .concat(KMFAppConstants.SINGLE_QUOTATION_MARK)
                            .concat(deltaToken)
                            .concat(KMFAppConstants.SINGLE_QUOTATION_MARK);
                }
            }
        }

        //TODO: escapeovat mezery v requestu. Vyreseno v KMFODataRequestBuilder2.getRequestURL();

        return getRequestURLWithSAPClient(requestURL);
    }

    /**
     * Set request URL.
     *
     * @param requestURL
     */
    public void setRequestURL(String requestURL) {
        if (requestURL == null) {
            return;
        }

        if (requestURL.contains(mService)) {
            mRequestURL = requestURL.substring(
                    requestURL.lastIndexOf(mService) + mService.length()
                    , requestURL.length()
            );
        } else {
            mRequestURL = requestURL;
        }
    }

    /**
     * Return request URL with SAP client.
     *
     * @return request url containing SAP client if is set
     */
    public String getRequestURLWithSAPClient(String requestURL) {
        //TODO: lze resit pres hlavicku requestu, kde klic je SAP-CLIENT
        if (mClient == null) {
            return requestURL;
        }

        if (requestURL.contains(KMFAppConstants.QUESTION_MARK)) {
            return requestURL.concat(KMFAppConstants.AMPERSAND).concat(KMFODataConstants.SAP_CLIENT).concat(mClient);
        } else {
            return requestURL.concat(KMFAppConstants.QUESTION_MARK).concat(KMFODataConstants.SAP_CLIENT).concat(mClient);
        }
    }

    protected byte[] getRequestData() throws ParserException, OutOfMemoryError {
        String body = new String("");
        if (mUseJSONFormat)
        // TODO - v ramci deep insertu dochazi k chybe
        {
            body = KMFApplication.getParser().buildODataEntryRequestBody(
                    mEntry
                    , mKMFODataEntity.getCollectionId()
                    , KMFApplication.getMetaDocument()
                    , IParser.FORMAT_JSON
            );
        } else {
            body = KMFApplication.getParser().buildODataEntryRequestBody(
                    mEntry
                    , mKMFODataEntity.getCollectionId()
                    , KMFApplication.getMetaDocument()
                    , IParser.FORMAT_XML
            );
        }

        return body.getBytes();
    }

    /**
     *
     * @param method
     * @param entity
     * @param requestKeys
     * @param requestFilter
     */
    protected void setRequest(int method, KMFODataEntity entity, KMFODataRequestKeys requestKeys, KMFODataRequestFilter requestFilter) {
        mRequest.setRequestMethod(method);
        requests.add(new KMFODataRequest(entity, requestKeys, requestFilter));
    }

    /**
     * Set:
     * <ul>
     * <li>request method,</li>
     * <li>request tag,</li>
     * <li>request URL,</li>
     * </ul>
     * for service document.
     */
    public void prepareServiceDocumentRequest() {
        mRequest.setRequestMethod(IRequest.REQUEST_METHOD_GET);
        mRequest.setRequestTAG(IODataServiceDocument.class.getSimpleName());
        mRequest.setRequestUrl(mRequest.getRequestUrl().concat(getRequestURLWithSAPClient(getRequestURL(IODataServiceDocument.class))));
    }

    /**
     * Set:
     * <ul>
     * <li>request method,</li>
     * <li>request tag,</li>
     * <li>request URL,</li>
     * </ul>
     * for meta data.
     */
    public void prepareMetadataRequest() {
        mRequest.setRequestMethod(IRequest.REQUEST_METHOD_GET);
        mRequest.setRequestTAG(IODataSchema.class.getSimpleName());
        mRequest.setRequestUrl(mRequest.getRequestUrl().concat(getRequestURLWithSAPClient(getRequestURL(IODataSchema.class))));
    }

    /**
     * Set parameters for READ request.
     *
     * @param entity entity to be queried
     */
    public void prepareReadRequest(KMFODataEntity entity) {
        setRequest(IRequest.REQUEST_METHOD_GET, entity, null, null);
    }

    /**
     * Set parameters for READ request using keys.
     *
     * @param entity entity to be queried
     * @param keys   entity keys
     */
    public void prepareReadRequest(KMFODataEntity entity, KMFODataRequestKeys keys) {
        setRequest(IRequest.REQUEST_METHOD_GET, entity, keys, null);
    }

    /**
     * Set parameters for READ request using filters.
     *
     * @param entity entity to be queried
     * @param filter query filter, may be null
     */
    public void prepareReadRequest(KMFODataEntity entity, KMFODataRequestFilter filter) {
        setRequest(IRequest.REQUEST_METHOD_GET, entity, null, filter);
    }

    /**
     * Set parameters for READ request using expand.
     *
     * @param expand expand string
     */
    public void prepareReadRequest(String expand) {
        mRequestExpand = expand;
    }

    /**
     * Set parameters for READ data stream request using keys.
     *
     * @param entity        entity to be queried
     * @param fileDirectory file directory
     */
    public void prepareReadDataStreamRequest(KMFODataEntity entity, File fileDirectory) {
        prepareReadDataStreamRequest(entity, fileDirectory, null);
    }

    /**
     * Set parameters for READ data stream request using keys.
     *
     * @param entity        entity to be queried
     * @param fileDirectory file directory
     * @param fileName      file name
     */
    public void prepareReadDataStreamRequest(KMFODataEntity entity, File fileDirectory, String fileName) {
        prepareReadDataStreamRequest(
                entity
                , new KMFODataRequestKeys(entity)
                , fileDirectory
                , fileName
        );
    }

    /**
     * Set parameters for READ data stream request using keys.
     *
     * @param entity        entity to be queried
     * @param keys          entity keys
     * @param fileDirectory file directory
     * @param fileName      file name
     */
    public void prepareReadDataStreamRequest(KMFODataEntity entity, KMFODataRequestKeys keys, File fileDirectory, String fileName) {
        mIsDataStream = true;
        setRequest(IRequest.REQUEST_METHOD_GET, entity, keys, null);
        mFileDirectory = fileDirectory;
        mFileName = fileName;
    }

    /**
     * Set parameters for CREATE request.
     *
     * @param entity Entity to be created.
     */
    public void prepareCreateRequest(KMFODataEntity entity) {
        setRequest(IRequest.REQUEST_METHOD_POST, entity, null, null);
    }

    public void prepareCreateRequestBody(ODataEntry entry) {
        mEntry = entry;
    }

    /**
     * Set parameters for CREATE data stream request.
     *
     * @param entity entity to be queried
     * @param file   file
     */
    public void prepareCreateDataStreamRequest(KMFODataEntity entity, File file) {
        setRequest(IRequest.REQUEST_METHOD_POST, entity, null, null);
        mFile = file;
    }

    /**
     * Set parameters for UPDATE request.
     *
     * @param entity Entity to be updated.
     */
    public void prepareUpdateRequest(KMFODataEntity entity) {
        setRequest(IRequest.REQUEST_METHOD_PUT, entity, new KMFODataRequestKeys(entity), null);
    }

    /**
     * Set parameters for UPDATE request.
     *
     * @param entity Entity to be updated.
     * @param keys   Query keys, may be null.
     */
    public void prepareUpdateRequest(KMFODataEntity entity, KMFODataRequestKeys keys) {
        setRequest(IRequest.REQUEST_METHOD_PUT, entity, keys, null);
    }

    public void prepareUpdateRequestBody(ODataEntry entry) {
        mEntry = entry;
    }

    /**
     * Set parameters for DELETE request.
     *
     * @param entity Entity to be deleted.
     */
    public void prepareDeleteRequest(KMFODataEntity entity) {
        setRequest(IRequest.REQUEST_METHOD_DELETE, entity, new KMFODataRequestKeys(entity), null);
    }

    /**
     * Set parameters for DELETE request.
     *
     * @param entity Entity to be deleted.
     * @param keys   Query keys, may be null.
     */
    public void prepareDeleteRequest(KMFODataEntity entity, KMFODataRequestKeys keys) {
        setRequest(IRequest.REQUEST_METHOD_DELETE, entity, keys, null);
    }

    /**
     * Set parameters for subscription request.
     *
     * @param subscriptionEntry
     */
    public void prepareSubscriptionRequest(IODataSubscriptionEntry subscriptionEntry) {
        mRequest.setRequestMethod(IRequest.REQUEST_METHOD_POST);
        mRequest.setRequestTAG(IODataSubscriptionEntry.class.getSimpleName());
        mRequest.setRequestUrl(mRequest.getRequestUrl().concat(getRequestURLWithSAPClient(getRequestURL(IODataSubscriptionEntry.class))));
        mHeaders.put(KMFODataConstants.CONTENT_TYPE, KMFODataConstants.APPLICATION_ATOM_XML);
        mRequest.setHeaders(mHeaders);
        mRequest.setData(subscriptionEntry.toXMLString().getBytes());
    }

    /**
     * Add headers into mHeaders.
     *
     * @param headers map of headers
     */
    public void addHeader(Map<String, String> headers) {
        mHeaders.putAll(headers);
    }

    /**
     * Add header into mHeaders.
     *
     * @param headerKey   header key
     * @param headerValue header value
     */
    public void addHeader(String headerKey, String headerValue) {
        mHeaders.put(headerKey, headerValue);
    }

    /**
     * INTERNAL, do not call from application!
     * Set:
     * <ul>
     * <li>request tag,</li>
     * <li>request url,</li>
     * </ul>
     * for entities added in list.
     */
    public void prepareForExecute() throws ParserException, OutOfMemoryError, IOException {
        if (requests.size() > 0) {
            mKMFODataEntity = requests.get(requests.size() - 1).getEntity();
            if (mKMFODataEntity != null) {
                mRequest.setRequestTAG(mKMFODataEntity.getName());
            }

            mRequest.setRequestUrl(mRequest.getRequestUrl().concat(getRequestURL()));

            if (mEntry != null) {
                mRequest.setData(getRequestData());

                if (mUseJSONFormat) {
                    mHeaders.put(KMFODataConstants.CONTENT_TYPE, KMFODataConstants.APPLICATION_JSON);
                } else {
                    mHeaders.put(KMFODataConstants.CONTENT_TYPE, KMFODataConstants.APPLICATION_ATOM_XML);
                }
            }
        }

        if (mApplicationConnectionId != null) {
            mHeaders.put(KMFODataConstants.X_SUP_APPCID, mApplicationConnectionId);
        }

        if (mHeaders != null) {
            mRequest.setHeaders(mHeaders);
        }

        if (mPagingUse && mRequest.getRequestMethod() == IRequest.REQUEST_METHOD_GET) {
            mPagingBaseURL = mRequest.getRequestUrl();

            mPagingResultList = new ArrayList<KMFODataEntity>();
            mPagingCurrentPage = 0;

            String pagingParams =
                    KMFODataConstants.SKIP
                            .concat(KMFAppConstants.EQUATE)
                            .concat(String.valueOf(0))
                            .concat(KMFAppConstants.AMPERSAND)
                            .concat(KMFODataConstants.TOP)
                            .concat(KMFAppConstants.EQUATE)
                            .concat(String.valueOf(mPagingSize));

            String url = mRequest.getRequestUrl();
            int i = url.indexOf("?");
            if (i > 0) {
                mPagingURLPath = url.substring(0, i);
                mPagingURLParams = url.substring(i + 1);
                mRequest.setRequestUrl(
                        mPagingURLPath
                                .concat(KMFAppConstants.QUESTION_MARK)
                                .concat(mPagingURLParams)
                                .concat(KMFAppConstants.AMPERSAND)
                                .concat(pagingParams));
            } else {
                mPagingURLPath = mPagingBaseURL;
                mRequest.setRequestUrl(
                        mPagingURLPath
                                .concat(KMFAppConstants.QUESTION_MARK)
                                .concat(pagingParams)
                );
            }
        }

        if (mFile != null) {
            if (mFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(mFile);
                byte fileContent[] = new byte[(int) mFile.length()];
                fileInputStream.read(fileContent);
                mRequest.setData(fileContent);
            } else {
                mRequest.setData(new byte[0]);
            }
        }
    }

    /**
     * Execute requests.
     */
    public void execute() {
        if (KMFApplication.getConfig().getConfigOData().getMaxThreads() == 1) {
            execute(false);
        } else {
            execute(true);
        }
    }

    /**
     * Execute requests.
     *
     * @param parallelExecution parallel execution (true = parallel / false = serial)
     */
    public void execute(boolean parallelExecution) {
        if (KMFApplication.getConfig().getConfigSettings().getCheckNetworkBeforeMakeRequest()) {
            if (!KMFHelperNetworkInfo.isConnected(mContext)) {
                onRequestFailure(mContext.getString(R.string.No_connections_are_available));
                return;
            }
        }

        // veskere upravy requestu provadene tesne pred spustenim musi byt
        // v prepareForExecute, jinak nebudou fungovat batch requesty
        try {
            mHandler = new Handler(this);
            KMFODataRequestAsync requestAsyncTask = new KMFODataRequestAsync(mContext, this);

            prepareForExecute();
            if (parallelExecution) {
                requestAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                requestAsyncTask.execute();
            }
        } catch (ParserException e) {
            Log.e(TAG, "KMFODataRequestBuilder#execute", e);
            onRequestFailure(e.getMessage());
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "KMFODataRequestBuilder#execute", e);
            onRequestFailure(e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "KMFODataRequestBuilder#execute", e);
            onRequestFailure(e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "KMFODataRequestBuilder#execute", e);
            onRequestFailure(e.getMessage());
        }
    }

    @Override
    public IRequest getRequest() {
        return mRequest;
    }

    @Override
    public void processResponseSuccess(IResponse response) {
        if (mProgressDialogShow && mProgressDialog == null) {
            mProgressDialog = new KMFDialogProgress(mContext);
            mProgressDialog.setTitleBackground(R.drawable.dialog_info);
            mProgressDialog.setTitleImage(R.drawable.ic_dialog_info);
            mProgressDialog.setTitleImageTint(mContext.getResources().getColor(R.color.dialogInfo));
            mProgressDialog.setTitleText(mContext.getString(R.string.Process_data));
            mProgressDialog.setTitleTextColor(mContext.getResources().getColor(R.color.dialogInfo));
            mProgressDialog.setProgressBarSpinner();
            mProgressDialog.setMessageText(mContext.getString(R.string.Wait_please));
            mProgressDialog.show();
        }

        switch (mRequest.getRequestMethod()) {
            case IRequest.REQUEST_METHOD_GET:
                processRequestGet(response);
                break;

            case IRequest.REQUEST_METHOD_POST:
                processRequestPost(response);
                break;

            case IRequest.REQUEST_METHOD_PUT:
                processRequestPut();
                break;

            case IRequest.REQUEST_METHOD_DELETE:
                processRequestDelete();
                break;
        }
    }

    @Override
    public void processResponseError(IResponse response, String errorMessage) {
        handleRequestFailure(errorMessage);
    }

    /**
     * Process response data using method GET.
     */
    private void processRequestGet(final IResponse response) {
        String requestTag = getRequest().getRequestTAG();
        if (requestTag.equals(IODataServiceDocument.class.getSimpleName())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        KMFApplication.getCache().storeDocument(
                                KMFApplication.getParser().parseODataServiceDocument(
                                        getEntityUsingUtils(response)
                                )
                                , ICache.DocumentType.ServiceDocument
                                , IODataServiceDocument.class.getSimpleName());

                        handleRequestSuccess(null);
                    } catch (CacheException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (ParserException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    }
                }
            }).start();
        } else if (requestTag.equals(IODataSchema.class.getSimpleName())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        KMFApplication.getCache().storeDocument(
                                KMFApplication.getParser().parseODataSchema(
                                        getEntityUsingUtils(response)
                                        , KMFApplication.getServiceDocument()
                                )
                                , ICache.DocumentType.MetaDocument
                                , IODataSchema.class.getSimpleName());

                        handleRequestSuccess(null);
                    } catch (CacheException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (ParserException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    }
                }
            }).start();
        } else if (mPagingUse && mPagingCurrentPage == PAGING_GET_COUNT) {
            try {
                //pri $count = 0 metoda getEntityUsingUtils(response) vracela prazdny string ""
                //nasledoval pad aplikace
                //jako hotfix se v takovem pripade podstrci "0"
                String result = getEntityUsingUtils(response);
                pagingFirstPage(Integer.parseInt(TextUtils.isEmpty(result) ?
                        "0" :
                        result));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                handleRequestFailure(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                handleRequestFailure(e.getMessage());
            }
        } else if (mIsDataStream) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fileOutputStream = null;
                    InputStream inputStream = null;
                    File file = null;
                    try {
                        if (mFileName == null || mFileName.isEmpty()) {
                            String contentDisposition = null;
                            contentDisposition = response.getHeadersMap().get(KMFODataConstants.HEADER_CONTENT_DISPOSITION);

                            if (contentDisposition == null) {
                                contentDisposition = response.getHeadersMap().get(KMFODataConstants.HEADER_CONTENT_DISPOSITION.toLowerCase());
                            }

                            if (contentDisposition == null) {
                                contentDisposition = response.getHeadersMap().get(KMFODataConstants.HEADER_CONTENT_DISPOSITION.toUpperCase());
                            }

                            file = new File(
                                    mFileDirectory
                                    , URLDecoder.decode(contentDisposition.substring(
                                    contentDisposition.indexOf(KMFODataConstants.FILENAME)
                                            + KMFODataConstants.FILENAME.length()
                                    , contentDisposition.length()), "UTF-8")
                                    .replace("\"", "")
                            );
                        } else {
                            file = new File(
                                    mFileDirectory
                                    , mFileName
                            );
                        }

                        if (file.exists()) {
                            file.delete();
                        }

                        inputStream = response.getEntity().getContent();
                        fileOutputStream = new FileOutputStream(file.getPath());

                        byte[] buffer = new byte[1024 * 1024];
                        int byteCount = 0;
                        while ((byteCount = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, byteCount);
                        }

                        if (inputStream != null) {
                            inputStream.close();
                        }

                        fileOutputStream.flush();
                        fileOutputStream.close();

                        List<File> files = new ArrayList<File>();
                        files.add(file);

                        handleRequestSuccess(files);
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            handleRequestFailure(e.getMessage());
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                handleRequestFailure(e.getMessage());
                            }
                        }
                    }
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<IODataEntry> ioDataEntryList;
                        if (mKMFODataEntity.isFunctionImport() && mKMFODataEntity.isComplexType()) {
                            ioDataEntryList = parseFunctionImportResult(
                                    getEntityUsingUtils(response)
                            );
                        } else {
                            IODataFeed dataFeed = parseODataFeed(
                                    getEntityUsingUtils(response)
                            );
                            if (mCacheSave) {
                                cacheSave(dataFeed);
                            }

                            if (mUseDeltaToken) {
                                saveDeltaToken(dataFeed);
                            }

                            ioDataEntryList = dataFeed.getEntries();
                        }

                        if (ioDataEntryList == null) {
                            if (mPagingUse) {
                                pagingNextPage();
                            } else {
                                handleRequestSuccess(new ArrayList<KMFODataEntity>());//v novem android SDK pouzit: new ArrayList<>()
                            }
                        }

                        if (ioDataEntryList == null) {
                            handleRequestSuccess(new ArrayList<KMFODataEntity>());//v novem android SDK pouzit: new ArrayList<>()
                        }

                        List<KMFODataEntity> listEntity = new ArrayList<KMFODataEntity>();

                        if (ioDataEntryList != null) {
                            for (IODataEntry entry : ioDataEntryList) {
                                try {
                                    KMFODataEntity entryClone = mKMFODataEntity.clone();
                                    entryClone.setEntry(entry);
                                    listEntity.add(entryClone);
                                } catch (CloneNotSupportedException e) {
                                    KMFApplication.getLogger().e(TAG, e.getLocalizedMessage());
                                }
                            }
                        }

                        if (mPagingUse) {
                            mPagingResultList.addAll(listEntity);
                            pagingNextPage();
                        } else {
                            handleRequestSuccess(listEntity);
                        }
                    } catch (CacheException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (ParserException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleRequestFailure(e.getMessage());
                    }
                }
            }).start();
        }
    }

    /**
     * Save delta token from dataFeed to shared preferences
     * @param dataFeed
     */
    private void saveDeltaToken(IODataFeed dataFeed) {
        KMFODataDeltaToken.saveDeltaToken(mURLNoDeltaToken, dataFeed);
    }

    /**
     * Process response data using method POST.
     * Response contains created entry.
     */

    /**
     * Process response data using method POST.
     * Response contains created entry.
     */
    protected void processRequestPost(final IResponse response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<IODataEntry> ioDataEntryList = null;
                    if (getRequest().getRequestTAG().equals(IODataSubscriptionEntry.class.getSimpleName())) {
                        //TODO - co delat se subsckripci? nekam ulozit ID pro pripadnou oddregistraci subscripce
                        //method DELETE
                        ///sap/opu/odata/sap/ZKMF_SRV/SubscriptionCollection('ID')
                    } else if (mKMFODataEntity.isFunctionImport() && mKMFODataEntity.isComplexType()) {
                        ioDataEntryList = parseFunctionImportResult(getEntityUsingUtils(response));
                    } else {
                        IODataFeed dataFeed = parseODataFeed(getEntityUsingUtils(response));
                        if (mCacheSave) {
                            cacheSave(dataFeed);
                        }
                        ioDataEntryList = dataFeed.getEntries();
                    }

                    if (ioDataEntryList == null) {
                        handleRequestSuccess(null);
                        return;
                    }

                    List<KMFODataEntity> listEntity = new ArrayList<KMFODataEntity>();

                    if (ioDataEntryList != null) {
                        for (IODataEntry entry : ioDataEntryList) {
                            try {
                                KMFODataEntity entryClone = mKMFODataEntity.clone();
                                entryClone.setEntry(entry);
                                listEntity.add(entryClone);
                            } catch (CloneNotSupportedException e) {
                                KMFApplication.getLogger().e(TAG, e.getLocalizedMessage());
                            }
                        }
                    }

                    handleRequestSuccess(listEntity);
                } catch (CacheException e) {
                    e.printStackTrace();
                    handleRequestFailure(e.getMessage());
                } catch (ParserException e) {
                    e.printStackTrace();
                    // TODO - POZOR pri batch create, pokud jedna z operaci skonci chybou chybou
                    // (napr. zaznam uz existuje), pak VSECHNY create operace v batchi budou mit
                    // prazdne telo responsu, i kdyz probehly uspesne!!!
                    // zrejme bug ve frameworku
                    handleRequestFailure(e.getMessage());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    handleRequestFailure(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    handleRequestFailure(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Process response data using method PUT.
     * Response is empty.
     */
    protected void processRequestPut() {
        handleRequestSuccess(null);
    }

    /**
     * Process response data using method DELETE.
     * Response is empty.
     */
    protected void processRequestDelete() {
        handleRequestSuccess(null);
    }

    /**
     * Parse data using collection ID. If entity is import function entity set is used as
     * collection ID. Method return data feed.
     *
     * @param responseString
     * @return
     * @throws ParserException
     */
    protected IODataFeed parseODataFeed(String responseString) throws ParserException, IllegalArgumentException {
        if (mKMFODataEntity.isFunctionImport()) {
            return KMFApplication.getParser().parseODataFeed(
                    responseString
                    , mKMFODataEntity.getEntitySet()
                    , KMFApplication.getMetaDocument()
            );
        } else {
            try {
                return KMFApplication.getParser().parseODataFeed(
                        responseString
                        , mKMFODataEntity.getCollectionId()
                        , KMFApplication.getMetaDocument()
                );
            } catch (IndexOutOfBoundsException e) {
                if (responseString.equals("{\"d\":{\"results\":[]}}") &&
                        e.getMessage().equals("Invalid index 0, size is 0")) {
                    return new ODataFeed();
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Parse data using function import. Method return entries.
     *
     * @param responseString
     * @return
     * @throws ParserException
     */

    protected List<IODataEntry> parseFunctionImportResult(String responseString) throws ParserException {
        return KMFApplication.getParser().parseFunctionImportResult(
                responseString
                , mKMFODataEntity.getFunctionImport()
                , KMFApplication.getMetaDocument()
        );
    }

    /**
     * Get entity using utils.
     *
     * @param response response
     * @return String containing entity data
     * @throws IOException
     */
    protected String getEntityUsingUtils(IResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
    }

    /**
     * Save data to cache (server cache). If cache clear is true, cache will be cleared first.
     * Data in cache is defined by key, f.e.:
     * first URL ".../service/DOCSet?$filter=(DOC_TYPE%20eq%20'KMF1')" = 5 entries
     * second URL ".../service/DOCSet?$filter=(DOC_TYPE%20eq%20'KMF2')" = 10 entries
     * all merge entries into cache with key "DOCSet" -> cache contains 15 entries
     *
     * @param dataFeed
     * @throws CacheException
     */
    protected void cacheSave(IODataFeed dataFeed) throws CacheException {
        if (mCacheCollectionId == null) {
            mCacheCollectionId = mKMFODataEntity.getCollectionId();
        }

        if (mCacheClear) {
            KMFApplication.getCache().clearCache(mCacheCollectionId);
            mCacheClear = false; // do not clear again on paged requests
        }

        KMFApplication.getCache().mergeEntries(dataFeed, mCacheCollectionId);
    }

    /**
     * Handle request success.
     *
     * @param list data
     */
    protected void handleRequestSuccess(List<?> list) {
        dismissProgressDialog();

        if (mHandler == null) {
            onRequestSuccess(list);
        } else {
            mHandler.sendMessage(
                    mHandler.obtainMessage(
                            REQUEST_SUCCESS
                            , list
                    )
            );
        }
    }

    /**
     * Handle request failure.
     *
     * @param errorMessage error message
     */
    protected void handleRequestFailure(String errorMessage) {
        dismissProgressDialog();

        if (mHandler == null) {
            onRequestFailure(errorMessage);
        } else {
            mHandler.sendMessage(
                    mHandler.obtainMessage(
                            REQUEST_FAILURE
                            , errorMessage
                    )
            );
        }
    }

    /**
     * Dismiss progress dialog.
     */
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

    }

    /**
     * Return data (List<KMFODataEntity>) for success request.
     *
     * @param list data
     */
    public abstract void onRequestSuccess(List<?> list);

    /**
     * Return error message for error request.
     *
     * @param errorMessage error message
     */
    public abstract void onRequestFailure(String errorMessage);
}
