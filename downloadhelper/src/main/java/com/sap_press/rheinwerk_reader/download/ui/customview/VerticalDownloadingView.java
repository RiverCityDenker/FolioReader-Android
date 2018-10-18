package com.sap_press.rheinwerk_reader.download.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.sap_press.rheinwerk_reader.downloadhelper.R;


public class VerticalDownloadingView extends DownloadingView {

    private static final String TAG = VerticalDownloadingView.class.getSimpleName();

    public VerticalDownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalDownloadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void configViews() {
        super.configViews();
        configText();
    }

    private void configText() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvBookSize.getLayoutParams();
        layoutParams.setMargins(0, mTextMarginTop, 0, 0);
        tvBookSize.setLayoutParams(layoutParams);
        tvBookSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
    }

    @Override
    protected void onPaused(int progress) {
        mImageView.setImageDrawable(mFailedDownloadIcon);
        if (progress >= 80) {
            tvBookSize.setTextColor(mContext.getResources().getColor(R.color.color_white));
        } else {
            tvBookSize.setTextColor(mContext.getResources().getColor(R.color.color_gray_paused_download));
        }
        if (progress >= 50) {
            mImageView.setColorFilter(mContext.getResources().getColor(R.color.color_white));
        } else {
            mImageView.setColorFilter(mContext.getResources().getColor(R.color.color_gray_paused_download));
        }
        Log.e(TAG, "onPaused: >>>" + progress);
        configProgressBar(mContext.getResources().getColor(R.color.color_white), mContext.getResources().getColor(R.color.color_gray_paused_download));
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_download_vertical;
    }

    @Override
    public synchronized void updateUI(int progress, int ebookId) {
        if (ebookId != getEbook().getId()) return;
        if (progress >= 100) {
            showViewFinish();
            return;
        }
        mRootView.setBackgroundColor(mBackgroundColorDefault);
        if (!mEnableUpdateProgress) {
            showViewNomal();
        } else if (progress < 0) {
            showViewNomal();
        } else if (progress == 0) {
            if (mEbook.isDownloadFailed()) {
                onPaused(progress);
                tvBookSize.setText(String.format("%d%%", progress));
                showHideContent(true);
            } else {
                showHideContent(false);
                showHideWaitingProgressBar(true);
            }
        } else {
            if (mEbook.isDownloadFailed()) {
                onPaused(progress);
            } else {
                if (progress >= 80) {
                    tvBookSize.setTextColor(mActiveColor);
                } else {
                    tvBookSize.setTextColor(mTextColorDefault);
                }
                if (progress >= 50) {
                    mImageView.setImageDrawable(mActiveDownloadIcon);
                    mImageView.setColorFilter(mActiveColor);
                } else {
                    mImageView.setImageDrawable(mDefaultDownloadIcon);
                    mImageView.setColorFilter(mDefaultColor);
                }
            }
            tvBookSize.setText(String.format("%d%%", progress));
            showHideContent(true);
        }

        showHideWaitingProgressBar(progress == 0 && !mEbook.isDownloadFailed());
        mProgressBar.setProgress(progress);
        Log.e(TAG, "updateUI: >>>" + mEbook.isDownloadFailed());
    }

    @Override
    protected void showViewNomal() {
        showHideContent(true);
        mImageView.setImageDrawable(mDefaultDownloadIcon);
        mImageView.setColorFilter(mDefaultColor);
        tvBookSize.setTextColor(mDefaultColor);
        final double size = (double) mEbook.getFileSize() / FILE_SIZE_UNIT;
        tvBookSize.setText(fromDotToComma(size));
    }

    @Override
    protected void showViewFinish() {
        final double size = (double) mEbook.getFileSize() / FILE_SIZE_UNIT;
        tvBookSize.setText(fromDotToComma(size));
        mImageView.setImageDrawable(mRemoveDownloadIcon);
        mImageView.setColorFilter(mActiveColor);
        tvBookSize.setTextColor(mActiveColor);
        resetProgressBar();
        mProgressBar.setProgress(100);
        showHideContent(true);
    }

    @Override
    protected void showHideContent(boolean isShown) {
        if (isShown) {
            mImageView.setVisibility(VISIBLE);
            tvBookSize.setVisibility(VISIBLE);
            showHideWaitingProgressBar(false);
        } else {
            mImageView.setVisibility(GONE);
            tvBookSize.setVisibility(GONE);
        }
    }
}
