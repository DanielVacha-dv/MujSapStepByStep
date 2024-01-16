package com.sap.stepbystep.smf.repository;

import android.content.Context;
import android.util.Log;

import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.listener.KMFOnlineStoreOpenListener;
import com.sap.stepbystep.kmf.store.manager.KMFOnlineManager;

import java.util.Objects;

public class SMFLoginRepository {
    public static final String TAG = SMFLoginRepository.class.getName();

    public static boolean login(Context context, OnlineODataStore.OpenListener listener) {
        Log.d(TAG, "login(Context context, IKMFResponseHandler responseHandler)");

        try {
            LogonCore.getInstance().getLogonContext();
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return openOnlineStoreAsync(context, listener);
    }

    public static boolean openOnlineStoreAsync(Context context, OnlineODataStore.OpenListener listener) {
        try {
            KMFOnlineStoreOpenListener.getInstance().setStore(null);
            return KMFOnlineManager.openOnlineStoreAsync(context, listener);
        } catch (KMFOnlineODataStoreException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
        return false;
    }
}
