package com.sap.stepbystep;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.supportability.ClientLogDestination;
import com.sap.smp.client.supportability.ClientLogLevel;
import com.sap.smp.client.supportability.ClientLogManager;
import com.sap.smp.client.supportability.ClientLogger;
import com.sap.smp.client.supportability.Supportability;
import com.sap.stepbystep.smf.repository.SMFLoginRepository;

import java.util.EnumSet;

public class MainActivity extends AppCompatActivity implements OnlineODataStore.OpenListener {

    protected static SharedPreferences.Editor mDefaultSharedPreferencesEditor = null;
    protected static SharedPreferences mDefaultSharedPreferences = null;

    public static final String TAG = MainActivity.class.getName();
    public static final String SERVICE_PROTOCOL = "http";
    public static final String SERVER = "nwgw.kctdata.cz";
    public static final String PORT = "80";
    public static final String SERVICE = "/sap/opu/odata/KCT/MA2_SRV;v=3";
    private Integer numberOfPresses = 0;
    private final String myTag = "myDebuggingTag";
    public static final String PASS = "mAsset123!";
    public static final String USER = "vachad";

    private static ClientLogger logger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        getClientLogger(getApplicationContext());
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDefaultSharedPreferencesEditor = mDefaultSharedPreferences.edit();

    }


    public  ClientLogger getClientLogger(Context ctx) {
        if (logger == null) {
            LogonCore logonCore= LogonCore.getInstance();
            logonCore.init(ctx, ctx.getPackageName());
//            Supportability.getInstance().
            ClientLogManager logManager = Supportability.getInstance().getClientLogManager(ctx);
            logManager.setLogDestination(EnumSet.of(ClientLogDestination.CONSOLE, ClientLogDestination.FILESYSTEM));
            logger = logManager.getLogger("logger");
            logManager.setLogLevel(ClientLogLevel.INFO, "logger");
        }

        return logger;
    }

    public void onLogALine(View view) {
        Log.d(myTag, "In onLogALine");
        numberOfPresses = numberOfPresses + 1;
        Log.d(myTag, "Button pressed " + numberOfPresses + " times");
    }

    public void onRegister(View view) {
        SMFLoginRepository.login(this, this);
    }

    public void onUploadLog(View view) {
        Log.d(myTag, "In onUploadLog");
    }

    public void onOnlineOData(View view) {
        Log.d(myTag, "In onOnlineOData");
    }

    public void onOfflineOData(View view) {
        Log.d(myTag, "In onOfflineOData");
    }



    @Override
    public void storeOpened(OnlineODataStore onlineODataStore) {
        Log.d(TAG, "Store opened");
    }

    @Override
    public void storeOpenError(ODataException e) {
        Log.d(TAG, "Store storeOpenError");
    }

    public static void removeDefaultSharedPreferences(String key) {
        mDefaultSharedPreferencesEditor.remove(key);
        mDefaultSharedPreferencesEditor.commit();
    }

    /**
     * Save key with String value into the default shared preferences.
     *
     * @param key key
     * @param value value
     */
    public static void setDefaultSharedPreferences(String key, String value) {
        mDefaultSharedPreferencesEditor.putString(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    public static String getDefaultSharedPreferencesString(String key, String defaultValue) {
        return mDefaultSharedPreferences.getString(key, defaultValue);
    }
}
