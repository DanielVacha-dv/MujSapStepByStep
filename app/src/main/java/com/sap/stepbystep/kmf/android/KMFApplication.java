package com.sap.stepbystep.kmf.android;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
//import android.support.multidex.MultiDex;
import com.sap.mobile.lib.cache.Cache;
import com.sap.mobile.lib.cache.CacheException;
import com.sap.mobile.lib.cache.ICache;
import com.sap.mobile.lib.configuration.IPreferences;
import com.sap.mobile.lib.configuration.Preferences;
import com.sap.mobile.lib.configuration.PreferencesException;
import com.sap.mobile.lib.parser.IODataSchema;
import com.sap.mobile.lib.parser.IODataServiceDocument;
import com.sap.mobile.lib.parser.Parser;
import com.sap.mobile.lib.parser.ParserException;
import com.sap.mobile.lib.persistence.EncryptionKeyManager;
import com.sap.mobile.lib.persistence.PersistenceException;
import com.sap.mobile.lib.request.ConnectivityParameters;
import com.sap.mobile.lib.request.RequestManager;
import com.sap.mobile.lib.supportability.Logger;
import com.sap.smp.rest.ClientConnection;
import com.sap.stepbystep.kmf.KMFSPConstants;
import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.app.KMFDevices;
import com.sap.stepbystep.kmf.app.KMFXMLParser4Config;
import com.sap.stepbystep.kmf.data.KMFPreferences;
import com.sap.stepbystep.kmf.data.KMFUserData;
import com.sap.stepbystep.kmf.helpers.KMFHelperRootDetection;
import com.sap.stepbystep.kmf.helpers.KMFHelperRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class KMFApplication extends Application {
    protected static final String KMF_version = "0.1";
    /**
     * Config
     */
    protected static final String CONFIG_DIR = "config";
    /**
     * Device
     */
    protected static final String mDeviceModel = Build.MODEL;
    protected static String TAG = KMFApplication.class.getName();
    protected static KMFApplication mInstance;
    /**
     * SharedPreferences
     */
    protected static SharedPreferences mDefaultSharedPreferences = null;
    protected static SharedPreferences.Editor mDefaultSharedPreferencesEditor = null;
    protected static String CONFIG_FILE = "config.xml";
    protected static KMFXMLParser4Config mKMFConfig;
    protected static SharedPreferences mDeltaTokenSharedPreferences = null;
    protected static SharedPreferences.Editor mDeltaTokenSharedPreferencesEditor = null;
    protected static String DELTA_TOKEN_FILE = "delta_tokens";
    /**
     * OData
     */
    protected static Logger mLogger;
    protected static Preferences mPreferences;
    protected static Parser mParser;
    protected static Cache mCache;
    protected static RequestManager mRequestManager;
    private static KMFUserData mUserData;

    public KMFApplication() {
        super();

        mInstance = this;

        // Create logger
        this.mLogger = new Logger();

        //povleni pristupu k souborum na sd karte od A7
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    protected static KMFApplication getInstance() {
        return mInstance;
    }

    /**
     * Get config.
     *
     * @return
     */
    public static KMFXMLParser4Config getConfig() {
        return mKMFConfig;
    }

    /**
     * Get user data.
     *
     * @return KMFUserData
     */
    public static KMFUserData getUserData() {
        return mUserData;
    }

    /**
     * Set user data.
     *
     * @param userData user data
     */
    public static void setUserData(KMFUserData userData) {
        mUserData = userData;
    }

    /**
     * Return true/false whether the user is registered.
     *
     * @return
     */
    public static boolean isRegistrated() {
        if (isUseSMP()) {
            return (
                    (new KMFPreferences()).getApplicationConnectionId() != null
                            && getServiceDocument() != null
                            && getMetaDocument() != null
            );
        } else if (isUseTechnicalUser()) {
            return (getServiceDocument() != null && getMetaDocument() != null && getUserData().getUserPIN() != null);
        } else {
            return (getServiceDocument() != null && getMetaDocument() != null);
        }
    }

    /**
     * Return true/false whether the device is registered on GCM
     *
     * @return
     */
    public static boolean isGCMRegistered() {
        return getDefaultSharedPreferencesString(KMFSPConstants.SPC_GCM_ID, null) != null;
    }

    /**
     * Return true/false whether the SMP will be used
     *
     * @return boolean
     */
    public static boolean isUseSMP() {
        Boolean isPreferencesUseSMP = (new KMFPreferences()).isUseSMP();

        return (isPreferencesUseSMP != null)
                ? isPreferencesUseSMP
                : getConfig().getConfigOData().useSMP();

    }

    /**
     * Return true/false whether the technical user will be used
     *
     * @return boolean
     */
    public static boolean isUseTechnicalUser() {
        Boolean isUseTechUser = (new KMFPreferences()).isUseTechnicalUser();

        return (isUseTechUser != null)
                ? isUseTechUser
                : getConfig().getConfigSettings().getUseTechnicalUser();

    }

    /**
     * Get a Set<String> value for specified key from the default shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return {@link java.lang.String}
     */
    public static Set<String> getDefaultSharedPreferencesStringSet(String key, Set<String> defaultValue) {
        return mDefaultSharedPreferences.getStringSet(key, defaultValue);
    }

    /**
     * Get a String value for specified key from the default shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return {@link java.lang.String}
     */
    public static String getDefaultSharedPreferencesString(String key, String defaultValue) {
        return mDefaultSharedPreferences.getString(key, defaultValue);
    }

    /**
     * Get an int value for specified key from the default shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return int
     */
    public static int getDefaultSharedPreferencesInt(String key, int defaultValue) {
        return mDefaultSharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Get a boolean value for specified key from the default shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return boolean
     */
    public static boolean getDefaultSharedPreferencesBoolean(String key, boolean defaultValue) {
        return mDefaultSharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save key with Set<String> value into the default shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDefaultSharedPreferences(String key, Set<String> value) {
        mDefaultSharedPreferencesEditor.putStringSet(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    /**
     * Save key with String value into the default shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDefaultSharedPreferences(String key, String value) {
        mDefaultSharedPreferencesEditor.putString(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    /**
     * Save key with int value into the default shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDefaultSharedPreferences(String key, int value) {
        mDefaultSharedPreferencesEditor.putInt(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    /**
     * Save key and boolean value into the default shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDefaultSharedPreferences(String key, boolean value) {
        mDefaultSharedPreferencesEditor.putBoolean(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    /**
     * Remove key with his value from the default shared preferences.
     *
     * @param key
     */
    public static void removeDefaultSharedPreferences(String key) {
        mDefaultSharedPreferencesEditor.remove(key);
        mDefaultSharedPreferencesEditor.commit();
    }

    //Delta token shared preferences

    /**
     * Get a Set<String> value for specified key from the delta token shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return {@link java.lang.String}
     */
    public static Set<String> getDeltaTokenSharedPreferencesStringSet(String key, Set<String> defaultValue) {
        return mDeltaTokenSharedPreferences.getStringSet(key, defaultValue);
    }

    /**
     * Get a String value for specified key from the delta token shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return {@link java.lang.String}
     */
    public static String getDeltaTokenSharedPreferencesString(String key, String defaultValue) {
        return mDeltaTokenSharedPreferences.getString(key, defaultValue);
    }

    /**
     * Get an int value for specified key from the delta token shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return int
     */
    public static int getDeltaTokenSharedPreferencesInt(String key, int defaultValue) {
        return mDeltaTokenSharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Get a boolean value for specified key from the delta token shared preferences.
     *
     * @param key
     * @param defaultValue
     * @return boolean
     */
    public static boolean getDeltaTokenSharedPreferencesBoolean(String key, boolean defaultValue) {
        return mDeltaTokenSharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save key with Set<String> value into the delta token shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDeltaTokenSharedPreferences(String key, Set<String> value) {
        mDeltaTokenSharedPreferencesEditor.putStringSet(key, value);
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Save key with String value into the delta token shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDeltaTokenSharedPreferences(String key, String value) {
        mDeltaTokenSharedPreferencesEditor.putString(key, value);
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Save key with int value into the delta token shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDeltaTokenSharedPreferences(String key, int value) {
        mDeltaTokenSharedPreferencesEditor.putInt(key, value);
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Save key and boolean value into the delta token shared preferences.
     *
     * @param key
     * @param value
     */
    public static void setDeltaTokenSharedPreferences(String key, boolean value) {
        mDeltaTokenSharedPreferencesEditor.putBoolean(key, value);
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Remove all keys from the delta token shared preferences.
     */
    public static void removeDeltaTokenSharedPreferences(String key) {
        mDeltaTokenSharedPreferencesEditor.remove(key);
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Remove all keys from the delta token shared preferences.
     */
    public static void clearDeltaTokenSharedPreferences() {
        mDeltaTokenSharedPreferencesEditor.clear();
        mDeltaTokenSharedPreferencesEditor.commit();
    }

    /**
     * Get logger.
     *
     * @return mLogger
     */
    public static Logger getLogger() {
        return mLogger;
    }

    /**
     * Get preferences.
     *
     * @return mPreferences
     */
    public static Preferences getPreferences() {
        return mPreferences;
    }

    /**
     * Get parser.
     *
     * @return mParser
     */
    public static Parser getParser() {
        return mParser;
    }

    /**
     * Get cache.
     *
     * @return mCache
     */
    public static Cache getCache() {
        return mCache;
    }

    /**
     * Get request manager.
     *
     * @return mRequestManager
     */
    public static RequestManager getRequestManager() {
        if (mRequestManager == null) {
            mRequestManager = new RequestManager(
                    KMFApplication.getLogger()
                    , KMFApplication.getPreferences()
                    , getConnectivityParameters()
                    , getConfig().getConfigOData().getMaxThreads()
            );
        }
        return mRequestManager;
    }

    /**
     * Get connectivity parameters.
     *
     * @return ConnectivityParameters
     */
    protected static ConnectivityParameters getConnectivityParameters() {
        KMFPreferences preferences = new KMFPreferences();
        ConnectivityParameters connectivityParameters = new ConnectivityParameters();
        connectivityParameters.setUserName(preferences.getUser());
        connectivityParameters.setUserPassword(preferences.getPassword());
        connectivityParameters.setLanguage(preferences.getLanguage());
        connectivityParameters.enableXsrf(true);
        return connectivityParameters;
    }

    /**
     * Get client connection.
     *
     * @param context Context
     * @return ClientConnection
     */
    public static ClientConnection getClientConnection(Context context) {
        KMFPreferences preferences = new KMFPreferences();
        ClientConnection clientConnection = new ClientConnection(
                context
                , preferences.getService()
                , null
                , null
                , getRequestManager()
        );
        clientConnection.setConnectionProfile(
                true
                , preferences.getServer()
                , preferences.getPort()
                , null
                , null
        );
        return clientConnection;
    }

    /**
     * Terminate request manager.
     */
    public static void terminateRequestManager() {
        if (mRequestManager == null) {
            return;
        }

        mRequestManager.terminate();
    }

    /**
     * Clean request manager.
     */
    public static void cleanRequestManager() {
        if (mRequestManager != null) {
            mRequestManager.terminate();
        }

        mRequestManager = null;
    }

    /**
     * Get service document from cache.
     */
    public static IODataServiceDocument getServiceDocument() {
        try {
            return ((IODataServiceDocument) mCache.readDocument(ICache.DocumentType.ServiceDocument, IODataServiceDocument.class.getSimpleName()));
        } catch (CacheException e) {
            getLogger().e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    public static boolean hasDeviceScanner() {
        return getDeviceModel().contains(KMFDevices.MODEL_CN51) ||
                getDeviceModel().contains(KMFDevices.MODEL_DOLPHIN_70E_BLACK) ||
                getDeviceModel().contains(KMFDevices.MODEL_ET1) ||
                getDeviceModel().contains(KMFDevices.MODEL_TC55) ||
                getDeviceModel().contains(KMFDevices.MODEL_TC700H);
    }

    /**
     * Get meta data from cache.
     */
    public static IODataSchema getMetaDocument() {
        try {
            return ((IODataSchema) mCache.readDocument(ICache.DocumentType.MetaDocument, IODataSchema.class.getSimpleName()));
        } catch (CacheException e) {
            getLogger().e(TAG, e.getLocalizedMessage());
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get device model.
     *
     * @return device model
     */
    public static String getDeviceModel() {
        return mDeviceModel;
    }

    /**
     * Clear data. Same functionality like like button in:
     * "Settings"->"Apps"->"this application"->"Clear data".
     */
    public static void clearData() {
        File cache = KMFApplication.getInstance().getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] childrens = appDir.list();
            for (String children : childrens) {
                if (!children.equals("lib")) {
                    deleteDir(new File(appDir, children));
                }
            }
        }
    }

    /**
     * Delete directory.
     *
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate()");

        if (KMFHelperRootDetection.isDeviceRooted(getApplicationContext())) {
            throw new RuntimeException("The device is rooted!");
        }

        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDefaultSharedPreferencesEditor = mDefaultSharedPreferences.edit();

        mDeltaTokenSharedPreferences = getApplicationContext().getSharedPreferences(DELTA_TOKEN_FILE, Context.MODE_PRIVATE);
        mDeltaTokenSharedPreferencesEditor = mDeltaTokenSharedPreferences.edit();

        String encryptionKey = getDefaultSharedPreferencesString(KMFSPConstants.SPC_ENCRYPTION_KEY_OFFLINE, null);

        try {
            // If encryption key is empty, then generates an new key
            if (TextUtils.isEmpty(encryptionKey)) {
                encryptionKey = EncryptionKeyManager.getEncryptionKey(getApplicationContext());
                // Storing new encryption key in share preferences
                setDefaultSharedPreferences(KMFSPConstants.SPC_ENCRYPTION_KEY_OFFLINE, encryptionKey);
            } else {
                // Setting the stored encryption key
                EncryptionKeyManager.setEncryptionKey(encryptionKey, getApplicationContext());
            }
        } catch (PersistenceException e) {
            mLogger.e(TAG, e.getLocalizedMessage());
        }

        File configFile = new File(getExternalFilesDir(CONFIG_DIR), CONFIG_FILE);
        try {
            mKMFConfig = new KMFXMLParser4Config(getAssets().open(CONFIG_FILE));

            if (configFile.exists()) {
                mKMFConfig = new KMFXMLParser4Config(new FileInputStream(configFile));
            }

        } catch (IOException e) {
            mLogger.e(TAG, e.getLocalizedMessage());
        }

        // Create preferences
        try {
            mPreferences = new Preferences(getApplicationContext(), mLogger);
            if (mKMFConfig.getConfigPreferences().getPersistenceSecureMode() != null) {
                mPreferences.setBooleanPreference(
                        IPreferences.PERSISTENCE_SECUREMODE
                        , mKMFConfig.getConfigPreferences().getPersistenceSecureMode()
                );
            }

            if (mKMFConfig.getConfigPreferences().getConnectivityHttpPort() != null) {
                mPreferences.setIntPreference(
                        IPreferences.CONNECTIVITY_HTTP_PORT
                        , mKMFConfig.getConfigPreferences().getConnectivityHttpPort()
                );
            }

            if (mKMFConfig.getConfigPreferences().getConnectivityConnTimeOut() != null) {
                mPreferences.setIntPreference(
                        IPreferences.CONNECTIVITY_CONNTIMEOUT
                        , mKMFConfig.getConfigPreferences().getConnectivityConnTimeOut()
                );
            }

            if (mKMFConfig.getConfigPreferences().getConnectivitySconnTimeOut() != null) {
                mPreferences.setIntPreference(
                        IPreferences.CONNECTIVITY_SCONNTIMEOUT
                        , mKMFConfig.getConfigPreferences().getConnectivitySconnTimeOut());
            }

            if (mKMFConfig.getConfigPreferences().getLogLevel() != null) {
                mPreferences.setIntPreference(
                        IPreferences.LOG_LEVEL
                        , mKMFConfig.getConfigPreferences().getLogLevel());
            }

        } catch (PreferencesException e) {
            mLogger.e(TAG, e.getLocalizedMessage());
        }

        // Create parser
        try {
            this.mParser = new Parser(mPreferences, mLogger);
        } catch (ParserException e) {
            this.mLogger.e(TAG, e.getLocalizedMessage());
        }

        // Create cache
        this.mCache = new Cache(getApplicationContext(), mLogger);
        this.mCache.initializeCache();

        if (mKMFConfig.getConfigSettings().getUseTechnicalUser()) {
            mUserData = new KMFUserData();
        }

        //Create handler for uncought exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.d(TAG, "uncaughtException(Thread thread, Throwable throwable)");
                handleUncaughtException(thread, throwable);
                System.exit(0);
            }
        });
    }

    public void handleUncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG, "handleUncaughtException (Thread thread, Throwable throwable)", throwable);
        saveLogCat();
        KMFPreferences preferences = new KMFPreferences();
        preferences.setCrashedLogCreated(true);
        preferences.save();
    }

    protected void saveLogCat() {
        String fileName = KMFAppConstants.CRASHED_LOG_NAME + KMFAppConstants.LOG_FILE_SUFFIX;

        if (KMFApplication.getConfig().getConfigSettings().getUseTechnicalUser()) {
            addLogInfo(
                    TAG,
                    "Log vytvořil uživatel %1$s." +
                            getUserData().getUserName()
            );

        } else {
            addLogInfo(
                    TAG,
                    "Log vytvořil uživatel %1$s." +
                            (new KMFPreferences()).getUser()
            );
        }

        addLogInfo(
                TAG,
                "Seriové číslo " +
                        KMFFragmentActivity.getBuildSerial(this)
        );

        addLogInfo(
                TAG,
                "Verze OS %1$s." +
                        KMFFragmentActivity.getBuildVersionRelease(this)
        );
        addLogInfo(
                TAG,
                "Verze aplikace %1$s." +
                        KMFFragmentActivity.getPackageVersionName(this)
        );

        KMFHelperRuntime.execLogcat(
                new File(
                        getExternalFilesDir(KMFAppConstants.DIR_LOGCAT),
                        fileName
                )
        );
    }

    protected void addLogInfo(String tag, String logMessage) {
        getLogger().i(tag, logMessage);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "onTerminate()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.i(TAG, "onConfigurationChanged()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Log.i(TAG, "onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.i(TAG, "onTrimMemory()");
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        super.registerComponentCallbacks(callback);
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        super.unregisterComponentCallbacks(callback);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
    }

    /**
     * Remove session cookies.
     */
    public void removeSessionCookie() {
        CookieManager.getInstance().removeSessionCookie();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }
}
