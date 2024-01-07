package com.sap.stepbystep.kmf.data;

public class KMFConfigSettings {
    private Boolean mCheckNetworkBeforeMakeRequest;
    private Boolean mUseTechnicalUser = false;

    public KMFConfigSettings() {
    }

    /**
     * Get check network before make request.
     *
     * @return {@link Boolean}
     */
    public Boolean getCheckNetworkBeforeMakeRequest() {
        return mCheckNetworkBeforeMakeRequest == null || mCheckNetworkBeforeMakeRequest;
    }

    /**
     * Set check network before make request.
     *
     * @param checkNetworkBeforeMakeRequest check network before make request
     */
    public void setCheckNetworkBeforeMakeRequest(Boolean checkNetworkBeforeMakeRequest) {
        this.mCheckNetworkBeforeMakeRequest = checkNetworkBeforeMakeRequest;
    }

    /**
     * Get check use technical user.
     *
     * @return {@link Boolean}
     */
    public Boolean getUseTechnicalUser() {
        return mUseTechnicalUser == null || mUseTechnicalUser;
    }

    /**
     * Set check network before make request.
     *
     * @param useTechnicalUser check network before make request
     */
    public void setUseTechnicalUser(Boolean useTechnicalUser) {
        this.mUseTechnicalUser = useTechnicalUser;
    }
}

