package com.sap.stepbystep.kmf.reader;

import android.content.Context;
import android.util.Log;

public abstract class KMFReaderCN51 {
    private static final String TAG = KMFReaderCN51.class.getName();

    protected KMFReaderCN51Listener listener;

    /**
     * Vrátí obálku nad objekty skeneru CN51.
     * Pro různé verze Androidu jsou různě rozhraní, proto tato obálka.
     *
     * @param listener Listener pro události vyvolané skenerem
     * @return KMFReaderCN51 - obálka nad skenerem
     */
    public static KMFReaderCN51 createInstance(KMFReaderCN51Listener listener) {
        Class reader = null;

        try {
            reader = Class.forName("cz.kctdata.kmf.Reader.KMFReaderCN51A42");
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Reader cz.kctdata.kmf.Reader.KMFReaderCN51A42 not found");
        }

        if (reader == null) {
            try {
                reader = Class.forName("cz.kctdata.kmf.Reader.KMFReaderCN51A60");
            } catch (ClassNotFoundException e) {
                Log.w(TAG, "Reader cz.kctdata.kmf.Reader.KMFReaderCN51A60 not found");
            }
        }

        if (reader == null) {
            Log.e(TAG, "No implementation KMFReaderCN51 found.");
            return null;
        }

        try {
            Object instance = reader
                    .getConstructor(
                            new Class[]{KMFReaderCN51Listener.class}
                    )
                    .newInstance(
                            listener
                    );
            return (KMFReaderCN51) instance;
        } catch (Exception e) {
            Log.e(TAG, "Problem during instancing KMFReaderCN51", e);
            return null;
        }
    }

    public abstract void startBarcodeService(Context context, boolean enabled);

    public abstract void stopBarcodeService();

    public abstract void enableBarcodeReader();

    public abstract void disableBarcodeReader();

    public interface KMFReaderCN51Listener {
        void onSuccessScan(String barcodeScanResult);
    }
}




