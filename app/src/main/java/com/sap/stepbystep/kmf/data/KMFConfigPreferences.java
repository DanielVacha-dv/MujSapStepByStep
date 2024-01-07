package com.sap.stepbystep.kmf.data;

public class KMFConfigPreferences {
    private Boolean mPersistenceSecureMode;
    private Integer mConnectivityHttpPort;
    private Integer mConnectivityConnTimeOut;
    private Integer mConnectivitySconnTimeOut;
    private Integer mLogLevel;

    public KMFConfigPreferences() {
    }

    /**
     * Get persistence secure mode.
     *
     * @return {@link Boolean}
     */
    public Boolean getPersistenceSecureMode() {
        return mPersistenceSecureMode;
    }

    /**
     * Set persistence secure mode.
     *
     * @param persistenceSecureMode
     */
    public void setPersistenceSecureMode(boolean persistenceSecureMode) {
        mPersistenceSecureMode = persistenceSecureMode;
    }

    /**
     * Get connectivity http port.
     *
     * @return {@link java.lang.Integer}
     */
    public Integer getConnectivityHttpPort() {
        return mConnectivityHttpPort;
    }

    /**
     * Set connectivity http port.
     *
     * @param connectivityHttpPort
     */
    public void setConnectivityHttpPort(int connectivityHttpPort) {
        mConnectivityHttpPort = connectivityHttpPort;
    }

    /**
     * Get connectivity connection time out.
     *
     * @return {@link java.lang.Integer}
     */
    public Integer getConnectivityConnTimeOut() {
        return mConnectivityConnTimeOut;
    }

    /**
     * Set connectivity connection time out.
     *
     * @param connectivityConnTimeOut
     */
    public void setConnectivityConnTimeOut(int connectivityConnTimeOut) {
        mConnectivityConnTimeOut = connectivityConnTimeOut;
    }

    /**
     * Get connectivity socket connection time out.
     *
     * @return {@link java.lang.Integer}
     */
    public Integer getConnectivitySconnTimeOut() {
        return mConnectivitySconnTimeOut;
    }

    /**
     * Set connectivity socket connection time out.
     *
     * @param connectivitySconnTimeOut
     */
    public void setConnectivitySconnTimeOut(int connectivitySconnTimeOut) {
        mConnectivitySconnTimeOut = connectivitySconnTimeOut;
    }

    /**
     * Get log level.
     *
     * @return {@link java.lang.Integer}
     */
    public Integer getLogLevel() {
        return mLogLevel;
    }

    /**
     * Set log level.
     *
     * @param logLevel
     */
    public void setLogLevel(int logLevel) {
        mLogLevel = logLevel;
    }
}
