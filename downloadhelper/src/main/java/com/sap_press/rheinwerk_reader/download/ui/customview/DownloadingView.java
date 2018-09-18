package com.sap_press.rheinwerk_reader.download.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap_press.rheinwerk_reader.download.events.DownloadingEvent;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.downloadhelper.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public abstract class DownloadingView extends RelativeLayout {

    protected static final int FILE_SIZE_UNIT = 1024 * 1024;
    protected ProgressBar mProgressBar;
    protected ProgressBar mProgressBarWaiting;
    protected ImageView mImageView;
    protected TextView tvBookSize;
    protected View mRootView;
    protected Ebook mEbook;
    protected boolean mEnableUpdateProgress;
    protected int mDefaultColor;
    protected int mActiveColor;
    protected int mTextSize;
    protected int mBackgroundColorDefault;
    protected int mProgressBarWaitingSize;
    protected int mTextMarginTop;
    protected Drawable mActiveDownloadIcon;
    protected Drawable mDefaultDownloadIcon;
    protected Drawable mRemoveDownloadIcon;

    public DownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DownloadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = layoutInflater.inflate(getLayout(), null);
        addView(mRootView);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DownloadingView,
                0, 0);
        try {
            mDefaultColor = a.getColor(R.styleable.DownloadingView_defaultColor, context.getResources().getColor(R.color.color_gray));
            mActiveColor = a.getColor(R.styleable.DownloadingView_activeColor, context.getResources().getColor(R.color.color_gray));
            mBackgroundColorDefault = a.getColor(R.styleable.DownloadingView_backgroundColorDefault, context.getResources().getColor(R.color.color_gray));
            mActiveDownloadIcon = a.getDrawable(R.styleable.DownloadingView_activeDownloadIcon);
            mDefaultDownloadIcon = a.getDrawable(R.styleable.DownloadingView_defaultDownloadIcon);
            mRemoveDownloadIcon = a.getDrawable(R.styleable.DownloadingView_removeDownloadIcon);
            mProgressBarWaitingSize = a.getDimensionPixelSize(R.styleable.DownloadingView_progressBarWaitingSize, context.getResources().getDimensionPixelSize(R.dimen.dp_12));
            mTextSize = a.getDimensionPixelSize(R.styleable.DownloadingView_sizeOfText, context.getResources().getDimensionPixelSize(R.dimen.sp_9));
            mTextMarginTop = a.getDimensionPixelSize(R.styleable.DownloadingView_marginTopFromText, context.getResources().getDimensionPixelSize(R.dimen.dp_5));
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
        configWaitingProgressBar();
    }

    private void configWaitingProgressBar() {
        ViewGroup.LayoutParams progressBarWaitingLayoutParams = mProgressBarWaiting.getLayoutParams();
        progressBarWaitingLayoutParams.width = mProgressBarWaitingSize;
        progressBarWaitingLayoutParams.height = mProgressBarWaitingSize;
        mProgressBarWaiting.setLayoutParams(progressBarWaitingLayoutParams);
    }

    protected int getLayout() {
        return R.layout.layout_download_base;
    }

    protected abstract void updateUI(int progress, int ebookId);

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

    public Ebook getEbook() {
        return mEbook;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadingEvent(DownloadingEvent event) {
        updateUI(event.getProgress(), event.getEbookId());
    }
}
