package com.sap.stepbystep.kmf.data;

public class KMFConfigOData {
    private boolean mUseSMP = false;
    private boolean mUseJsonFormat = false;
    private boolean mUsePaging = false;
    private int mPagingSize = 100;
    private int mPagingListSize = 5000;
    private int mDeepInsertEntitiesCount = 100;
    private int mMaxThreads = 1;
    private boolean mUseOnlineStore;

    public KMFConfigOData() {
    }

    /**
     * Is used SMP?
     *
     * @return
     */
    public boolean useSMP() {
        return mUseSMP;
    }

    /**
     * Is used Online Store?
     *
     * @return
     */
    public boolean useOnlineStore() {
        return mUseOnlineStore;
    }

    /**
     * Set SMP use.
     *
     * @param useSMP
     */
    public void setUseSMP(boolean useSMP) {
        mUseSMP = useSMP;
    }

    /**
     * Set Online Store use.
     *
     * @param useOnlineStore
     */
    public void setUseOnlineStore(boolean useOnlineStore) {
        mUseOnlineStore = useOnlineStore;
    }

    /**
     * Is used json format?
     *
     * @return
     */
    public boolean useJsonFormat() {
        return mUseJsonFormat;
    }

    /**
     * Set use json format.
     *
     * @param useJsonFormat
     */
    public void setUseJsonFormat(boolean useJsonFormat) {
        mUseJsonFormat = useJsonFormat;
    }

    /**
     * Is used paging.
     *
     * @return
     */
    public boolean usePaging() {
        return mUsePaging;
    }

    /**
     * Set use paging.
     *
     * @param usePaging
     */
    public void setUsePaging(boolean usePaging) {
        mUsePaging = usePaging;
    }

    /**
     * Get paging size.
     *
     * @return
     */
    public int getPagingSize() {
        return mPagingSize;
    }

    /**
     * Set paging size.
     *
     * @param pagingSize
     */
    public void setPagingSize(int pagingSize) {
        mPagingSize = pagingSize;
    }

    /**
     * Get paging list size.
     *
     * @return
     */
    public int getPagingListSize() {
        return mPagingListSize;
    }


    /**
     * Set paging list size.
     *
     * @param pagingListSize
     */
    public void setPagingListSize(int pagingListSize) {
        mPagingListSize = pagingListSize;
    }

    /**
     * Get count of entities in deep insert.
     *
     * @return
     */
    public int getDeepInsertEntitiesCount() {
        return mDeepInsertEntitiesCount;
    }

    /**
     * Set count of entities in deep insert.
     *
     * @param deepInsertEntitiesCount
     */
    public void setDeepInsertEntitiesCount(int deepInsertEntitiesCount) {
        mDeepInsertEntitiesCount = deepInsertEntitiesCount;
    }

    /**
     * Get max threads.
     *
     * @return
     */
    public int getMaxThreads() {
        return mMaxThreads;
    }


    /**
     * Set max threads.
     *
     * @param maxThreads
     */
    public void setMaxThreads(int maxThreads) {
        mMaxThreads = maxThreads;
    }
}

