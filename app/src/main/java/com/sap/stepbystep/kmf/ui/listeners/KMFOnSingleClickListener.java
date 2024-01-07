package com.sap.stepbystep.kmf.ui.listeners;

import android.view.View;

public abstract class KMFOnSingleClickListener implements View.OnClickListener {
    private boolean mClicked = false;

    @Override
    public void onClick(View v) {
        if (mClicked) {
            return;
        }

        mClicked = true;
        onSingleClick(v);
    }

    public void finish() {
        mClicked = false;
    }

    public abstract void onSingleClick(View v);

}

