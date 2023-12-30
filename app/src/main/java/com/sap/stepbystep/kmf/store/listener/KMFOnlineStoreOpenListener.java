package com.sap.stepbystep.kmf.store.listener;

import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.online.OnlineODataStore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KMFOnlineStoreOpenListener implements OnlineODataStore.OpenListener {
    public static final int TIMEOUT = 30;
    private static KMFOnlineStoreOpenListener instance;

    private final CountDownLatch latch = new CountDownLatch(1);
    private OnlineODataStore store;
    private Exception error;

    public static KMFOnlineStoreOpenListener getInstance() {
        if (KMFOnlineStoreOpenListener.instance == null) {
            KMFOnlineStoreOpenListener.instance = new KMFOnlineStoreOpenListener();
        }

        return KMFOnlineStoreOpenListener.instance;
    }

    @Override
    public void storeOpened(OnlineODataStore onlineODataStore) {
        this.store = onlineODataStore;
        latch.countDown();
    }

    @Override
    public void storeOpenError(ODataException e) {
        this.error = e;
        latch.countDown();
    }

    public synchronized boolean finished() {
        return (this.store != null || this.error != null);
    }

    public synchronized Exception getError() {
        return this.error;
    }

    public synchronized OnlineODataStore getStore() {
        return this.store;
    }

    public void waitForCompletion() {
        try {
            if (!latch.await(KMFOnlineStoreOpenListener.TIMEOUT, TimeUnit.SECONDS))
                throw new IllegalStateException("Open listener was not called within 30 seconds.");
            else if (!finished())
                throw new IllegalStateException("Open listener is not in finished state after having completed successfully");
        } catch (InterruptedException e) {
            throw new IllegalStateException("Open listener waiting for results was interrupted.", e);
        }
    }

    public void setStore(OnlineODataStore store) {
        this.store = store;
    }
}
