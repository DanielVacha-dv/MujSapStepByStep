package com.sap.stepbystep.smf.repository;

import android.content.Context;

import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.stepbystep.kmf.store.error.KMFOnlineODataStoreException;
import com.sap.stepbystep.kmf.store.error.KMFRegistrationManagerException;
import com.sap.stepbystep.kmf.store.manager.KMFOnlineManager;
import com.sap.stepbystep.kmf.store.manager.KMFRegistrationManager;

public class SMFBaseRepository {
    public static final String TAG = SMFBaseRepository.class.getName();

    protected static void openOnlineStore(Context context) throws KMFOnlineODataStoreException {
        try {
            LogonCore.getInstance().getLogonContext();
        } catch (Exception ex) {
            try {
                KMFRegistrationManager.initialize(context, context.getPackageName());
            } catch (KMFRegistrationManagerException e) {
                e.printStackTrace();
            }
        }

        KMFOnlineManager.openOnlineStore(context);
    }
}

