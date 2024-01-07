package com.sap.stepbystep.kmf.android;

import android.content.Context;
import android.util.Log;

import com.sap.smp.client.odata.metadata.ODataMetadata;
import com.sap.smp.client.supportability.ClientLogDestination;
import com.sap.smp.client.supportability.ClientLogLevel;
import com.sap.smp.client.supportability.ClientLogManager;
import com.sap.smp.client.supportability.ClientLogger;
import com.sap.smp.client.supportability.Supportability;
import com.sap.stepbystep.kmf.store.manager.KMFOnlineManager;

import java.util.EnumSet;

public class KMFApplication2 extends KMFApplication {
    private static ClientLogger logger = null;

    public static ODataMetadata getMetadataDocument() {
        return KMFOnlineManager.getStore().getMetadata();
    }

    public static ClientLogger getClientLogger(Context ctx) {
        if (logger == null) {
            ClientLogManager logManager = Supportability.getInstance().getClientLogManager(ctx);
            logManager.setLogDestination(EnumSet.of(ClientLogDestination.CONSOLE, ClientLogDestination.FILESYSTEM));
            logger = logManager.getLogger("logger");
            logManager.setLogLevel(ClientLogLevel.INFO, "logger");
        }

        return logger;
    }

    public static void logI(Context ctx, String tag, String message, Throwable throwable) {
        getClientLogger(ctx).logInfo(message, throwable);
        Log.i(tag, message, throwable);
    }

    public static void logD(Context ctx, String tag, String message, Throwable throwable) {
        getClientLogger(ctx).logDebug(message, throwable);
        Log.d(tag, message, throwable);
    }

    public static void logW(Context ctx, String tag, String message, Throwable throwable) {
        getClientLogger(ctx).logWarning(message, throwable);
        Log.w(tag, message, throwable);
    }

    public static void logE(Context ctx, String tag, String message, Throwable throwable) {
        getClientLogger(ctx).logError(message, throwable);
        Log.e(tag, message, throwable);
    }

}

