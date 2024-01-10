package com.sap.stepbystep.kmf.odata;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sap.mobile.lib.parser.IODataError;
import com.sap.mobile.lib.parser.ParserException;
import com.sap.mobile.lib.request.INetListener;
import com.sap.mobile.lib.request.IRequest;
import com.sap.mobile.lib.request.IRequestStateElement;
import com.sap.mobile.lib.request.IResponse;
import com.sap.stepbystep.R;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.helpers.KMFHelperString;
import com.sap.stepbystep.kmf.ui.listeners.KMFDialogProgress;

import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class KMFODataRequestAsync
        extends AsyncTask<Void, Integer, String>
        implements INetListener {

    protected static String TAG = KMFODataRequestAsync.class.getName();

    private Context mContext;
    private IKMFODataRequest mKMFODataRequest;

    private KMFDialogProgress mProgressDialog;
    private int mProgressMax;
    private int mProgressActual;
    private boolean mResponseSuccess;
    private IResponse mResponse;
    private String mResponseEntityString;
    private String mErrorMessage;
    private boolean mResponseReceived;
    private long mStartTimeStamp;

    public KMFODataRequestAsync(Context context, IKMFODataRequest KMFODataRequest) {
        mContext = context;
        mKMFODataRequest = KMFODataRequest;
        mProgressMax = KMFApplication.getConfig().getConfigPreferences().getConnectivitySconnTimeOut() / 1000;
    }

    /**
     * Get progress dialog message.
     *
     * @return progress dialog message
     */
    private String getProgressDialogMessage() {
        String progressDialogMessage = null;
        String tempProgressDialogMessage;

        if (mKMFODataRequest instanceof KMFODataRequestBuilder) {
            tempProgressDialogMessage = ((KMFODataRequestBuilder) mKMFODataRequest).getProgressDialogMessage();
            if (tempProgressDialogMessage != null)
                progressDialogMessage = KMFHelperString
                        .concatStingsWithSeparator(progressDialogMessage
                                , KMFAppConstants.NEW_LINE
                                , tempProgressDialogMessage
                        );
        }

//        if (mKMFODataRequest instanceof KMFODataBatchRequestBuilder) {
//            for (KMFODataRequestBuilder requests : ((KMFODataBatchRequestBuilder) mKMFODataRequest).getRequests()) {
//                tempProgressDialogMessage = requests.getProgressDialogMessage();
//                if (tempProgressDialogMessage != null)
//                    progressDialogMessage = KMFHelperString
//                            .concatStingsWithSeparator(progressDialogMessage
//                                    , KMFAppConstants.NEW_LINE
//                                    , tempProgressDialogMessage
//                            );
//            }
//        }

        return progressDialogMessage;
    }

    /**
     * Show progress dialog.
     *
     * @return false if progress dialog is disabled otherwise true
     */
    private boolean showProgressDialog() {
        if (mKMFODataRequest instanceof KMFODataRequestBuilder)
            return ((KMFODataRequestBuilder) mKMFODataRequest).showProgressDialog();

//        if (mKMFODataRequest instanceof KMFODataBatchRequestBuilder)
//            //kontroluji pouze prvni request, pokud je potreba doplnti logiku, jak mam urcit, zda zobrazovat progress dialog
//            return ((KMFODataBatchRequestBuilder) mKMFODataRequest).getRequests().get(0).showProgressDialog();

//        if (mKMFODataRequest instanceof KMFODataGCMRequest)
//            return false;

        return true;
    }

    /**
     * At first show progress dialog using style horiznotal and continue in doInBackground()
     */
    @Override
    protected void onPreExecute() {
        if (showProgressDialog()) {
            String progressDialogMessage = getProgressDialogMessage();

            mProgressDialog = new KMFDialogProgress(mContext);
            mProgressDialog.setTitleBackground(R.drawable.dialog_info);
            mProgressDialog.setTitleImage(R.drawable.ic_dialog_info);
            mProgressDialog.setTitleImageTint(mContext.getResources().getColor(R.color.dialogInfo));
            mProgressDialog.setTitleText(mContext.getString(R.string.Sending_request_to_server));
            mProgressDialog.setTitleTextColor(mContext.getResources().getColor(R.color.dialogInfo));
            if (progressDialogMessage != null && !progressDialogMessage.isEmpty())
                mProgressDialog.setMessageText(progressDialogMessage);
            else
                mProgressDialog.setMessageText(mContext.getString(R.string.Wait_please));
            mProgressDialog.setProgressBarHorizontal(0, mProgressMax);
            mProgressDialog.show();
        }

        mStartTimeStamp = System.currentTimeMillis();
    }

    /**
     * Make request and chceck if progress dialog status has max value. If status is lower
     * than max value, thread go to sleep for 1000ms and then update progress dialog status.
     * Otherwise continue in onPostExecute().
     *
     * @param voids
     * @return
     */
    @Override
    protected String doInBackground(Void... voids) {
        int progress = 0;

        mResponseReceived = false;

        mKMFODataRequest.getRequest().setListener(this);
        KMFApplication.getRequestManager().makeRequest(mKMFODataRequest.getRequest());
        while (!mResponseReceived) {
            try {
                Thread.sleep(1000);
                progress++;
                if (showProgressDialog() && mProgressActual < mProgressMax)
                    publishProgress(progress);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mErrorMessage;
    }

    /**
     * Update progress dialog status (progress).
     * Refresh
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length == 1 && mProgressActual < values[0])
            mProgressActual = values[0];
        else
            mProgressActual = mProgressMax;

        if (mProgressDialog != null)
            mProgressDialog.setProgressBarHorizontalProgress(mProgressActual);
    }

    /**
     * After request success(INetListener):
     * - set mResponseSuccess = true
     * - save response
     * - finish progress bar
     *
     * @param request
     * @param response
     */
    @Override
    public void onSuccess(IRequest request, IResponse response) {
        mResponseSuccess = true;
        mResponse = response;
        mResponseReceived = true;

        Log.d(TAG, "Request " + request.getRequestUrl() + " success in " + (System.currentTimeMillis() - mStartTimeStamp) + "ms");
    }

    /**
     * After request error(INetListener):
     * - save response //TODO - je opravdu potreba??? asi ne
     *
     * @param request
     * @param response
     * @param requestStateElement
     */
    @Override
    public void onError(IRequest request, IResponse response, IRequestStateElement requestStateElement) {
        mResponse = response;
        Log.d(TAG, "Request " + request.getRequestUrl() + " failed in " + (System.currentTimeMillis() - mStartTimeStamp) + "ms");

        Exception ex = requestStateElement.getException();

        if(ex != null) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        switch (requestStateElement.getErrorCode()) {
            case IRequestStateElement.NO_ERROR: // -1
                /**
                 * Possible errors:
                 * - Documentation ->
                 */
                setErrorMessage(mContext.getString(R.string.Unknown_erros_please_do_debug_and_complete_string_xml));
                // TODO - zatim nenasimulovano -> nevim o jakou chybu se muze jednat a zda se zapisuje do logu
                break;
            case IRequestStateElement.HTTP_ERROR: // 1
                /**
                 * Possible errors:
                 * - Documentation -> Server response status > 302
                 */
                setErrorMessage(String.format(mContext.getString(R.string.HTTP_status_code), requestStateElement.getHttpStatusCode()));
                // TODO - nezapisuje se do logu
                break;
            case IRequestStateElement.PARSE_ERROR: // 2
                /**
                 * Possible errors:
                 * - Documentation -> Problem with parsing the response xml
                 */
                setErrorMessage(mContext.getString(R.string.Unknown_erros_please_do_debug_and_complete_string_xml));
                // TODO - zatim nenasimulovano -> nevim o jakou chybu se muze jednat a zda se zapisuje do logu
                break;
            case IRequestStateElement.NETWORK_ERROR: // 3
                /**
                 * Possible errors:
                 * - Documentation -> There was a network error during a read/write to/from stream.
                 * - AndroidManifest.xml dosn't containt internet persmission (<uses-permission android:name="android.permission.INTERNET" />)
                 * - Check request URL.
                 * - Device is not connected to internet.
                 */
                setErrorMessage(mContext.getString(R.string.Failed_to_connect_the_service));
                // TODO - vyzkouset zda se loguje a pokud ne zda se ma logovat?
                break;
            case IRequestStateElement.AUTHENTICATION_ERROR: // 4
                /**
                 * Possible errors:
                 * - Documentation -> Authentication error
                 * - user name or password is incorrect
                 */
                setErrorMessage(mContext.getString(R.string.Authorization_error_User_or_password_is_incorrect));
                // TODO - vyzkouset zda se loguje a pokud ne zda se ma logovat?
                break;
            case IRequestStateElement.CLIENT_ERROR: // 5
                /**
                 * Possible errors:
                 * - Documentation -> Client error
                 * - Request with not complete keys.
                 * - Call import function which return error.
                 */
                setErrorMessage(mContext.getString(R.string.Bad_request));
                break;
            case IRequestStateElement.REQUEST_ERROR: // 6
                /**
                 * Possible errors:
                 * - Documentation ->  Problem with parsing the request
                 */
                setErrorMessage(mContext.getString(R.string.Unknown_erros_please_do_debug_and_complete_string_xml));
                // TODO - zatim nenasimulovano -> nevim o jakou chybu se muze jednat a zda se zapisuje do logu
                break;
        }

        if (response != null) {
            try {
                mResponseEntityString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mResponseReceived = true;
    }

    /**
     * Close progress dialog with style horizontal and reopen with style spinner for process response data.
     *
     * @param errorMessage
     */
    @Override
    protected void onPostExecute(String errorMessage) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        if (mResponseSuccess) {
            mKMFODataRequest.processResponseSuccess(mResponse);
        } else {
            if (mResponseEntityString != null && !mResponseEntityString.isEmpty()) {
                if (!(mResponseEntityString.contains("<html>") || mResponseEntityString.contains("<HTML>"))) {
                    try {
                        IODataError dataError = KMFApplication.getParser().parseODataError(mResponseEntityString);
                        String error = new String("");
                        String message = dataError.getMessage();
                        if (message != null)
                            error = message;

                        String errorCode = dataError.getErrorCode();
                        if (errorCode != null)
                            error = error.concat(KMFHelperString.bracket(errorCode));

                        setErrorMessage(error);
                    } catch (ParserException e) {
                        setErrorMessage(mResponseEntityString);
                        e.printStackTrace();
                    }
                }
            }
            mKMFODataRequest.processResponseError(mResponse, mErrorMessage);
        }
    }

    /**
     * Set error message.
     *
     * @param errorMessage error message
     */
    protected void setErrorMessage(String errorMessage) {
        if (mErrorMessage == null)
            mErrorMessage = errorMessage;
        else
            mErrorMessage = KMFHelperString.concatStingsWithSeparator(
                    mErrorMessage
                    , KMFAppConstants.NEW_LINE
                    , errorMessage
            );
    }
}