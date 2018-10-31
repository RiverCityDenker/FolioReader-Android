package com.folioreader.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class LoadingView extends FrameLayout {

    private ConstraintLayout rootView;
    private ProgressBar progressBar;
    private static final String LOG_TAG = LoadingView.class.getSimpleName();

    public LoadingView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        LayoutInflater.from(context).inflate(R.layout.view_loading, this);

        if (isInEditMode())
            return;

        rootView = findViewById(R.id.rootView);
        progressBar = findViewById(R.id.progressBar);

        updateTheme();
        show();
    }

    public void updateTheme() {

        Config config = AppUtil.getSavedConfig(getContext());
        if (config == null)
            config = new Config();
        UiUtil.setColorToImage(getContext(), config.getThemeColor(), progressBar.getIndeterminateDrawable());
        if (config.isNightMode()) {
            rootView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.webview_night));
        } else {
            rootView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void show() {
        setVisibility(VISIBLE);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void hide() {
        setVisibility(INVISIBLE);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void visible() {
        setVisibility(VISIBLE);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void invisible() {
        setVisibility(INVISIBLE);
    }
}
