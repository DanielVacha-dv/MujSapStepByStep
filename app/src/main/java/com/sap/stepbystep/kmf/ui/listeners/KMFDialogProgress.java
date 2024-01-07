package com.sap.stepbystep.kmf.ui.listeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.sap.stepbystep.kmf.helpers.KMFHelperView;

public class KMFDialogProgress {

    private static final int PD_WITH_TITLE_MESSAGE = R.layout.progress_dialog_with_title_messsage;
    /**
     * Resouorce ids
     */
    private static final int progressDialogTitle = R.id.progressDialogTitle;
    private static final int progressDialogTitleImage = R.id.progressDialogTitleImage;
    private static final int progressDialogTitleText = R.id.progressDialogTitleText;
    private static final int progressDialogMessage = R.id.progressDialogMessage;
    private static final int progressBarSpinner = R.id.progressBarSpinner;
    private static final int progressDialogMessageText = R.id.progressDialogMessageText;
    private static final int progressDialogHorizontal = R.id.progressDialogHorizontal;
    private static final int progressBarHorizontal = R.id.progressBarHorizontal;
    private static final int progressDialogHorizontalProgress = R.id.progressDialogHorizontalProgress;
    private static final int progressDialogHorizontalProgressPercentage = R.id.progressDialogHorizontalProgressPercentage;
    private static final int progressDialogHorizontalProgressInfo = R.id.progressDialogHorizontalProgressInfo;
    private static final int progressDialogButtonCancel = R.id.progressDialogButtonCancel;
    private Context mContext;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mDialogProgressBuilder;
    private View mView;
    private OnCancelListener mOnCancelListener;

    private KMFOnSingleClickListener mSingleClickListener;

    public KMFDialogProgress(Context context) {
        mContext = context;
        mDialogProgressBuilder = new AlertDialog.Builder(context);
        mDialogProgressBuilder.setCancelable(false);
        mView = LayoutInflater.from(mContext).inflate(PD_WITH_TITLE_MESSAGE, null);
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
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogTitle));
        mView.findViewById(progressDialogTitle).setBackgroundResource(resources);
    }

    /**
     * Set title image.
     *
     * @param image
     */
    public void setTitleImage(int image) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogTitle));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getImageView(mView, progressDialogTitleImage));
        KMFHelperView.getImageView(mView, progressDialogTitleImage).setImageResource(image);
    }

    /**
     * Set title image tint(color).
     *
     * @param color
     */
    public void setTitleImageTint(int color) {
        KMFHelperView.getImageView(mView, progressDialogTitleImage).setColorFilter(color);
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
        KMFHelperView.getTextView(mView, progressDialogTitleText).setTextColor(color);
    }

    /**
     * Set title text.
     *
     * @param text
     */
    public void setTitleText(CharSequence text) {
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogTitle));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, progressDialogTitleText));
        KMFHelperView.getTextView(mView, progressDialogTitleText).setText(text);
    }

    /**
     * Set progress bar spinner.
     */
    public void setProgressBarSpinner() {
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogMessage));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getProgressBar(mView, progressBarSpinner));
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
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogMessage));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, progressDialogMessageText));
        KMFHelperView.getTextView(mView, progressDialogMessageText).setText(text);
    }

    /**
     * Set progress bar horizontal.
     */
    public void setProgressBarHorizontal() {
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogHorizontal));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getProgressBar(mView, progressBarHorizontal));
        KMFHelperView.setVisibilityVisible(mView.findViewById(progressDialogHorizontalProgress));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, progressDialogHorizontalProgressPercentage));
        KMFHelperView.setVisibilityVisible(KMFHelperView.getTextView(mView, progressDialogHorizontalProgressInfo));
    }

    /**
     * Set progress bar horizontal info.
     *
     * @param progress
     * @param max
     */
    public void setProgressBarHorizontal(int progress, int max) {
        setProgressBarHorizontal();
        KMFHelperView.getProgressBar(mView, progressBarHorizontal).setMax(max);
        setProgressBarHorizontalProgress(progress);
    }

    /**
     * Get progress bar horizontal progress.
     */
    public int getProgressBarHorizontalProgress() {
        return KMFHelperView.getProgressBar(mView, progressBarHorizontal).getProgress();
    }

    /**
     * Set progress bar horizontal progress.
     *
     * @param progress
     */
    public void setProgressBarHorizontalProgress(int progress) {
        int max = KMFHelperView.getProgressBar(mView, progressBarHorizontal).getMax();

        KMFHelperView.getProgressBar(mView, progressBarHorizontal).setProgress(progress);
        KMFHelperView.getTextView(mView, progressDialogHorizontalProgressPercentage).setText(String.format("%1$d %%", (progress * 100) / max));
        KMFHelperView.getTextView(mView, progressDialogHorizontalProgressInfo).setText(String.format("%1$d / %2$d", progress, max));
    }

    /**
     * Set cancelable.
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        if (mDialogProgressBuilder != null && cancelable) {
            mDialogProgressBuilder.setCancelable(cancelable);
            mDialogProgressBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mOnCancelListener != null)
                        mOnCancelListener.onCancel(mAlertDialog);
                }
            });

            Button button = KMFHelperView.getButton(mView, progressDialogButtonCancel);
            KMFHelperView.setVisibilityVisible(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnCancelListener != null)
                        mOnCancelListener.onCancel(mAlertDialog);
                }
            });
        }
    }

    /**
     * Register a callback to be invoked when a cancel is pressed.
     *
     * @param onCancelListener The callback that will run.
     */
    public void setOnCancelListener(OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    /**
     * Show progress dialog.
     */
    public void show() {
        mDialogProgressBuilder.setView(mView);
        mAlertDialog = mDialogProgressBuilder.create();
        try {
            mAlertDialog.show();
        } catch (WindowManager.BadTokenException e) {
            // called from background service
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
     * Interface definition for a callback to be invoked when a cancel is pressed.
     */
    public interface OnCancelListener {
        public void onCancel(AlertDialog alertDialog);
    }
}
