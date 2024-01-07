package com.sap.stepbystep.kmf.data;

import com.sap.stepbystep.kmf.KMFSPConstants;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.app.KMFSecurity;

public class KMFPreferences {

    private String mProfileId, mUseSMP, mUseTechnicalUser, mProtocol, mServer, mPort, mClient, mLanguage, mService, mUser, mPassword, mApplicationConnectionId, mSigned;
    private String mCrashedLogCreated;

    public KMFPreferences() {
        getPreferencesFromSharedPreferences();
    }

    /**
     * Get data from shared preferences.
     */
    protected void getPreferencesFromSharedPreferences() {
        mProfileId = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_PROFILE, null);
        mUseSMP = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_USE_SMP, null);
        mUseTechnicalUser = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_USE_TECHNICAL_USER, null);
        mProtocol = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_PROTOCOL, null);
        mServer = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_SERVER, null);
        mPort = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_PORT, null);
        mClient = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_CLIENT, null);
        mLanguage = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_LANGUAGE, null);
        mService = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_SERVICE, null);
        mUser = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_USER, null);
        mPassword = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_PASSWORD, null);
        mApplicationConnectionId = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_AAP_CONN_ID, null);
        mSigned = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_SIGNED, null);
        mCrashedLogCreated = getPreferenceValueFromSharedPreferences(KMFSPConstants.SPC_CRASHED_LOG_CREATED, null);
    }

    /**
     * Get data from shared prefereneces using predefined values.
     * If data from shared preferences is null or empty, predefined value is returned.
     *
     * @param key             The SP key.
     * @param predefinedValue The predefined value.
     * @return The SP value for the specified key.
     */
    protected String getPreferenceValueFromSharedPreferences(String key, String predefinedValue) {
        String value = KMFApplication.getDefaultSharedPreferencesString(key, predefinedValue);
        if (value == null || value.isEmpty())
            return predefinedValue;
        else
            return KMFSecurity.decrypt(value);
    }

    /**
     * Get profile ID.
     *
     * @return The profile ID.
     */
    public String getProfileId() {
        return mProfileId;
    }

    /**
     * Set profile ID.
     *
     * @param profileId The profile ID.
     */
    public void setProfileId(String profileId) {
        mProfileId = profileId;
    }

    /**
     * Is NWGW/SMP used.
     *
     * @return The SMP usage.
     */
    public Boolean isUseSMP() {
        return (mUseSMP == null) ? null : Boolean.valueOf(mUseSMP);
    }

    /**
     * Is technical user used.
     *
     * @return The technical user usage.
     */
    public Boolean isUseTechnicalUser() {
        return (mUseTechnicalUser == null) ? null : Boolean.valueOf(mUseTechnicalUser);
    }

    /**
     * Set NWGW/SMP usage.
     *
     * @param useSMP The SMP usage.
     */
    public void setUseSMP(Boolean useSMP) {
        if (useSMP != null)
            mUseSMP = useSMP.toString();
    }

    /**
     * Set tech user usage.
     *
     * @param useTechnicalUser The technical user usage.
     */
    public void setUseTechnicalUser(Boolean useTechnicalUser) {
        if (useTechnicalUser != null)
            mUseTechnicalUser = useTechnicalUser.toString();
    }

    /**
     * Get protocol (http/https).
     *
     * @return The protocol.
     */
    public String getProtocol() {
        return mProtocol;
    }

    /**
     * Set protocol (http/https).
     *
     * @param protocol The protocol.
     */
    public void setProtocol(String protocol) {
        mProtocol = protocol;
    }

    /**
     * Get NWGW/SMP server.
     *
     * @return The server.
     */
    public String getServer() {
        return mServer;
    }

    /**
     * Set NWGW/SMP server.
     *
     * @param server The server.
     */
    public void setServer(String server) {
        mServer = server;
    }

    /**
     * Get NWGW/SMP port.
     *
     * @return The port.
     */
    public String getPort() {
        return mPort;
    }

    /**
     * Set NWGW/SMP port.
     *
     * @param port The port.
     */
    public void setPort(String port) {
        mPort = port;
    }

    /**
     * Get NWGW service/SMP application.
     *
     * @return The NWGW service/SMP application.
     */
    public String getService() {
        return mService;
    }

    /**
     * Set NWGW service/SMP application.
     *
     * @param service The NWGW service/SMP application.
     */
    public void setService(String service) {
        mService = service;
    }

    /**
     * Get client.
     *
     * @return The client.
     */
    public String getClient() {
        return mClient;
    }

    /**
     * Set client.
     *
     * @param client The client.
     */
    public void setClient(String client) {
        mClient = client;
    }

    /**
     * Get language.
     *
     * @return The language.
     */
    public String getLanguage() {
        return mLanguage;
    }

    /**
     * Set language.
     *
     * @param language The language.
     */
    public void setLanguage(String language) {
        mLanguage = language;
    }

    /**
     * Get user.
     *
     * @return The user.
     */
    public String getUser() {
        return mUser;
    }

    /**
     * Set user.
     *
     * @param user The user.
     */
    public void setUser(String user) {
        mUser = user;
    }

    /**
     * Get password.
     *
     * @return The password.
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Set password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    /**
     * Get application connection ID.
     *
     * @return The application connection ID.
     */
    public String getApplicationConnectionId() {
        return mApplicationConnectionId;
    }

    /**
     * Set application connection ID.
     *
     * @param applicationConnectionId The application connection ID.
     */
    public void setApplicationConnectionId(String applicationConnectionId) {
        mApplicationConnectionId = applicationConnectionId;
    }

    /**
     * Get signed.
     *
     * @return if the user is signed
     */
    public String getSigned() {
        return mSigned;
    }

    /**
     * Set signed on true or false.
     *
     * @param signed if the user is signed
     */
    public void setSigned(Boolean signed) {
        if (signed != null)
            mSigned = signed.toString();
    }

    /**
     * Is user signed.
     *
     * @return true or false
     */
    public Boolean isSigned() {
        return (mSigned == null) ? false : Boolean.valueOf(mSigned);
    }

    /**
     * Set crashed log created.
     *
     * @param crashedLogCreated
     */
    public void setCrashedLogCreated(Boolean crashedLogCreated) {
        mCrashedLogCreated = crashedLogCreated.toString();
    }

    /**
     * Get crashed log created.
     *
     * @return
     */
    public String getCrashedLogCreated() {
        return mCrashedLogCreated;
    }

    /**
     * Is crashed log created.
     *
     * @return
     */
    public Boolean isCrashedLogCreated() {
        return (mCrashedLogCreated == null) ? false : Boolean.valueOf(mCrashedLogCreated);
    }

    /**
     * Save data to shared preferences using key and value.
     */
    public void save() {
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_SERVICE, KMFSecurity.encrypt(mService));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_PORT, KMFSecurity.encrypt(mPort));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_USER, KMFSecurity.encrypt(mUser));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_CLIENT, KMFSecurity.encrypt(mClient));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_PROTOCOL, KMFSecurity.encrypt(mProtocol));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_AAP_CONN_ID, KMFSecurity.encrypt(mApplicationConnectionId));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_PROFILE, KMFSecurity.encrypt(mProfileId));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_PASSWORD, KMFSecurity.encrypt(mPassword));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_LANGUAGE, KMFSecurity.encrypt(mLanguage));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_SERVER, KMFSecurity.encrypt(mServer));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_USE_SMP, KMFSecurity.encrypt(mUseSMP));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_SIGNED, KMFSecurity.encrypt(mSigned));
        KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_CRASHED_LOG_CREATED, KMFSecurity.encrypt(mCrashedLogCreated));
    }

    /**
     * Remove data from shared preferences using key.
     */
    public void delete() {
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_SERVICE);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_PORT);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_USER);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_CLIENT);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_PROTOCOL);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_AAP_CONN_ID);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_PROFILE);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_PASSWORD);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_LANGUAGE);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_SERVER);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_USE_SMP);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_SIGNED);
        KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_CRASHED_LOG_CREATED);
        getPreferencesFromSharedPreferences();
    }
}
