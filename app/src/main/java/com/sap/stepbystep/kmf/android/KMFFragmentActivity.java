package com.sap.stepbystep.kmf.android;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.honeywell.scanintent.ScanIntent;
import com.sap.stepbystep.kmf.app.KMFDevices;
import com.sap.stepbystep.kmf.reader.KMFReaderCN51;
import com.sap.stepbystep.kmf.ui.KMFDialogAlert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KMFFragmentActivity
        extends FragmentActivity implements KMFReaderCN51.KMFReaderCN51Listener
        /*implements BarcodeReadListener*/ {

    public static final int REQUEST_CODE_NOTIFICATION = 2;
    public static final int REQUEST_CODE_CAMERA = 0;
    public static final int REQUEST_CODE_FILE_GET = 1;
    protected static String TAG = KMFFragmentActivity.class.getName();
    private KMFReaderCN51 mReaderCN51;
    private Handler mHandler = new Handler();
    private BroadcastReceiver mBroadcastReceiver;
    private String mCN51BarcodeData;
    /**
     * Return barcode scan result for device Intermec.
     */
    final Runnable barCodeScantResult = new Runnable() {
        public void run() {
            onSuccessBarcodeScanResult(mCN51BarcodeData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /**
         * Start activity for device Honeywell to start barcode scanner reader.
         */
        if (KMFApplication.getDeviceModel().equals(KMFDevices.MODEL_DOLPHIN_70E_BLACK) && keyCode == KMFDevices.MODEL_DOLPHIN_70E_BLACK_SCANNER_KEY_DOWN) {
            Intent scanIntent = new Intent(ScanIntent.SCAN_ACTION);
            scanIntent.addCategory(Intent.CATEGORY_DEFAULT);
            scanIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            scanIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            scanIntent.putExtra(ScanIntent.EXTRA_SCAN_MODE, ScanIntent.SCAN_MODE_RESULT_AS_URI);
            this.startActivityForResult(scanIntent, ScanIntent.SCAN_MODE_RESULT_AS_URI);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    onCameraSuccess();
                } else {
                    onCameraFailure();
                }
                break;
            case REQUEST_CODE_FILE_GET:
                if (resultCode == RESULT_OK) {
                    onSuccessFileGet(data.getData().getPath());
                }
                break;
            case ScanIntent.SCAN_MODE_RESULT_AS_URI:
                if (data != null) {
                    onSuccessBarcodeScanResult(data.getStringExtra(ScanIntent.EXTRA_RESULT_BARCODE_DATA));
                }
                break;
            default:
        }
    }

//    @Override
//    public void barcodeRead(BarcodeReadEvent barcodeReadEvent) {
//        mCN51BarcodeData = barcodeReadEvent.getBarcodeData();
//        mHandler.post(barCodeScantResult);
//    }

    /**
     * Set logger level.
     *
     * @param loggerLevel logger level
     */
    protected void setLoggerLevel(int loggerLevel) {
        KMFApplication.getLogger().setLogLevel(loggerLevel);
    }

    /**
     * Return resources for defined logo. Logo is string containing name of file with logo saved in drawable.
     *
     * @return
     */
    public int getResources4Logo() {
        return getResources().getIdentifier("kct_logo", "drawable", getPackageName());
    }

    /**
     * Get string from resources.
     *
     * @param resources
     * @return string for resources
     */
    protected String getStringFromResources(Integer resources) {
        return resources == null ?
                null :
                getString(resources);
    }

    /**
     * Get package version name.
     *
     * @return package version name
     */
    public String getPackageVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return getString(R.string.NA);
        }
    }

    /**
     * Get package version name.
     *
     * @return package version name
     */
    public static String getPackageVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return context.getString(R.string.NA);
        }
    }

    /**
     * Get build version release (android version).
     *
     * @return android version name
     */
    public String getBuildVersionRelease() {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.NA);
        }
    }

    /**
     * Get build version release (android version).
     *
     * @return android version name
     */
    public static String getBuildVersionRelease(Context context) {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.NA);
        }
    }

    /**
     * Get build model (the end-user-visible name for the end product).
     *
     * @return android model name
     */
    public String getBuildModel() {
        try {
            return Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.NA);
        }
    }

    /**
     * Get build serial number (a hardware serial number, if available).
     *
     * @return android serial number
     */
    public String getBuildSerial() {
        try {
            return Build.SERIAL;
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.NA);
        }
    }

    /**
     * Get build serial number (a hardware serial number, if available).
     *
     * @return android serial number
     */
    public static String getBuildSerial(Context context) {
        try {
            return Build.SERIAL;
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.NA);
        }
    }

    /**
     * Check whether edit text is empty.
     */
    @Deprecated
    public boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().isEmpty();
    }

    /**
     * Add information into log.
     *
     * @param logTag     resource
     * @param logMessage resource
     */
    public void addLogInfo(int logTag, int logMessage) {
        addLogInfo(logTag, getString(logMessage));
    }

    /**
     * Add information into log.
     *
     * @param logTag     resource
     * @param logMessage message
     */
    public void addLogInfo(int logTag, String logMessage) {
        KMFApplication.getLogger().i(getString(logTag), logMessage);
    }

    /**
     * Add warning into log.
     *
     * @param logTag     resource
     * @param logMessage resource
     */
    public void addLogWarning(int logTag, int logMessage) {
        addLogWarning(logTag, getString(logMessage));
    }

    /**
     * Add warning into log.
     *
     * @param logTag     resource
     * @param logMessage message
     */
    public void addLogWarning(int logTag, String logMessage) {
        KMFApplication.getLogger().w(getString(logTag), logMessage);
    }

    /**
     * Add error into log.
     *
     * @param logTag     resource
     * @param logMessage resource
     */
    public void addLogError(int logTag, int logMessage) {
        addLogError(logTag, getString(logMessage));
    }

    /**
     * Add error into log.
     *
     * @param logTag     resource
     * @param logMessage message
     */
    public void addLogError(int logTag, String logMessage) {
        KMFApplication.getLogger().e(getString(logTag), logMessage);
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftResources
     */
    public void setActionBar(boolean backButton, Integer icon, Integer leftResources) {
        setActionBar(backButton, icon, leftResources, (Integer) null);
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftString
     */
    public void setActionBar(boolean backButton, Integer icon, CharSequence leftString) {
        setActionBar(backButton, icon, leftString, (CharSequence) null);
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftResources
     * @param rightResources
     */
    public void setActionBar(boolean backButton, Integer icon, Integer leftResources, Integer rightResources) {
        setActionBar(backButton, icon, getStringFromResources(leftResources), getStringFromResources(rightResources));
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftResources
     * @param rightString
     */
    public void setActionBar(boolean backButton, Integer icon, Integer leftResources, CharSequence rightString) {
        setActionBar(backButton, icon, getStringFromResources(leftResources), rightString);
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftString
     * @param rightResources
     */
    public void setActionBar(boolean backButton, Integer icon, CharSequence leftString, int rightResources) {
        setActionBar(backButton, icon, leftString, getStringFromResources(rightResources));
    }

    /**
     * Set action bar.
     *
     * @param backButton
     * @param icon
     * @param leftString
     * @param rightString
     */
    public void setActionBar(boolean backButton, int icon, CharSequence leftString, CharSequence rightString) {
        KMFActionBar.setActionBar(this, backButton, icon, leftString, rightString);
    }

    /**
     * Show action bar (show last known state of action bar w/o change).
     */
    public void showActionBar() {
        KMFActionBar.showActionBar(this);
    }

    /**
     * Hide action bar.
     */
    public void hideActionBar() {
        KMFActionBar.hideActionBar(this);
    }

    /**
     * Hide keyboard.
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    /**
     * Get visible fragments array list.
     */
    public ArrayList<Fragment> getArrayListFragmentVisible() {
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null
                    && fragment.isVisible()) {
                fragments.add(fragment);
            }
        }

        return fragments;
    }

    /**
     * Return true if fragment is visible.
     *
     * @return
     */
    public boolean isFragmentVisible(String tag) {
        for (Fragment fragment : getArrayListFragmentVisible()) {
            if (fragment != null
                    && fragment.getTag() != null
                    && fragment.getTag().equals(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return fragment of specified {@code tag} if is visible.
     *
     * @param tag fragment tag
     * @return {@link android.support.v4.app.Fragment} if visible fragment equals specified
     * {@code tag} otherwise {@code null}
     */
    public Fragment getFragmentVisible(String tag) {
        for (Fragment fragment : getArrayListFragmentVisible()) {
            if (fragment != null
                    && fragment.getTag() != null
                    && fragment.getTag().equals(tag)) {
                return fragment;
            }
        }

        //Po prepsani ze SAP cache do KMFCache je nutny tento dirty hack
        //Puvodne cache byla pomala a ansynchronni - akce zobrazeni fragmentu a nacteni cache
        //probihaly paralelne a hlavne nacitani pomalu. Diky tomu se fragment dostal do stavu
        //isVisible() = true.
        if (getSupportFragmentManager().getFragments() != null &&
                getSupportFragmentManager().getFragments().size() == 1 &&
                getSupportFragmentManager().getFragments().get(0).getTag().equals(tag) &&
                getSupportFragmentManager().getFragments().get(0).isAdded() &&
                !getSupportFragmentManager().getFragments().get(0).isRemoving() &&
                !getSupportFragmentManager().getFragments().get(0).isHidden()
        ) {
            return getSupportFragmentManager().getFragments().get(0);
        }

        return null;
    }

    /**
     * Return back stack state name using TAG of
     * actual visible fragment.
     *
     * @return name of back stack state
     */
    public String getBackStackStateName() {
        if (getArrayListFragmentVisible().isEmpty()) {
            return null;
        } else {
            return getArrayListFragmentVisible().get(0).getTag();
        }
    }

    /**
     * Return fragment of specified {@code tag}.
     *
     * @param tag fragment tag
     * @return {@link android.support.v4.app.Fragment} if fragment equals specified
     * {@code tag} otherwise {@code null}
     */
    public Fragment getFragment(String tag) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null
                    && fragment.getTag() != null
                    && fragment.getTag().equals(tag)) {
                return fragment;
            }
        }

        return null;
    }

    /**
     * Get set of fragments tags.
     *
     * @return {@link java.util.Set} containing fragments tags
     */
    public Set<String> getSetFragmentTag() {
        Set<String> setFragmentTag = new HashSet<String>();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.getTag() != null) {
                setFragmentTag.add(fragment.getTag());
            }
        }

        return setFragmentTag;
    }

    /**
     * Show information alert dialog.
     *
     * @param resources message
     */
    public void showAlertDialogInformationWithoutResponse(int resources) {
        showAlertDialogInformationWithoutResponse(getString(resources));
    }

    /**
     * Show information alert dialog, that have set by default:
     * <ul>
     * <li>title image and title text</li>
     * <li>one positive button </li>
     * </ul>
     * Message text is set by parametr {@code message}.
     *
     * @param message message
     */
    public void showAlertDialogInformationWithoutResponse(CharSequence message) {
        KMFDialogAlert alertDialogInformation = new KMFDialogAlert(this) {
            @Override
            public void onButtonPositive(View view) {
            }

            @Override
            public void onButtonNegative(View view) {
            }
        };
        alertDialogInformation.setTitleBackground(R.drawable.dialog_info);
        alertDialogInformation.setTitleImage(R.drawable.ic_dialog_info);
        alertDialogInformation.setTitleImageTint(getResources().getColor(R.color.dialogInfo));
        alertDialogInformation.setTitleText(R.string.Information);
        alertDialogInformation.setTitleTextColor(getResources().getColor(R.color.dialogInfo));
        alertDialogInformation.setMessageText(message);
        alertDialogInformation.setButtonPositive(R.string.Ok);
        alertDialogInformation.setCancelable(true);
        alertDialogInformation.show();
    }

    /**
     * Show error alert dialog.
     *
     * @param resources message
     */
    public void showAlertDialogErrorWithoutResponse(int resources) {
        showAlertDialogErrorWithoutResponse(getString(resources));
    }

    /**
     * Show error alert dialog, that have set by default:
     * <ul>
     * <li>title image and title text</li>
     * <li>one positive button </li>
     * </ul>
     * Message text is set by parametr {@code message}.
     *
     * @param message message
     */
    public void showAlertDialogErrorWithoutResponse(CharSequence message) {
        KMFDialogAlert dialogAlert = new KMFDialogAlert(this) {
            @Override
            public void onButtonPositive(View view) {
            }

            @Override
            public void onButtonNegative(View view) {
            }
        };
        dialogAlert.setTitleBackground(R.drawable.dialog_error);
        dialogAlert.setTitleImage(R.drawable.ic_dialog_error);
        dialogAlert.setTitleImageTint(getResources().getColor(R.color.dialogError));
        dialogAlert.setTitleText(R.string.Error);
        dialogAlert.setTitleTextColor(getResources().getColor(R.color.dialogError));
        dialogAlert.setMessageText(message);
        dialogAlert.setButtonPositive(R.string.Ok);
        dialogAlert.setCancelable(true);
        dialogAlert.show();
    }

    /**
     * Show error alert dialog.
     *
     * @param resources message
     */
    public void showAlertDialogWarningWithoutResponse(int resources) {
        showAlertDialogWarningWithoutResponse(getString(resources));
    }

    /**
     * Show error alert dialog, that have set by default:
     * <ul>
     * <li>title image and title text</li>
     * <li>one positive button </li>
     * </ul>
     * Message text is set by parametr {@code message}.
     *
     * @param message message
     */
    public void showAlertDialogWarningWithoutResponse(CharSequence message) {
        KMFDialogAlert dialogAlert = new KMFDialogAlert(this) {
            @Override
            public void onButtonPositive(View view) {
            }

            @Override
            public void onButtonNegative(View view) {
            }
        };
        dialogAlert.setTitleBackground(R.drawable.dialog_warning);
        dialogAlert.setTitleImage(R.drawable.ic_dialog_warning);
        dialogAlert.setTitleImageTint(getResources().getColor(R.color.dialogWarning));
        dialogAlert.setTitleText(R.string.Warning);
        dialogAlert.setTitleTextColor(getResources().getColor(R.color.dialogWarning));
        dialogAlert.setMessageText(message);
        dialogAlert.setButtonPositive(R.string.Ok);
        dialogAlert.setCancelable(true);
        dialogAlert.show();
    }

    /**
     * Start barcode scanner used for devices:
     * <ul>
     * <li>Intermec</li>
     * <li>Motorola</li>
     * </ul>
     */
    public void startBarcodeScanner() {
        startBarcodeScanner(true);
    }

    /**
     * Start barcode scanner used for devices:
     * <ul>
     * <li>Intermec</li>
     * <li>Motorola</li>
     * </ul>
     *
     * @param enabled enabled
     */
    public void startBarcodeScanner(boolean enabled) {
        String deviceModel = KMFApplication.getDeviceModel();

        if (deviceModel.contains(KMFDevices.MODEL_CN51) ||
                deviceModel.contains(KMFDevices.MODEL_CT40) ||
                deviceModel.contains(KMFDevices.MODEL_CT60)) {
            startBarcodeReaderService(enabled);
        } else if (deviceModel.equals(KMFDevices.MODEL_TC55)
                || deviceModel.equals(KMFDevices.MODEL_ET1)
                || deviceModel.equals(KMFDevices.MODEL_TC700H)) {
            registerBroadcastReceiver();
        }
    }

    /**
     * Stop barcode scanner used for devices:
     * <ul>
     * <li>Intermec</li>
     * <li>Motorola</li>
     * </ul>
     */
    public void stopBarcodeScanner() {
        String deviceModel = KMFApplication.getDeviceModel();

        if (deviceModel.contains(KMFDevices.MODEL_CN51) ||
                deviceModel.contains(KMFDevices.MODEL_CT40) ||
                deviceModel.contains(KMFDevices.MODEL_CT60)) {
            stopBarcodeReaderService();
        } else if (deviceModel.equals(KMFDevices.MODEL_TC55)
                || deviceModel.equals(KMFDevices.MODEL_ET1)
                || deviceModel.equals(KMFDevices.MODEL_TC700H)) {
            unregisterBroadcastReceiver();
        }
    }

    /**
     * Set barcode reader enabled.
     *
     * @param enabled enabled
     */
    public void setBarcodeReaderEnabled(boolean enabled) {
        if (KMFApplication.getDeviceModel().contains(KMFDevices.MODEL_CN51) ||
                KMFApplication.getDeviceModel().contains(KMFDevices.MODEL_CT40) ||
                KMFApplication.getDeviceModel().contains(KMFDevices.MODEL_CT60)) {
            if (mReaderCN51 == null) {
                return;
            }

            if (enabled) {
                mReaderCN51.enableBarcodeReader();
            } else {
                mReaderCN51.disableBarcodeReader();
            }
        }
    }

    /**
     * Start barcode reader service for device Intermec.
     *
     * @param enabled enabled
     */
    private void startBarcodeReaderService(final boolean enabled) {
        mReaderCN51 = KMFReaderCN51.createInstance(this);
        mReaderCN51.startBarcodeService(this, enabled);
    }

    /**
     * Stop barcode reader service for device Intermec.
     */
    private void stopBarcodeReaderService() {
        if (mReaderCN51 != null) {
            mReaderCN51.stopBarcodeService();
            mReaderCN51 = null;
        }
    }

    /**
     * Register broadcast receiver for device Motorola.
     * Data wedge must be configured!
     */
    private void registerBroadcastReceiver() {
        if (this.mBroadcastReceiver != null) {
            unregisterBroadcastReceiver(); //dulezite nejdrive vymazu predchozi
        }

        this.mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(KMFDevices.MOTOROLA_INTENT_FILTER)) {
                    onSuccessBarcodeScanResult(intent.getExtras().getString(KMFDevices.MOTOROLA_DATA_STRING));
                }
            }
        };
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(KMFDevices.MOTOROLA_INTENT_FILTER));
    }

    /**
     * Unregister broadcast recevier for device Motorola.
     */
    protected void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            this.unregisterReceiver(mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
    }

    /**
     * On success barcode scan result.
     * Overriding this method implement your own code.
     *
     * @param barcodeScanResult
     */
    public void onSuccessBarcodeScanResult(String barcodeScanResult) {
    }

    /**
     * Start intent for image capture.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Deprecated
    public void getPhoto(Uri uri) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intentCamera.setClipData(ClipData.newRawUri(null, uri));
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intentCamera.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024 * 1024);
        try {
            startActivityForResult(intentCamera, REQUEST_CODE_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showAlertDialogErrorWithoutResponse(e.getLocalizedMessage());
        }
    }

    /**
     * Start activity for file.
     */
    public void startActivityForResultCamera(File file) {
        startActivityForResultCamera(file, BuildConfig.APPLICATION_ID);
    }

    /**
     * Start activity for file.
     */
    public void startActivityForResultCamera(File file, String applicationId) {
        Uri uri = FileProvider.getUriForFile(
                this,
                applicationId + ".provider",
                file
        );

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(
                MediaStore.EXTRA_OUTPUT,
                uri
        );

        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intentCamera, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivityForResult(intentCamera, REQUEST_CODE_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showAlertDialogErrorWithoutResponse(e.getLocalizedMessage());
        }
    }

    /**
     * Start activity for file.
     */
    public void startActivityForResultFileGet(String type) {
        Intent intentFile = new Intent(Intent.ACTION_GET_CONTENT);
        if (type == null) {
            intentFile.setType("file/*");
        } else {
            intentFile.setType(type);
        }
        try {
            startActivityForResult(intentFile, REQUEST_CODE_FILE_GET);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showAlertDialogErrorWithoutResponse(e.getLocalizedMessage());
        }
    }

    /**
     * Start activity for file for samsung.
     */
    public void startActivityForResultFileGet4Samsung(String type) {
        Intent intentFile = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        if (type == null) {
            intentFile.putExtra("CONTENT_TYPE", "*/*");
        } else {
            intentFile.putExtra("CONTENT_TYPE", type);
        }
        try {
            startActivityForResult(intentFile, REQUEST_CODE_FILE_GET);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            startActivityForResultFileGet(type);
        }
    }

    /**
     * On success photo capture.
     * Overriding this method implement your own code.
     */
    @Deprecated
    public void onSuccessPhotoCaptureResult() {
    }

    /**
     * On success camera.
     */
    @Deprecated
    public void onSuccessCamera() {
        onSuccessPhotoCaptureResult();
    }

    /**
     * On camera success.
     */
    public void onCameraSuccess() {
        onSuccessCamera();
    }

    /**
     * On camera failure.
     */
    public void onCameraFailure() {

    }

    /**
     * On success file get.
     *
     * @param filePath file path
     */
    public void onSuccessFileGet(String filePath) {
    }

    /**
     * Return external directory.
     *
     * @param type
     * @return
     */
    public File getExternalDir(String type) {
        return getExternalFilesDir(type);
    }

    /**
     * Get mime type from uri.
     *
     * @param uri
     * @return
     */
    @Deprecated
    public String getMimeType(Uri uri) {
        return getContentResolver().getType(uri);
    }

    @Override
    public void onSuccessScan(String barcodeScanResult) {
        mCN51BarcodeData = barcodeScanResult;
        mHandler.post(barCodeScantResult);
    }
}
