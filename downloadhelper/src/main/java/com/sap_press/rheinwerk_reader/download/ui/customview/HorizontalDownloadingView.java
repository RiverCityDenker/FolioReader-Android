package com.sap_press.rheinwerk_reader.download.ui.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.sap_press.rheinwerk_reader.downloadhelper.R;


/**
 * Created by hale on 5/9/2018.
 */

public class HorizontalDownloadingView extends DownloadingView {

    public HorizontalDownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalDownloadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void configViews() {
        super.configViews();
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_download_horizontal;
    }

    @Override
    public synchronized void updateUI(int progress, int ebookId) {
        if (ebookId != getEbook().getId()) return;
        if (progress >= 100) {
            showViewFinish();
            return;
        }
        mRootView.setBackgroundColor(mBackgroundColorDefault);
        mProgressBar.setVisibility(VISIBLE);
        if (!mEnableUpdateProgress) {
            showViewNomal();
        } else if (progress < 0) {
            showViewNomal();
        } else if (progress == 0) {
            showHideContent(false);
            showHideWaitingProgressBar(true);
        } else {
            if (progress >= 45) {
                tvBookSize.setTextColor(mActiveColor);
                mImageView.setImageDrawable(mActiveDownloadIcon);
                mImageView.setColorFilter(mActiveColor);
            } else {
                tvBookSize.setTextColor(mDefaultColor);
                mImageView.setImageDrawable(mDefaultDownloadIcon);
                mImageView.setColorFilter(mDefaultColor);
            }
            tvBookSize.setText(String.format("%d%%", progress));
            showHideContent(true);
        }

        showHideWaitingProgressBar(progress == 0);
        mProgressBar.setProgress(progress);
    }

    @Override
    protected void showViewFinish() {
        showHideContent(true);
        final double size = (double) mEbook.getFileSize() / (1024 * 1024);
        tvBookSize.setText(fromDotToComma(size));
        mImageView.setImageDrawable(mRemoveDownloadIcon);
        mImageView.setColorFilter(mDefaultColor);
        tvBookSize.setTextColor(mDefaultColor);
        mProgressBar.setVisibility(INVISIBLE);
    }

    @Override
    protected void showViewNomal() {
        showHideContent(true);
        mImageView.setImageDrawable(mDefaultDownloadIcon);
        mImageView.setColorFilter(mDefaultColor);
        tvBookSize.setTextColor(mDefaultColor);
        final double size = (double) mEbook.getFileSize() / (1024 * 1024);
        tvBookSize.setText(fromDotToComma(size));
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
