package com.sap.stepbystep.kmf.store;

import com.sap.smp.client.httpc.authflows.UsernamePasswordProvider;
import com.sap.smp.client.httpc.authflows.UsernamePasswordToken;
import com.sap.smp.client.httpc.events.IReceiveEvent;
import com.sap.smp.client.httpc.events.ISendEvent;

public class KMFCredentialsProviderGW implements UsernamePasswordProvider {
    private static KMFCredentialsProviderGW instance;
    private final String mUsername;
    private final String mPassword;

    private KMFCredentialsProviderGW(String username, String password) {
        this.mUsername = username;
        this.mPassword = password;
    }
    public static KMFCredentialsProviderGW getInstance() {
        return KMFCredentialsProviderGW.instance;
    }

    public static KMFCredentialsProviderGW getInstance(String username, String password) {
        if(KMFCredentialsProviderGW.instance == null) {
            KMFCredentialsProviderGW.instance = new KMFCredentialsProviderGW(username, password);
        }

        return KMFCredentialsProviderGW.instance;
    }

    @Override
    public Object onCredentialsNeededUpfront(ISendEvent iSendEvent) {
        return new UsernamePasswordToken(this.mUsername, this.mPassword);
    }

    @Override
    public Object onCredentialsNeededForChallenge(IReceiveEvent iReceiveEvent) {
        return new UsernamePasswordToken(this.mUsername, this.mPassword);
    }
}
