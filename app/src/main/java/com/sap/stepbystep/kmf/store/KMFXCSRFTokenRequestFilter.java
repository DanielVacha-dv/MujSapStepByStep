package com.sap.stepbystep.kmf.store;

import android.util.Log;

import com.sap.maf.tools.logon.core.LogonCoreContext;
import com.sap.maf.tools.logon.core.LogonCoreException;
import com.sap.smp.client.httpc.HttpMethod;
import com.sap.smp.client.httpc.events.ISendEvent;
import com.sap.smp.client.httpc.filters.IRequestFilter;
import com.sap.smp.client.httpc.filters.IRequestFilterChain;

public class KMFXCSRFTokenRequestFilter implements IRequestFilter {
    public static final String TAG =
            KMFXCSRFTokenRequestFilter.class.getSimpleName();
    private static final String HTTP_HEADER_SUP_APPCID = "X-SUP-APPCID";
    private static final String HTTP_HEADER_SMP_APPCID = "X-SMP-APPCID";

    private static KMFXCSRFTokenRequestFilter instance;

    private String mLastXCSRFToken = null;
    private final LogonCoreContext mLogonCoreCtx;

    private KMFXCSRFTokenRequestFilter(LogonCoreContext logonContext) {
        mLogonCoreCtx = logonContext;
    }

    /**
     * @return KMFXCSRFTokenRequestFilter
     */
    public static KMFXCSRFTokenRequestFilter getInstance(LogonCoreContext logonContext) {
        if (instance == null) {
            instance = new KMFXCSRFTokenRequestFilter(logonContext);
        }
        return instance;
    }


    @Override
    public Object filter(ISendEvent event, IRequestFilterChain chain) {
        HttpMethod method = event.getMethod();
        Log.i(TAG, "method: " + method + ", mLastXCSRFToken: " + mLastXCSRFToken);
        if (method == HttpMethod.GET /* && mLastXCSRFToken == null */) {
            event.getRequestHeaders().put("X-CSRF-Token", "Fetch");
        } else if (mLastXCSRFToken != null) {
            event.getRequestHeaders().put("X-CSRF-Token", mLastXCSRFToken);
        } else {
            event.getRequestHeaders().put("X-Requested-With", "XMLHttpRequest");
        }

        String appConnID = null;
        try {
            appConnID = mLogonCoreCtx.getConnId();
        } catch (LogonCoreException e) {
            Log.e(TAG, "error getting connection id", e);
        }

        //for backward compatibility. not needed for SMP 3.0 SP05
        if (appConnID != null) {
            event.getRequestHeaders().put(HTTP_HEADER_SUP_APPCID, appConnID);
            event.getRequestHeaders().put(HTTP_HEADER_SMP_APPCID, appConnID);
        }
        event.getRequestHeaders().put("Connection", "Keep-Alive");

        return chain.filter();
    }

    @Override
    public Object getDescriptor() {
        return "KMFXCSRFTokenRequestFilter";
    }

    public void setLastXCSRFToken(String lastXCSRFToken) {
        this.mLastXCSRFToken = lastXCSRFToken;
    }

}
