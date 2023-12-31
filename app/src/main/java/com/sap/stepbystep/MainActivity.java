package com.sap.stepbystep;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sap.cloud.mobile.foundation.authentication.BasicAuthCredentialStore;
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.sap.cloud.mobile.foundation.common.SettingsParameters;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.user.UserInfo;
import com.sap.cloud.mobile.foundation.user.UserRoles;
import com.sap.maf.tools.logon.core.LogonCore;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.smp.client.supportability.ClientLogDestination;
import com.sap.smp.client.supportability.ClientLogLevel;
import com.sap.smp.client.supportability.ClientLogManager;
import com.sap.smp.client.supportability.ClientLogger;
import com.sap.smp.client.supportability.Supportability;
import com.sap.stepbystep.kmf.data.KMFPreferences;
import com.sap.stepbystep.smf.repository.SMFLoginRepository;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.Set;

import ch.qos.logback.classic.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnlineODataStore.OpenListener {

    protected static SharedPreferences.Editor mDefaultSharedPreferencesEditor = null;
    protected static SharedPreferences mDefaultSharedPreferences = null;

    public static final String TAG = MainActivity.class.getName();
    private OkHttpClient myOkHttpClient;
    private String deviceID;
    public static final String SERVICE_PROTOCOL = "http";
    public static final String SERVER = "nwgw.kctdata.cz";
    public static final String PORT = "80";
    public static final String SERVICE = "/sap/opu/odata/KCT/MA2_SRV;v=3";
    public static final String serviceURL = "http://nwgw.kctdata.cz";
    public static final String appID = "com.sap.stepbystep";
    public static final String connectionID = "/sap/opu/odata/KCT/MA2_SRV;v=3";
    private String messageToToast;
    private Toast toast;
    private String currentUser;
    //    Logger logger = (Logger) LoggerFactory.getLogger(MainActivity.class);
    private Integer numberOfPresses = 0;
    private final String myTag = "myDebuggingTag";
    public static final String PASS = "mAsset123!";
    public static final String USER = "mAsset123!";
    SharedPreferences.Editor editor;
    KMFPreferences preferences;

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


    private void toastAMessage(String msg) {
        if (toast != null && toast.getView().isShown()) {
            msg = messageToToast + "\n" + msg;
        } else {  //clear any previously shown toasts that have since stopped being displayed
            messageToToast = "";
        }
        messageToToast = msg;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(getApplicationContext(),
                        messageToToast,
                        Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void getUser() {
        Log.d(myTag, "In getUser");
        SettingsParameters sp = null;
        try {
            sp = new SettingsParameters(serviceURL, appID, deviceID, "1.0");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        UserRoles roles = new UserRoles(myOkHttpClient, sp);
        UserRoles.CallbackListener callbackListener = new UserRoles.CallbackListener() {
            @Override
            public void onSuccess(@NonNull UserInfo ui) {
                Log.d(myTag, "User Name: " + ui.getUserName());
                Log.d(myTag, "User Id: " + ui.getId());
                String[] roleList = ui.getRoles();
                Log.d(myTag, "User has the following Roles");
                assert roleList != null;
                for (String s : roleList) {
                    Log.d(myTag, "Role Name " + s);
                }
                currentUser = ui.getId();
                toastAMessage("Currently logged with " + ui.getId());
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                toastAMessage("UserRoles onFailure " + throwable.getMessage());
            }
        };

        roles.load(callbackListener);
    }

    private void enableButtonsOnRegister(final boolean enable) {
        final Button uploadLogButton = findViewById(R.id.b_uploadLog);
        final Button onlineODataButton = findViewById(R.id.b_odata);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                uploadLogButton.setEnabled(enable);
                onlineODataButton.setEnabled(enable);
            }
        });
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

    public static Set<String> getDefaultSharedPreferencesStringSet(String key, Set<String> defaultValue) {
        return mDefaultSharedPreferences.getStringSet(key, defaultValue);
    }

    public static void setDefaultSharedPreferences(String key, Set<String> value) {
        mDefaultSharedPreferencesEditor.putStringSet(key, value);
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
