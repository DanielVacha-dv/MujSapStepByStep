package com.sap.stepbystep.kmf.app;

public class KMFDevices {

    /**
     * Intermec
     * Scanner use barcode service.
     */
    public static final String MODEL_CN51 = "CN51";
    public static final String MODEL_CT40 = "CT40";
    public static final String MODEL_CT60 = "CT60";
//    protected static boolean mBarcodeServiceIsEnabled = false;
//    protected static com.intermec.aidc.BarcodeReader mBarcodeReader;
//    protected static com.intermec.aidc.VirtualWedge mVirtualWedge;

    /**
     * Honeywell
     * Scanner start in another activity. Scanned barcode will be returned in
     * {@code Activity.onActivityResult}({@link android.app.Activity#onActivityResult}).
     */
    public static final String MODEL_DOLPHIN_70E_BLACK = "Dolphin_70e_Black";
    public static final int MODEL_DOLPHIN_70E_BLACK_SCANNER_KEY_DOWN = 0;

    /**
     * Motorola
     * Scanner use broadcast receiver.
     * Data wedge must be configured.
     */
    public static final String MODEL_TC55 = "TC55";
    public static final String MODEL_ET1 = "ET1";
    public static final String MODEL_TC700H = "TC700H";
    public static final String MOTOROLA_INTENT_FILTER = "android.intent.action.SCANNER";
    public static final String MOTOROLA_DATA_STRING = "com.motorolasolutions.emdk.datawedge.data_string";
}
