package com.sap.stepbystep.kmf.data;

import com.sap.stepbystep.kmf.KMFSPConstants;
import com.sap.stepbystep.kmf.android.KMFApplication;

import java.util.HashMap;
import java.util.Map;

public class KMFBaseUserData {
    public static final String FUNCTION_IMPORT_NAME = "Login";

    public static final String USER_NAME = "UserName";
    public static final String USER_PIN = "UserPIN";

    protected String mUserName;
    protected String mUserPIN;

    public KMFBaseUserData() {
        setUserName(KMFApplication.getDefaultSharedPreferencesString(KMFSPConstants.SPC_USER_NAME, null));
        setUserPIN(KMFApplication.getDefaultSharedPreferencesString(KMFSPConstants.SPC_USER_PIN, null));
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        setUserName(userName, false);
    }

    public void setUserName(String userName, boolean save) {
        this.mUserName = userName;

        if (save)
            saveUserName();
    }

    public String getUserPIN() {
        return mUserPIN;
    }

    public void setUserPIN(String userPIN) {
        setUserPIN(userPIN, false);
    }

    public void setUserPIN(String userPIN, boolean save) {
        this.mUserPIN = userPIN;

        if (save)
            saveUserPIN();
    }

    private void saveUserPIN() {
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_USER_PIN, mUserPIN);
    }

    private void saveUserName() {
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_USER_NAME, mUserName);
    }

    public void save() {
        saveUserName();
        saveUserPIN();
    }

    public Map<String, String> getRequestHeaders() {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(USER_NAME, mUserName);
        requestHeaders.put(USER_PIN, mUserPIN);
        return requestHeaders;
    }
}

