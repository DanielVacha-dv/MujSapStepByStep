package com.sap.stepbystep.kmf.odata;

import com.sap.stepbystep.kmf.app.KMFAppConstants;

public class KMFODataRequest {
    private KMFODataEntity mKMFODataEntity;
    private KMFODataRequestKeys mKMFODataRequestKeys;
    private KMFODataRequestFilter mKMFODataRequestFilter;

    public KMFODataRequest(KMFODataEntity entity, KMFODataRequestKeys requestKeys, KMFODataRequestFilter requestFilter) {
        this.mKMFODataEntity = entity;
        this.mKMFODataRequestKeys = requestKeys;
        this.mKMFODataRequestFilter = requestFilter;
    }

    /**
     * Get request URL.
     *
     * @return
     */
    public String getRequestURL() {
        if (this.mKMFODataRequestKeys == null && this.mKMFODataRequestFilter == null)
            return getRequestURL(this.mKMFODataEntity);
        else if (this.mKMFODataRequestKeys != null)
            return getRequestURL(this.mKMFODataEntity, this.mKMFODataRequestKeys);
        else if (this.mKMFODataRequestFilter != null)
            return getRequestURL(this.mKMFODataEntity, this.mKMFODataRequestFilter);
        return (new String(""));
    }

    /**
     * Get entity.
     *
     * @return
     */
    public KMFODataEntity getEntity() {
        return this.mKMFODataEntity;
    }

    /**
     * Get request URL.
     *
     * @param entity
     * @return
     */
    private String getRequestURL(KMFODataEntity entity) {
        if (entity.isFunctionImport())
            return (new String("")).concat(KMFAppConstants.SLASH).concat(entity.getFunctionImportName());
        else
            return (new String("")).concat(KMFAppConstants.SLASH).concat(entity.getCollectionId());
    }

    /**
     * Get request URL.
     *
     * @param entity
     * @param requestKeys
     * @return
     */
    private String getRequestURL(KMFODataEntity entity, KMFODataRequestKeys requestKeys) {
        boolean isImportFunction = entity.isFunctionImport();
        if (isImportFunction)
            return (new String("")).concat(KMFAppConstants.SLASH).concat(entity.getFunctionImportName()).concat(requestKeys.generateKeysString4Request(isImportFunction));
        else
            return (new String("")).concat(KMFAppConstants.SLASH).concat(entity.getCollectionId()).concat(requestKeys.generateKeysString4Request(isImportFunction));
    }

    /**
     * Get request URL.
     *
     * @param entity
     * @param requestFilter
     * @return
     */
    private String getRequestURL(KMFODataEntity entity, KMFODataRequestFilter requestFilter) {
        return (new String("")).concat(KMFAppConstants.SLASH).concat(entity.getCollectionId()).concat(requestFilter.generateFilterString4Request());
    }
}
