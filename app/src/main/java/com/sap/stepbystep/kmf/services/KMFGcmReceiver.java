//package com.sap.stepbystep.kmf.services;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.sap.mobile.lib.request.IResponse;
//import com.sap.stepbystep.kmf.android.KMFApplication;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class KMFGcmReceiver extends WakefulBroadcastReceiver {
//    private static final String TAG = KMFGcmReceiver.class.getName();
//    private static final String REGISTER = "REGISTER";
//    private static final String REPEAT_INTERVAL = "REPEAT_INTERVAL";
//
//    private static final int MAXIMUM_REPEAT_INTERVAL = 86400; // 24h
//
//    private static String mSenderId = "";
//    private static ArrayList<String> mListeners = new ArrayList<String>();
//
//    private static int mRepeatInterval = 2;
//
//    /**
//     * Set GCM project ID (sender ID).
//     * Do not use directly, use assets/config.xml value GMC_SENDER_ID
//     *
//     * @param senderId Sender ID = project number in API console.
//     */
//    public static void setSenderId(String senderId) {
//        Log.d(TAG, "setSenderId " + senderId);
//        mSenderId = senderId;
//    }
//
//    /**
//     * Adds service to process GCM messages.
//     * Do not use directly, use assets/config.xml value GCM_LISTENER.
//     *
//     * @param className Service's class name.
//     */
//    public static void addListener(String className) {
//        Log.d(TAG, "GCM addListener " + className);
//        if (className.isEmpty())
//            return;
//        mListeners.add(className);
//    }
//
//    /**
//     * Register device on GCM server.
//     *
//     * @param context Application context.
//     */
//    public static void register(final Context context) {
//        if (mSenderId.isEmpty())
//            return;
//        new AsyncTask<Object, Void, Void>() {
//            @Override
//            protected Void doInBackground(Object... params) {
//                GoogleCloudMessaging gcm;
//                final Context context = (Context) (params[0]);
//
//                try {
//                    gcm = GoogleCloudMessaging.getInstance(context);
//                    String regid = gcm.register((String) params[1]);
//                    KMFApplication.setDefaultSharedPreferences(KMFSPConstants.SPC_GCM_ID, regid);
//                    KMFApplication.getLogger().i(TAG, "GCM device registered");
//                    Log.d(TAG, "GCM registration id=" + regid);
//
//                    if (KMFApplication.isUseSMP()) {
//                        KMFODataGCMRequest gcmRequest = new KMFODataGCMRequest(regid) {
//                            @Override
//                            public void processResponseSuccess(IResponse response) {
//                                KMFApplication.getLogger().i(TAG, "GCM ID registered on SMP");
//                                Log.d(TAG, "GCM ID registered on SMP");
//                            }
//
//                            @Override
//                            public void processResponseError(IResponse error, String errorMessage) {
//                                KMFApplication.getLogger().i(TAG, "Error when registering GCM ID on SMP: " + errorMessage);
//                                Log.d(TAG, "Error when registering GCM ID on SMP: " + errorMessage);
//                                repeatAction(context, true);
//                            }
//                        };
//                        gcmRequest.execute(context);
//                    }
//                } catch (IOException ex) {
//                    KMFApplication.getLogger().e(TAG, "GCM register error: " + ex.getMessage());
//
//                    // in case of SERVICE_NOT_AVAILABLE try again later
//                    if (ex.getMessage().equals(GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE)) {
//                        repeatAction(context, true);
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void v) {
//            }
//        }.execute(context, mSenderId);
//    }
//
//    /**
//     * Unregister device on GCM server.
//     *
//     * @param context Application context.
//     */
//    public static void unregister(final Context context) {
//        if (mSenderId.isEmpty())
//            return;
//        new AsyncTask<Object, Void, Void>() {
//            @Override
//            protected Void doInBackground(Object... params) {
//                GoogleCloudMessaging gcm;
//                try {
//                    gcm = GoogleCloudMessaging.getInstance((Context) (params[0]));
//                    gcm.unregister();
//                    KMFApplication.removeDefaultSharedPreferences(KMFSPConstants.SPC_GCM_ID);
//                    KMFApplication.getLogger().i(TAG, "GCM device unregistered");
//                } catch (IOException ex) {
//                    KMFApplication.getLogger().e(TAG, "GCM unregister error: " + ex.getMessage());
//
//                    // in case of SERVICE_NOT_AVAILABLE try again later
//                    if (ex.getMessage().equals(GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE)) {
//                        repeatAction(context, false);
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void v) {
//            }
//        }.execute(context);
//    }
//
//    private static void repeatAction(Context context, boolean register) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager == null) {
//            Log.e(TAG, "GCM repeat: AlarmManager not available");
//            return;
//        }
//
//        mRepeatInterval = Math.min(mRepeatInterval * 2, MAXIMUM_REPEAT_INTERVAL);
//
//        Intent intent = new Intent(context, KMFGcmReceiver.class);
//        intent.putExtra(REGISTER, register);
//        intent.putExtra(REPEAT_INTERVAL, mRepeatInterval);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mRepeatInterval * 1000, pendingIntent);
//        Log.d(TAG, "GCM repeat: Scheduled in " + mRepeatInterval + " s");
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
//        String messageType = gcm == null ? null : gcm.getMessageType(intent);
//        if (gcm != null && messageType != null && messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
//            for (String listener : mListeners) {
//                ComponentName comp = new ComponentName(context.getPackageName(),
//                        listener);
//                startWakefulService(context, intent.setComponent(comp));
//            }
//        } else {
//            boolean register = intent.getBooleanExtra(REGISTER, true);
//            mRepeatInterval = intent.getIntExtra(REPEAT_INTERVAL, -1);
//            if (mRepeatInterval > 0) {
//                if (register)
//                    register(context);
//                else
//                    unregister(context);
//            }
//        }
//    }
//}
//
