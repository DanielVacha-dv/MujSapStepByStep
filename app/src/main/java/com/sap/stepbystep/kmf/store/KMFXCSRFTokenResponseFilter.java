package com.sap.stepbystep.kmf.store;

import android.content.Context;
import android.util.Log;

import com.sap.smp.client.httpc.events.IReceiveEvent;
import com.sap.smp.client.httpc.filters.IResponseFilter;
import com.sap.smp.client.httpc.filters.IResponseFilterChain;
import com.sap.smp.client.httpc.utils.SAPLoggerUtils;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.supportability.ClientLogLevel;
import com.sap.smp.client.supportability.ClientLogger;
import com.sap.smp.client.supportability.Supportability;
import com.sap.stepbystep.kmf.store.manager.KMFManager;

import java.io.IOException;
import java.util.List;

public class KMFXCSRFTokenResponseFilter implements IResponseFilter {
    public static final String TAG =
            KMFXCSRFTokenResponseFilter.class.getSimpleName();

    private static KMFXCSRFTokenResponseFilter instance;

    private final Context mContext;
    private final KMFXCSRFTokenRequestFilter mRequestFilter;
    private KMFStreamHandlersBuffer mStreamHandlersBuffer;

    public void setStreamHandlersBuffer(KMFStreamHandlersBuffer streamHandlersBuffer) {
        this.mStreamHandlersBuffer = streamHandlersBuffer;
    }

    private KMFXCSRFTokenResponseFilter(Context context, KMFXCSRFTokenRequestFilter requestFilter) {
        this.mContext = context;
        this.mRequestFilter = requestFilter;
    }

    /**
     * @return KMFXCSRFTokenResponseFilter
     */
    public static KMFXCSRFTokenResponseFilter getInstance(Context context, KMFXCSRFTokenRequestFilter requestFilter) {
        if (instance == null) {
            instance = new KMFXCSRFTokenResponseFilter(context, requestFilter);
        }
        return instance;
    }


    @Override
    public Object filter(IReceiveEvent event, IResponseFilterChain chain)
            throws IOException {
        ClientLogger logger = Supportability.getInstance().getClientLogger(this.mContext, OnlineODataStore.class.getCanonicalName());

        SAPLoggerUtils.logResponseDetails(event, logger, ClientLogLevel.INFO, true, true);
        List<String> xcsrfTokens = event.getResponseHeaders().get("X-CSRF-Token");
        Log.i(TAG, "xcsrfTokens: " + xcsrfTokens);
        if (xcsrfTokens != null) {
            String xcsrfToken = xcsrfTokens.get(0);
            if (xcsrfToken != null) {
                mRequestFilter.setLastXCSRFToken(xcsrfToken);
            }
        }

        if (event.getResponseURL().toString().contains(KMFManager.VALUE) &&
                event.getStream() != null &&
                this.mStreamHandlersBuffer.hasKey(event.getResponseURL().toString())) {
            this.mStreamHandlersBuffer.get(event.getResponseURL().toString()).handle(event.getStream());
        }

        return chain.filter();
    }

    @Override
    public Object getDescriptor() {
        return "KMFXCSRFTokenResponseFilter";
    }
}
