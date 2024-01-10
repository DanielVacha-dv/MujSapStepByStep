package com.sap.stepbystep.kmf.odata;

import android.text.TextUtils;
import android.util.Log;

import com.sap.mobile.lib.parser.IODataFeed;
import com.sap.smp.client.odata.ODataPayload;
import com.sap.smp.client.odata.impl.ODataEntitySetDefaultImpl;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.app.KMFAppConstants;

public class KMFODataDeltaToken {
    public static final String TAG = KMFODataDeltaToken.class.getName();

    /**
     * Parse delta token from data feed and save it to shared preferences under url
     *
     * @param url      url as shared preferences key
     * @param dataFeed data feed to parse delta token
     */
    public static void saveDeltaToken(String url, IODataFeed dataFeed) {
        String deltaToken = dataFeed.getDeltaLink().getHref();

        deltaToken = parseDeltaToken(deltaToken);

        saveDeltaToken(url, deltaToken);
    }

    /**
     * Parse delta token from data feed and save it to shared preferences under url
     *
     * @param payload data payload to parse delta token
     */
    public static void saveDeltaToken(String url, ODataPayload payload) {
        Log.d(TAG, "saveDeltaToken(String url, ODataPayload payload)");
        String deltaToken;

        deltaToken = ((ODataEntitySetDefaultImpl) payload).getDeltaPath();
        deltaToken = parseDeltaToken(deltaToken);

        saveDeltaToken(url, deltaToken);
    }

    private static String parseDeltaToken(String deltaToken) {
        if (TextUtils.isEmpty(deltaToken)) {
            return null;
        }
        if (!deltaToken.contains(KMFODataConstants.DELTATOKEN)) {
            return null;
        }

        deltaToken = deltaToken.substring(
                deltaToken.indexOf(KMFAppConstants.EXCLAMATION_MARK.concat(KMFODataConstants.DELTATOKEN)) +
                        KMFAppConstants.EXCLAMATION_MARK.concat(KMFODataConstants.DELTATOKEN).length()
        );

        if (deltaToken.contains(KMFAppConstants.AMPERSAND)) {
            deltaToken = deltaToken.substring(
                    0,
                    deltaToken.indexOf(KMFAppConstants.AMPERSAND)
            );
        }

        deltaToken = deltaToken.replace(KMFAppConstants.SINGLE_QUOTATION_MARK, "");
        deltaToken = deltaToken.replace(KMFAppConstants.HTML_SINGLE_QUOTATION_MARK, "");

        return deltaToken;
    }

    /**
     * Save delta token to shared preferences under url
     *
     * @param url        url as shared preferences key
     * @param deltaToken delta token
     */
    public static void saveDeltaToken(String url, String deltaToken) {
        if (TextUtils.isEmpty(url) ||
                TextUtils.isEmpty(deltaToken)) {
            return;
        }

        Log.d(TAG, "Saving delta token " + url + ": " + deltaToken);

        KMFApplication.setDeltaTokenSharedPreferences(url, deltaToken);
    }


    /**
     * Get saved delta token for url
     *
     * @param url url as shared preferences key
     * @return delta token
     */
    public static String getDeltaToken(String url) {
        return KMFApplication.getDeltaTokenSharedPreferencesString(url, null);
    }
}
