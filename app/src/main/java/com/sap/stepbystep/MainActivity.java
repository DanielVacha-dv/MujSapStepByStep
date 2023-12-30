package com.sap.stepbystep;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.stepbystep.smf.repository.SMFLoginRepository;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
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
    Logger logger = (Logger) LoggerFactory.getLogger(MainActivity.class);
    private Integer numberOfPresses = 0;
    private final String myTag = "myDebuggingTag";
    public static final String PASS = "mAsset123!";
    public static final String USER = "mAsset123!";
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.debug("onCreate");
//        deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        setContentView(R.layout.activity_main);
        SharedPreferences name = getSharedPreferences("NAME", Context.MODE_PRIVATE);
    }

    public void onLogALine(View view) {
        Log.d(myTag, "In onLogALine");
        numberOfPresses = numberOfPresses + 1;
        Log.d(myTag, "Button pressed " + numberOfPresses + " times");
        logger.debug("In onLogALine " + numberOfPresses);
    }

    public void onRegister(View view) {
        SMFLoginRepository.login(this, this);
    }


    private void registerOrig() {
        Log.d(myTag, "In onRegister");
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        myOkHttpClient = builder
                .addInterceptor(new AppHeadersInterceptor(appID, deviceID, "1.0"))
                .authenticator(new BasicAuthDialogAuthenticator())
                .cookieJar(new WebkitCookieJar())
                .build();

        Request request = new Request.Builder()
                .get()
                .url(serviceURL + "/" + connectionID + "/")
                .build();

        Callback updateUICallback = new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) { //called if there is no network
                Log.d(myTag, "onFailure called during authentication " + e.getMessage());
                toastAMessage("Registration failed " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(myTag, "Successfully authenticated");
                    toastAMessage("Successfully authenticated");
                    enableButtonsOnRegister(true);
                    getUser();
                } else { //called if the credentials are incorrect
                    Log.d(myTag, "Registration failed " + response.networkResponse());
                    toastAMessage("Registration failed " + response.networkResponse());
                }
            }
        };

        myOkHttpClient.newCall(request).enqueue(updateUICallback);
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
        final Button uploadLogButton = (Button) findViewById(R.id.b_uploadLog);
        final Button onlineODataButton = (Button) findViewById(R.id.b_odata);
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
     * @param key
     * @param value
     */
    public static void setDefaultSharedPreferences(String key, String value) {
        mDefaultSharedPreferencesEditor.putString(key, value);
        mDefaultSharedPreferencesEditor.commit();
    }

    public static String getDefaultSharedPreferencesString(String key, String defaultValue) {
        return mDefaultSharedPreferences.getString(key, defaultValue);
    }
}
