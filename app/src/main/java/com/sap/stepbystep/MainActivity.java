package com.sap.stepbystep;

import android.os.Handler;
import android.os.Looper;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.sap.cloud.mobile.foundation.common.SettingsParameters;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.user.UserInfo;
import com.sap.cloud.mobile.foundation.user.UserRoles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient myOkHttpClient;
    private String deviceID;
    private final String serviceURL = "http://nwgw.kctdata.cz";  //change p1743065160
    private final String appID = "com.sap.stepbystep";
    private final String connectionID = "/sap/opu/odata/KCT/MA2_SRV;v=3";
    //    private final String connectionID = "com.sap.edm.sampleservice.v2";
    private String messageToToast;
    private Toast toast;
    private String currentUser;
    Logger logger = LoggerFactory.getLogger(MainActivity.class);
    private Integer numberOfPresses = 0;
    private final String myTag = "myDebuggingTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(myTag, "onCreate");
        logger.debug("onCreate");
        setContentView(R.layout.activity_main);
    }

    public void onLogALine(View view) {
        Log.d(myTag, "In onLogALine");
        numberOfPresses = numberOfPresses + 1;
        Log.d(myTag, "Button pressed " + numberOfPresses + " times");
        logger.debug("In onLogALine "+numberOfPresses);
    }

    public void onRegister(View view) {
        Log.d(myTag, "In onRegister");
        Log.d(myTag, "In onRegister");
        myOkHttpClient = new OkHttpClient.Builder()
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
                }
                else { //called if the credentials are incorrect
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
        }
        else {  //clear any previously shown toasts that have since stopped being displayed
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
        }
        catch (MalformedURLException e) {
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
                for (int i=0; i < roleList.length; i++) {
                    Log.d(myTag, "Role Name " + roleList[i]);
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
}
