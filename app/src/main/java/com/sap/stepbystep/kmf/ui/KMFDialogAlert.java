package com.sap.stepbystep.kmf.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.sap.stepbystep.kmf.helpers.KMFHelperView;
import com.sap.stepbystep.kmf.ui.listeners.KMFOnSingleClickListener;

public abstract class KMFDialogAlert {

    private static final int AD_WITH_TITLE_MESSAGE_BUTTONS = R.layout.alert_dialog_with_title_messsage_buttons;

    /**
     * Resouorce ids
     */
    private static final int alertDialogTitle = R.id.alertDialogTitle;
    private static final int alertDialogTitleImage = R.id.alertDialogTitleImage;
    private static final int alertDialogTitleText = R.id.alertDialogTitleText;
    private static final int alertDialogMessage = R.id.alertDialogMessage;
    private static final int alertDialogMessageImage = R.id.alertDialogMessageImage;
    private static final int alertDialogMessageText = R.id.alertDialogMessageText;
    private static final int alertDialogButtons = R.id.alertDialogButtons;
    private static final int alertDialogButtonPositive = R.id.alertDialogButtonPositive;
    private static final int alertDialogButtonNegative = R.id.alertDialogButtonNegative;

    private Context mContext;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mAlertDialogBuilder;
    private View mView;

    private KMFOnSingleClickListener mSingleClickListener;

    /**
     * Create instance of AlertDialog.Builder and set cancelable = true as default.
     *
     * @param context
     */
    public KMFDialogAlert(Context context) {
        mContext = context;
        mAlertDialogBuilder = new AlertDialog.Builder(context);
        mAlertDialogBuilder.setCancelable(false);
        mView = LayoutInflater.from(mContext).inflate(AD_WITH_TITLE_MESSAGE_BUTTONS, null);
    }

    public void setSingleClickListener(KMFOnSingleClickListener singleClickListener) {
        mSingleClickListener = singleClickListener;
    }

    /**
     * Set title background resource.
     *
     * @param resources
     */
    public void setTitleBackground(int resources) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogTitle));
        mView.findViewById(alertDialogTitle).setBackgroundResource(resources);
    }

    /**
     * Set title image.
     *
     * @param image
     */
    public void setTitleImage(int image) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogTitle));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getImageView(mView, alertDialogTitleImage));
        KMFHelperView.getImageView(mView, alertDialogTitleImage).setImageResource(image);
    }

    /**
     * Set title image tint(color).
     *
     * @param color
     */
    public void setTitleImageTint(int color) {
        KMFHelperView.getImageView(mView, alertDialogTitleImage).setColorFilter(color);
    }

    /**
     * Set title text.
     *
     * @param text
     */
    public void setTitleText(int text) {
        setTitleText(mContext.getString(text));
    }

    /**
     * Set title text color.
     *
     * @param color
     */
    public void setTitleTextColor(int color) {
        KMFHelperView.getTextView(mView, alertDialogTitleText).setTextColor(color);
    }

    /**
     * Set title text.
     *
     * @param text
     */
    public void setTitleText(CharSequence text) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogTitle));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, alertDialogTitleText));
        KMFHelperView.getTextView(mView, alertDialogTitleText).setText(text);
    }

    /**
     * Set message image.
     *
     * @param image
     */
    public void setMessageImage(int image) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogMessage));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getImageView(mView, alertDialogMessageImage));
        KMFHelperView.getImageView(mView, alertDialogMessageImage).setImageResource(image);
    }

    /**
     * Set title image tint(color).
     *
     * @param color
     */
    public void setMessageImageTint(int color) {
        KMFHelperView.getImageView(mView, alertDialogMessageImage).setColorFilter(color);
    }

    /**
     * Set message text.
     *
     * @param text
     */
    public void setMessageText(int text) {
        setMessageText(mContext.getString(text));
    }

    /**
     * Set message text.
     *
     * @param text
     */
    public void setMessageText(CharSequence text) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogMessage));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, alertDialogMessageText));
        KMFHelperView.getTextView(mView, alertDialogMessageText).setText(text);
    }

    /**
     * Set button text.
     *
     * @param text
     */
    public void setButtonPositive(int text) {
        setButtonPositive(mContext.getString(text));
    }

    /**
     * Set button positive text.
     *
     * @param text
     */
    public void setButtonPositive(CharSequence text) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonPositive = KMFHelperView.getButton(mView, alertDialogButtonPositive);
        KMFHelperView.setVisibilityVisible(buttonPositive);
        buttonPositive.setText(text);
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onButtonPositive(view);
            }
        });
    }

    /**
     * Set button positive color.
     *
     * @param color resource id
     */
    public void setButtonPositiveBackgroundColor(int color) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonPositive = KMFHelperView.getButton(mView, alertDialogButtonPositive);

        buttonPositive.setBackgroundColor(color);
    }

    /**
     * Set button positive text color.
     *
     * @param color resource id
     */
    public void setButtonPositiveTextColor(int color) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonPositive = KMFHelperView.getButton(mView, alertDialogButtonPositive);

        buttonPositive.setTextColor(color);
    }

    /**
     * Set button negative text.
     *
     * @param text
     */
    public void setButtonNegative(int text) {
        setButtonNegative(mContext.getString(text));
    }

    /**
     * Set button negative text.
     *
     * @param text
     */
    public void setButtonNegative(CharSequence text) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonNegative = KMFHelperView.getButton(mView, alertDialogButtonNegative);
        KMFHelperView.setVisibilityVisible(buttonNegative);
        buttonNegative.setText(text);
        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onButtonNegative(view);
            }
        });
    }

    /**
     * Set button negative color.
     *
     * @param color resource id
     */
    public void setButtonNegativeBackgroundColor(int color) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonNegative = KMFHelperView.getButton(mView, alertDialogButtonNegative);

        buttonNegative.setBackgroundColor(color);
    }

    /**
     * Set button negative text color.
     *
     * @param color resource id
     */
    public void setButtonNegativeTextColor(int color) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(alertDialogButtons));
        Button buttonNegative = KMFHelperView.getButton(mView, alertDialogButtonNegative);

        buttonNegative.setTextColor(color);
    }

    /**
     * Set cancelable.
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        if (mAlertDialogBuilder != null)
            mAlertDialogBuilder.setCancelable(cancelable);
    }

    /**
     * Show alert dialog.
     */
    public void show() {
        if (mAlertDialogBuilder != null) {
            mAlertDialogBuilder.setView(mView);
            mAlertDialog = mAlertDialogBuilder.create();
            mAlertDialog.show();
        }
    }

    /**
     * Dismiss alert dialog.
     */
    public void dismiss() {
        if (mSingleClickListener != null) {
            mSingleClickListener.finish();
        }

        if (mAlertDialog != null)
            mAlertDialog.dismiss();
    }

    /**
     * Check if alert dialog is showing/displayed.
     *
     * @return
     */
    public boolean isShowing() {
        if (mAlertDialog != null && mAlertDialog.isShowing())
            return true;
        else
            return false;
    }

    /**
     * On positive button click. Write your own implementation.
     *
     * @param view
     */
    public abstract void onButtonPositive(View view);

    /**
     * On negative button click. Write your own implementation.
     *
     * @param view
     */
    public abstract void onButtonNegative(View view);
}