package com.sap.stepbystep.kmf.store.manager;

import android.content.Context;

import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.stepbystep.kmf.store.error.KMFRegistrationManagerException;

public class KMFRegistrationManager {
    public static final String VK_APPCID = "appcid";

    private static LogonCore logonCore;

    public static String getAppConnectionId() throws KMFRegistrationManagerException {
        try {
            return KMFRegistrationManager.logonCore.getLogonContext().getConnId();
        } catch (Exception e) {
            throw new KMFRegistrationManagerException(e);
        }
    }

    /**
     * Initialize logon core and return app connection id;
     * @param ctx context
     * @param appId application id
     * @return  connection id
     * @throws KMFRegistrationManagerException
     */
    public static String initialize(Context ctx, String appId) throws KMFRegistrationManagerException {
        return initialize(ctx, appId, null);
    }

    /**
     * Initialize logon core and return app connection id;
     * @param ctx ctx
     * @param appId appId
     * @param password password
     * @return  connection id
     * @throws KMFRegistrationManagerException
     */
    public static String initialize(Context ctx, String appId, String password) throws KMFRegistrationManagerException {
        String appConnectionId = null;
        KMFRegistrationManager.logonCore = LogonCore.getInstance();

        KMFRegistrationManager.logonCore.init(ctx, appId);
        try {
            if (KMFRegistrationManager.logonCore.isStoreAvailable()) {
                KMFRegistrationManager.logonCore.unlockStore(password);
                appConnectionId = KMFRegistrationManager.logonCore.getObjectFromStore(KMFRegistrationManager.VK_APPCID);
            }
        } catch (Exception e) {
            throw new KMFRegistrationManagerException(e);
        }

        return appConnectionId;
    }

//    public static void registerAsync(String host, int port, String username, String password, boolean isHttps, IKMFResponseHandler responseHandler, String storePassword) throws KMFRegistrationManagerException {
//        KMFLogonCoreListener listener = new KMFLogonCoreListener(KMFOperation.LOGIN, responseHandler, storePassword);
//        KMFRegistrationManager.logonCore.setLogonCoreListener(listener);
//
//        LogonCoreContext logonCoreContext = KMFRegistrationManager.logonCore.getLogonContext();
//        logonCoreContext.setHost(host);
//        logonCoreContext.setPort(port);
//        logonCoreContext.setHttps(isHttps);
//
//        LogonCore.UserCreationPolicy policy = LogonCore.UserCreationPolicy.automatic;
//        logonCoreContext.setUserCreationPolicy(policy);
//
//        try {
//            logonCoreContext.setBackendUser(username);
//            logonCoreContext.setBackendPassword(password);
//        } catch (Exception e) {
//            throw new KMFRegistrationManagerException(e);
//        }
//
//        KMFRegistrationManager.logonCore.register(logonCoreContext);
//    }
//


}
