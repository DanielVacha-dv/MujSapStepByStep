package com.sap.stepbystep.kmf.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class KMFHelperNetworkInfo {

    /**
     * Get active network info. For more detail see {@link android.net.ConnectivityManager#getActiveNetworkInfo()}
     *
     * @param context
     * @return
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    /**
     * Returns details about the currently active data network.
     * For more information check {@link android.net.ConnectivityManager} and {@link android.net.NetworkInfo}.
     *
     * @param context
     * @return true if connected
     */
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return (networkInfo != null && networkInfo.isConnected());
    }
}

