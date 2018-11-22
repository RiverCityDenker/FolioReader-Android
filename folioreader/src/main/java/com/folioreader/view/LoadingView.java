package com.folioreader.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
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
    }

    public void updateTheme() {

        Config config = AppUtil.getSavedConfig(getContext());
        if (config == null)
            config = new Config();
        UiUtil.setColorToImage(getContext(), config.getThemeColor(), progressBar.getIndeterminateDrawable());
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void show() {
        Log.d("todoHa", "show", new Exception());
        setVisibility(VISIBLE);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void hide() {
        Log.d("todoHa", "hide", new Exception());
        setVisibility(INVISIBLE);
    }
}
