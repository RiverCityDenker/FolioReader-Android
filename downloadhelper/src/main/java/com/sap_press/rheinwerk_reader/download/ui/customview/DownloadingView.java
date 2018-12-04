package com.sap_press.rheinwerk_reader.download.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap_press.rheinwerk_reader.download.events.DownloadingEvent;
import com.sap_press.rheinwerk_reader.download.events.OnResetDownloadBookEvent;
import com.sap_press.rheinwerk_reader.download.events.ResumeDownloadUpdateViewEvent;
import com.sap_press.rheinwerk_reader.downloadhelper.R;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public abstract class DownloadingView extends RelativeLayout {

    protected static final int FILE_SIZE_UNIT = 1024 * 1024;
    private static final String TAG = DownloadingView.class.getSimpleName();
    protected ProgressBar mProgressBar;
    protected ProgressBar mProgressBarWaiting;
    protected ImageView mImageView;
    protected TextView tvBookSize;
    protected View mRootView;
    protected Ebook mEbook;
    protected boolean mEnableUpdateProgress;
    protected int mDefaultColor;
    protected int mActiveColor;
    protected int mTextColorDefault;
    protected int mTextSize;
    protected int mBackgroundColorDefault;
    protected int mProgressBarWaitingSize;
    protected int mTextMarginTop;
    private int mIconSize;
    protected Drawable mActiveDownloadIcon;
    protected Drawable mDefaultDownloadIcon;
    protected Drawable mRemoveDownloadIcon;
    protected Drawable mFailedDownloadIcon;
    protected Context mContext;

    public DownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DownloadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = layoutInflater.inflate(getLayout(), null);
        addView(mRootView);
        mFailedDownloadIcon = AppCompatResources.getDrawable(context, R.drawable.ic_downloadpaused);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DownloadingView,
                0, 0);
        try {
            mTextColorDefault = a.getColor(R.styleable.DownloadingView_textColorDefault, context.getResources().getColor(R.color.color_gray));
            mDefaultColor = a.getColor(R.styleable.DownloadingView_defaultColor, context.getResources().getColor(R.color.color_gray));
            mActiveColor = a.getColor(R.styleable.DownloadingView_activeColor, context.getResources().getColor(R.color.color_gray));
            mBackgroundColorDefault = a.getColor(R.styleable.DownloadingView_backgroundColorDefault, context.getResources().getColor(R.color.color_gray));
            mActiveDownloadIcon = a.getDrawable(R.styleable.DownloadingView_activeDownloadIcon);
            mDefaultDownloadIcon = a.getDrawable(R.styleable.DownloadingView_defaultDownloadIcon);
            mRemoveDownloadIcon = a.getDrawable(R.styleable.DownloadingView_removeDownloadIcon);
            mProgressBarWaitingSize = a.getDimensionPixelSize(R.styleable.DownloadingView_progressBarWaitingSize, context.getResources().getDimensionPixelSize(R.dimen.dp_12));
            mTextSize = a.getDimensionPixelSize(R.styleable.DownloadingView_sizeOfText, context.getResources().getDimensionPixelSize(R.dimen.sp_9));
            mTextMarginTop = a.getDimensionPixelSize(R.styleable.DownloadingView_marginTopFromText, context.getResources().getDimensionPixelSize(R.dimen.dp_5));
            mIconSize = a.getDimensionPixelSize(R.styleable.DownloadingView_iconSize, context.getResources().getDimensionPixelSize(R.dimen.dp_24));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
        EventBus.getDefault().register(this);
    }

    private void initViews() {
        tvBookSize = findViewById(R.id.tv_book_size);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBarWaiting = findViewById(R.id.progress_bar_waiting);
        mImageView = findViewById(R.id.img_download);
        mRootView = findViewById(R.id.layout_download);
        configViews();
    }

    protected void configViews() {
        configText();
        configIcon();
        configProgressBar(mActiveColor, mDefaultColor);
        configWaitingProgressBar();
    }

    private void configIcon() {
        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.width = mIconSize;
        layoutParams.height = mIconSize;
        mImageView.setLayoutParams(layoutParams);
        mImageView.setColorFilter(mDefaultColor);
    }

    private void configText() {
        tvBookSize.setTextColor(mTextColorDefault);
    }

    protected void configProgressBar(int activeColor, int defaultColor) {
        LayerDrawable progressBarDrawable = (LayerDrawable) mProgressBar.getProgressDrawable();
        Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
        Drawable progressDrawable = progressBarDrawable.getDrawable(1);

        backgroundDrawable.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
        progressDrawable.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN);
    }

    protected abstract void onPaused(int progress);

    private void configWaitingProgressBar() {
        ViewGroup.LayoutParams progressBarWaitingLayoutParams = mProgressBarWaiting.getLayoutParams();
        progressBarWaitingLayoutParams.width = mProgressBarWaitingSize;
        progressBarWaitingLayoutParams.height = mProgressBarWaitingSize;
        mProgressBarWaiting.setLayoutParams(progressBarWaitingLayoutParams);
    }

    protected int getLayout() {
        return R.layout.layout_download_base;
    }

    protected abstract void updateUI(Ebook ebook);

    protected abstract void showViewNomal();

    protected abstract void showHideContent(boolean isShown);

    protected abstract void showViewFinish();

    public void enableUpdateProgress(boolean downloading) {
        mEnableUpdateProgress = downloading;
    }

    public void showHideWaitingProgressBar(boolean isShown) {
        mProgressBarWaiting.setVisibility(isShown ? VISIBLE : GONE);
    }

    protected String fromDotToComma(double size) {
        String result;
        int sizeMB = (int) Math.round(size);
        result = sizeMB + " MB";
        return result;
    }

    public void setEbook(Ebook ebook) {
        this.mEbook = ebook;
    }

    protected Ebook getEbook() {
        return mEbook;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadingEvent(DownloadingEvent event) {
        updateUI(event.getEbook());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResumeDownloadUpdateViewEvent(ResumeDownloadUpdateViewEvent event) {
        final Ebook ebook = event.getEbook();
        if (ebook.getId() == mEbook.getId()) {
            mEbook.setDownloadFailed(false);
            ebook.setDownloadFailed(false);
            ebook.setDownloadProgress(0);
            updateUI(event.getEbook());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResetDownloadBookEvent(OnResetDownloadBookEvent event) {
        if (event.getEbook().getId() != getEbook().getId()) return;
        mEbook.setDownloadFailed(event.getEbook().isDownloadFailed());
        if (!mEbook.isDownloadFailed()) {
            resetProgressBar();
            final int progress = mProgressBar.getProgress();
            if (progress == 0) {
                showHideContent(false);
                showHideWaitingProgressBar(true);
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
        } else {
            Log.d(TAG, "onResetDownloadBookEvent: >>>");
        }
    }

    protected void resetProgressBar() {
        configProgressBar(mActiveColor, mDefaultColor);
    }
}
