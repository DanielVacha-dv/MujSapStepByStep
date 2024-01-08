package com.sap.stepbystep.kmf.android;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.stepbystep.R;

public class KMFActionBar {

    @Deprecated
    public ActionBar mActionBar;

    @Deprecated
    public KMFActionBar(Activity activity, boolean homeButton, Integer icon, CharSequence leftString, CharSequence rightString) {
        mActionBar = activity.getActionBar();
        setHomeButton(homeButton);
        setIcon(icon);
        setTitles(activity, leftString, rightString);
    }

    /**
     * Get menu item index.
     *
     * @param menu
     * @param menuItem
     * @return
     */
    public static Integer getMenuItemIndex(Menu menu, int menuItem) {
        Integer index = null;
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == menuItem)
                return i;
        }
        return index;
    }

    private static ActionBar getActionBar(Activity activity) {
        return activity.getActionBar();
    }

    public static void hideActionBar(Activity activity) {
        if (activity == null)
            return;

        ActionBar actionBar = getActionBar(activity);
        if (actionBar != null && actionBar.isShowing())
            actionBar.hide();
    }

    public static void showActionBar(Activity activity) {
        showActionBar(getActionBar(activity));
    }

    public static void setActionBar(Activity activity, boolean homeButton, Integer icon, CharSequence leftTitle, CharSequence rightTitle) {
        ActionBar actionBar = getActionBar(activity);
        if (actionBar == null)
            return;

        showActionBar(actionBar);
        setHomeButton(actionBar, homeButton);
        setIcon(actionBar, icon);
        setTitles(activity, actionBar, leftTitle, rightTitle);
    }

    private static void showActionBar(ActionBar actionBar) {
        if (!actionBar.isShowing())
            actionBar.show();
    }

    private static void setHomeButton(ActionBar actionBar, boolean homeButton) {
        actionBar.setHomeButtonEnabled(homeButton);
        actionBar.setDisplayHomeAsUpEnabled(homeButton);
    }

    private static void setIcon(ActionBar actionBar, Integer icon) {
        if (icon != null)
            actionBar.setIcon(icon);
    }

    private static void setTitles(Activity activity, ActionBar actionBarIn, CharSequence leftString, CharSequence rightString) {
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        View actionBar = LayoutInflater.from(activity).inflate(R.layout.action_bar, null);

        actionBarIn.setCustomView(actionBar, layoutParams);
        actionBarIn.setDisplayShowCustomEnabled(true);

        ((TextView) actionBarIn.getCustomView().findViewById(R.id.actionBarLeft)).setText(leftString);
        ((TextView) actionBarIn.getCustomView().findViewById(R.id.actionBarRight)).setText(rightString);
    }

    /**
     * Set home button. Enbale/Disable home button on action bar icon.
     *
     * @param home
     */
    @Deprecated
    public void setHomeButton(boolean home) {
        mActionBar.setHomeButtonEnabled(home);
        mActionBar.setDisplayHomeAsUpEnabled(home);
    }

    /**
     * Set icon.
     *
     * @param icon
     */
    @Deprecated
    public void setIcon(Integer icon) {
        if (icon != null)
            mActionBar.setIcon(icon);
    }

    /**
     * Set left and right string to action bar.
     *
     * @param leftString
     * @param rightString
     */
    @Deprecated
    public void setTitles(Context context, CharSequence leftString, CharSequence rightString) {
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        View actionBar = LayoutInflater.from(context).inflate(R.layout.action_bar, null);

        mActionBar.setCustomView(actionBar, layoutParams);
        mActionBar.setDisplayShowCustomEnabled(true);

        ((TextView) mActionBar.getCustomView().findViewById(R.id.actionBarLeft)).setText(leftString);
        ((TextView) mActionBar.getCustomView().findViewById(R.id.actionBarRight)).setText(rightString);
    }

}

