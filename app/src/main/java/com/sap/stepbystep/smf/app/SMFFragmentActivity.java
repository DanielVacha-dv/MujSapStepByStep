package com.sap.stepbystep.smf.app;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.store.ODataRequestExecution;
import com.sap.stepbystep.kmf.store.interfac.IKMFResponseHandler;
import com.sap.stepbystep.smf.repository.SMFCodeListRepository;

import java.util.List;

public class SMFFragmentActivity extends FragmentActivity implements IKMFResponseHandler {

    public static final String TAG = SMFFragmentActivity.class.getName();

    public void beforeSynchronization(boolean synchronizationPhotos) {
        synchronization4CodeList();
    }
    /**
     * Synchronization for code list.
     * Return: boolean
     */
    public void synchronization4CodeList() {
        Log.d(TAG, "synchronization4CodeList()");

//        if (getCodeLists().isEmpty()) {
//            setDialogProgressTitle(
//                    getString(R.string.Synchronizing_code_lists)
//            );
//            mTryNumber++;
            SMFCodeListRepository.read(this, this);
//        } else {
//            Log.d(TAG, "Code list is filled.");
//            synchronization4CostCenters();
//        }
    }

    @Override
    public void onODataRequestError(String operation, Exception e, ODataRequestExecution requestExecution) {

    }

    @Override
    public void onODataRequestSuccess(String operation, ODataEntity entity, String message, ODataRequestExecution requestExecution) {

    }

    @Override
    public void onODataRequestSuccess(String operation, List<ODataEntity> entities, String message, ODataRequestExecution requestExecution) {

    }
}
