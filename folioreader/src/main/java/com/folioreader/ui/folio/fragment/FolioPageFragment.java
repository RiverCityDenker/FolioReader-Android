package com.folioreader.ui.folio.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bossturban.webviewmarker.TextSelectionSupport;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.ReadPositionImpl;
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.MediaOverlaySpeedEvent;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.model.event.RewindIndexEvent;
import com.folioreader.model.event.UpdateHighlightEvent;
import com.folioreader.model.quickaction.ActionItem;
import com.folioreader.model.quickaction.QuickAction;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.base.HtmlTask;
import com.folioreader.ui.base.HtmlTaskCallback;
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.ui.custom.CustomLink;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.activity.FolioActivityCallback;
import com.folioreader.ui.folio.mediaoverlay.MediaController;
import com.folioreader.ui.folio.mediaoverlay.MediaControllerCallbacks;
import com.folioreader.ui.folio.presenter.FolioPagePresenter;
import com.folioreader.ui.folio.views.FolioPageMvpView;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;
import com.folioreader.util.SMILParser;
import com.folioreader.util.UiUtil;
import com.folioreader.view.FolioWebView;
import com.folioreader.view.LoadingView;
import com.folioreader.view.MediaControllerView;
import com.folioreader.view.VerticalSeekbar;
import com.folioreader.view.WebViewPager;
import com.sap_press.rheinwerk_reader.crypto.CryptoManager;
import com.sap_press.rheinwerk_reader.download.events.DownloadFileSuccessEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadSingleFileErrorEvent;
import com.sap_press.rheinwerk_reader.utils.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.readium.r2_streamer.model.publication.link.Link;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.folioreader.ui.base.HtmlUtil.getErrorHtml;
import static com.folioreader.ui.folio.activity.FolioActivity.EpubSourceType.ENCRYPTED_FILE;

/**
 * Created by mahavir on 4/2/16.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class FolioPageFragment
        extends Fragment
        implements HtmlTaskCallback, MediaControllerCallbacks, FolioWebView.SeekBarListener, FolioPageMvpView {

    public static final String LOG_TAG = FolioPageFragment.class.getSimpleName();
    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.ui.folio.fragment.FolioPageFragment.POSITION";
    public static final String KEY_FRAGMENT_FOLIO_BOOK_TITLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.BOOK_TITLE";
    public static final String KEY_FRAGMENT_EPUB_FILE_NAME = "com.folioreader.ui.folio.fragment.FolioPageFragment.EPUB_FILE_NAME";
    private static final String KEY_IS_SMIL_AVAILABLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.IS_SMIL_AVAILABLE";
    private static final String BUNDLE_READ_POSITION_CONFIG_CHANGE = "BUNDLE_READ_POSITION_CONFIG_CHANGE";

    private static final int ACTION_ID_COPY = 1001;
    private static final int ACTION_ID_SHARE = 1002;
    private static final int ACTION_ID_HIGHLIGHT = 1003;
    private static final int ACTION_ID_DEFINE = 1004;

    private static final int ACTION_ID_HIGHLIGHT_COLOR = 1005;
    private static final int ACTION_ID_DELETE = 1006;

    private static final int ACTION_ID_HIGHLIGHT_YELLOW = 1007;
    private static final int ACTION_ID_HIGHLIGHT_GREEN = 1008;
    private static final int ACTION_ID_HIGHLIGHT_BLUE = 1009;
    private static final int ACTION_ID_HIGHLIGHT_PINK = 1010;
    private static final int ACTION_ID_HIGHLIGHT_UNDERLINE = 1011;
    private static final String KEY_TEXT_ELEMENTS = "text_elements";
    private static final String SPINE_ITEM = "spine_item";
    private static final String KEY_SOURCE_TYPE = "source_type";
    private static final String KEY_FRAGMENT_FOLIO_BOOK_CONTENT_KEY = "content_key";
    private static final String KEY_FRAGMENT_FOLIO_BOOK_USER_KEY = "user_key";
    private static final String KEY_FRAGMENT_FOLIO_BOOK_FILE_PATH = "file_path";
    private static final String TAG = FolioPageFragment.class.getSimpleName();
    public static final String URL_PREFIX = "file://";
    public static final String SLASH_SIGN = "/";

    private String mHtmlString = null;
    private boolean hasMediaOverlay = false;
    private String mAnchorId;
    private String rangy = "";
    private String highlightId;

    private ReadPosition lastReadPosition;
    private Bundle outState;
    private Bundle savedInstanceState;

    private View mRootView;

    private LoadingView loadingView;
    private VerticalSeekbar mScrollSeekbar;
    private FolioWebView mWebview;
    private WebViewPager webViewPager;
    private TextSelectionSupport mTextSelectionSupport;
    private TextView mPagesLeftTextView, mMinutesLeftTextView;
    private FolioActivityCallback mActivityCallback;

    private int mTotalMinutes;
    private String mSelectedText;
    private Animation mFadeInAnimation, mFadeOutAnimation;

    private Link spineItem;
    private int mPosition = -1;
    private String mBookTitle;
    private String mEpubFileName = null;
    private boolean mIsPageReloaded;

    private String highlightStyle;

    private MediaController mediaController;
    private Config mConfig;
    private String mBookId;
    private String mBookFilePath;
    private String mContentKey;
    private String mUserKey;
    private String mEpubSourceType;
    private String mConfigedHtml;
    private boolean mIsOnlineReading;
    private FolioPagePresenter mPresenter;
    private boolean mIsErrorPage;

    public static FolioPageFragment newInstance(int position, String bookTitle, Link spineRef, String bookId, FolioActivity.EpubSourceType mEpubSourceType) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, bookTitle);
        args.putString(FolioReader.INTENT_BOOK_ID, bookId);
        args.putString(KEY_SOURCE_TYPE, mEpubSourceType.name());
        args.putSerializable(SPINE_ITEM, spineRef);
        fragment.setArguments(args);
        return fragment;
    }


    public static Fragment newInstance(int position, String bookFilePath, Link spineRef,
                                       String bookId, String bookTitle, String contentKey, String userKey,
                                       FolioActivity.EpubSourceType mEpubSourceType) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, bookTitle);
        args.putString(FolioReader.INTENT_BOOK_ID, bookId);
        args.putString(KEY_SOURCE_TYPE, mEpubSourceType.name());
        args.putSerializable(SPINE_ITEM, spineRef);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_FILE_PATH, bookFilePath);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_CONTENT_KEY, contentKey);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_USER_KEY, userKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mPresenter = new FolioPagePresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.savedInstanceState = savedInstanceState;

        if (getActivity() instanceof FolioActivityCallback)
            mActivityCallback = (FolioActivityCallback) getActivity();

        mBookFilePath = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_FILE_PATH);
        mContentKey = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_CONTENT_KEY);
        mUserKey = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_USER_KEY);
        mBookId = getArguments().getString(FolioReader.INTENT_BOOK_ID);
        mEpubSourceType = getArguments().getString(KEY_SOURCE_TYPE);
        spineItem = (CustomLink) getArguments().getSerializable(SPINE_ITEM);

        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_BOOK_TITLE)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = savedInstanceState.getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = savedInstanceState.getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            spineItem = (CustomLink) savedInstanceState.getSerializable(SPINE_ITEM);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = getArguments().getString(KEY_FRAGMENT_EPUB_FILE_NAME);
        }

        if (spineItem != null) {
            if (spineItem.properties.contains("media-overlay")) {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.SMIL, this);
                hasMediaOverlay = true;
            } else {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.TTS, this);
                mediaController.setTextToSpeech(getActivity());
            }
        }
        highlightStyle = HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal);
        mRootView = inflater.inflate(R.layout.folio_page_fragment, container, false);
        mPagesLeftTextView = (TextView) mRootView.findViewById(R.id.pagesLeft);
        mMinutesLeftTextView = (TextView) mRootView.findViewById(R.id.minutesLeft);

        mConfig = AppUtil.getSavedConfig(getContext());
        mIsOnlineReading = ((FolioActivity) getActivity()).isOnlineReading();
        loadingView = mActivityCallback.getLoadingView();
        showLoading();
        initSeekbar();
        initAnimations();
        initWebView();
        //updatePagesLeftTextBg();

        return mRootView;
    }

    private String getWebviewUrl() {
        return Constants.LOCALHOST + mBookTitle + "/" + spineItem.href;
    }

    private String getFilePath() {
        return mBookFilePath + spineItem.href;
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link MediaControllerView#initListeners()} when pause/play
     * button is clicked
     *
     * @param event of type {@link MediaOverlayPlayPauseEvent} contains if paused/played
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void pauseButtonClicked(MediaOverlayPlayPauseEvent event) {
        if (isAdded()
                && spineItem.href.equals(event.getHref())) {
            mediaController.stateChanged(event);
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link MediaControllerView#initListeners()} when speed
     * change buttons are clicked
     *
     * @param event of type {@link MediaOverlaySpeedEvent} contains selected speed
     *              type HALF,ONE,ONE_HALF and TWO.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void speedChanged(MediaOverlaySpeedEvent event) {
        mediaController.setSpeed(event.getSpeed());
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link MediaControllerView#initListeners()} when new
     * style is selected on button click.
     *
     * @param event of type {@link MediaOverlaySpeedEvent} contains selected style
     *              of type DEFAULT,UNDERLINE and BACKGROUND.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void styleChanged(MediaOverlayHighlightStyleEvent event) {
        if (isAdded()) {
            switch (event.getStyle()) {
                case DEFAULT:
                    highlightStyle =
                            HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal);
                    break;
                case UNDERLINE:
                    highlightStyle =
                            HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.DottetUnderline);
                    break;
                case BACKGROUND:
                    highlightStyle =
                            HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.TextColor);
                    break;
            }
            mWebview.loadPage(String.format(getString(R.string.setmediaoverlaystyle), highlightStyle));
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered when any EBook configuration is changed.
     *
     * @param reloadDataEvent empty POJO.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reload(ReloadDataEvent reloadDataEvent) {

        if (isCurrentFragment())
            getLastReadPosition();

        if (isAdded()) {
            mIsPageReloaded = true;
            setHtml(true);
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * <p>
     * Function triggered when highlight is deleted and page is needed to
     * be updated.
     *
     * @param event empty POJO.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateHighlight(UpdateHighlightEvent event) {
        if (isAdded()) {
            this.rangy = HighlightUtil.generateRangyString(getPageName());
            loadRangy(mWebview, this.rangy);
        }
    }

    public void scrollToAnchorId(String href) {

        if (!TextUtils.isEmpty(href) && href.indexOf('#') != -1) {
            mAnchorId = href.substring(href.lastIndexOf('#') + 1);
            if (loadingView != null && loadingView.getVisibility() != View.VISIBLE) {
                loadingView.show();
                mWebview.loadPage(String.format(getString(R.string.go_to_anchor), mAnchorId));
                mAnchorId = null;
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void resetCurrentIndex(RewindIndexEvent resetIndex) {
        if (isCurrentFragment()) {
            mWebview.loadPage("javascript:rewindCurrentIndex()");
        }
    }

    @Override
    public void onReceiveHtml(String html) {
        if (isAdded()) {
            mHtmlString = html;
            setHtml(false);
        }
    }

    private synchronized void setHtml(boolean reloaded) {
        if (mIsErrorPage) {
            final String baseUrl = URL_PREFIX + "/";
            String mimeType = "text/html";
            mWebview.loadDataWithBaseURL(
                    baseUrl,
                    mHtmlString,
                    mimeType,
                    "UTF-8",
                    null);
        } else {
            Log.e(TAG, "setHtml: >>>href = " + spineItem.href + " - " + reloaded + " - " + mIsOnlineReading);
            if (spineItem != null) {
                String ref = spineItem.href;
                if (!reloaded && spineItem.properties.contains("media-overlay")) {
                    mediaController.setSMILItems(SMILParser.parseSMIL(mHtmlString));
                    mediaController.setUpMediaPlayer(spineItem.mediaOverlay, spineItem.mediaOverlay.getAudioPath(spineItem.href), mBookTitle);
                }
                mConfig = AppUtil.getSavedConfig(getContext());

                String path = "";
                int forwardSlashLastIndex = ref.lastIndexOf('/');
                if (forwardSlashLastIndex != -1)
                    path = ref.substring(0, forwardSlashLastIndex + 1);

                String mimeType;
                if (spineItem.typeLink.equalsIgnoreCase(getString(R.string.xhtml_mime_type))) {
                    mimeType = getString(R.string.xhtml_mime_type);
                } else {
                    mimeType = getString(R.string.html_mime_type);
                }

                final String baseUrl = mEpubSourceType.equals(ENCRYPTED_FILE.name()) ?
                        URL_PREFIX + mBookFilePath + SLASH_SIGN : Constants.LOCALHOST + mBookTitle + SLASH_SIGN + path;

                mConfigedHtml = HtmlUtil.reformatHtml(getContext(), mHtmlString, mConfig);
                mWebview.loadDataWithBaseURL(
                        baseUrl,
                        mConfigedHtml,
                        mimeType,
                        "UTF-8",
                        null);
            }
        }

    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String getDirection() {
        return mActivityCallback.getDirection().toString();
    }


    @SuppressWarnings("unused")
    @JavascriptInterface
    public void hideLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingView.hide();
                }
            });
        }
    }

    public void scrollToLast() {
        boolean isPageLoading = loadingView == null || loadingView.getVisibility() == View.VISIBLE;
        Log.v(LOG_TAG, "-> scrollToLast -> isPageLoading = " + isPageLoading);
        if (!isPageLoading) {
            mWebview.loadPage("javascript:scrollToLast()");
        }
    }

    public void scrollToFirst() {
        boolean isPageLoading = loadingView == null || loadingView.getVisibility() == View.VISIBLE;
        Log.v(LOG_TAG, "-> scrollToFirst -> isPageLoading = " + isPageLoading);
        if (!isPageLoading) {
            mWebview.loadPage("javascript:scrollToFirst()");
        }
    }

    private void initWebView() {

        FrameLayout webViewLayout = mRootView.findViewById(R.id.webViewLayout);
        mWebview = webViewLayout.findViewById(R.id.folioWebView);
        webViewPager = webViewLayout.findViewById(R.id.webViewPager);
        if (getActivity() instanceof FolioActivityCallback)
            mWebview.setFolioActivityCallback((FolioActivityCallback) getActivity());

        if (getActivity() instanceof FolioWebView.ToolBarListener)
            mWebview.setToolBarListener((FolioWebView.ToolBarListener) getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            WebView.setWebContentsDebuggingEnabled(true);

        setupScrollBar();
        mWebview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int height =
                        (int) Math.floor(mWebview.getContentHeight() * mWebview.getScale());
                int webViewHeight = mWebview.getMeasuredHeight();
                mScrollSeekbar.setMaximum(height - webViewHeight);
            }
        });

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setAllowFileAccess(true);

        mWebview.setHorizontalScrollBarEnabled(false);

        mWebview.addJavascriptInterface(this, "Highlight");
        mWebview.addJavascriptInterface(this, "FolioPageFragment");
        mWebview.addJavascriptInterface(webViewPager, "WebViewPager");
        mWebview.addJavascriptInterface(loadingView, "LoadingView");
        mWebview.addJavascriptInterface(mWebview, "FolioWebView");

        mWebview.setWebChromeClient(webChromeClient);
        mWebview.setWebViewClient(webViewClient);

        mTextSelectionSupport = TextSelectionSupport.support(getActivity(), mWebview);
        mTextSelectionSupport.setSelectionListener(new TextSelectionSupport.SelectionListener() {
            @Override
            public void startSelection() {
            }

            @Override
            public void selectionChanged(String text) {
                mSelectedText = text;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebview.loadPage("javascript:alert(getRectForSelectedText())");
                    }
                });
            }

            @Override
            public void endSelection() {

            }
        });

        mWebview.getSettings().setDefaultTextEncodingName("utf-8");
        if (mEpubSourceType.equals(ENCRYPTED_FILE.name())) {
            if (mIsOnlineReading) {
                final FolioActivity activity = (FolioActivity) getActivity();
                if (!FileUtil.isFileExist(getActivity(), mBookId, spineItem.href)
                        || mContentKey == null || mContentKey.isEmpty()) {
                    mPresenter.downloadSingleFile(activity, mActivityCallback.getDownloadInfo(),
                            activity.getEbook(),
                            spineItem.href);
                } else
                    onReceiveHtml(CryptoManager.decryptContentKey(mContentKey, mUserKey, getFilePath()));
            } else {
                onReceiveHtml(CryptoManager.decryptContentKey(mContentKey, mUserKey, getFilePath()));
            }
        } else {
            new HtmlTask(this).execute(getWebviewUrl());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDownloadFileSuccess(DownloadFileSuccessEvent event) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (event.getEbook().getHref().equalsIgnoreCase(FileUtil.reformatHref(spineItem.href))) {
                    mActivityCallback.updateEbook(event.getEbook());
                    onReceiveHtml(CryptoManager.decryptContentKey(event.getEbook().getContentKey(), mUserKey, getFilePath()));
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDownloadSingleFileErrorEvent(DownloadSingleFileErrorEvent event) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (event != null && event.getEbook() != null && event.getEbook().getHref().equalsIgnoreCase(FileUtil.reformatHref(spineItem.href))) {
                    mIsErrorPage = true;
                    onReceiveHtml(getErrorHtml(getContext(), mConfig, event.getTitle(), event.getMessage()));
                }
            });
        }
    }

    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "onReceivedError: " + description);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            hideLoading();
            if (isAdded()) {

                mWebview.loadPage("javascript:getCompatMode()");
                mWebview.loadPage("javascript:alert(getReadingTime())");

                if (!hasMediaOverlay)
                    mWebview.loadPage("javascript:wrappingSentencesWithinPTags()");

                if (mActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
                    mWebview.loadPage("javascript:initHorizontalDirection()");
                }

                mWebview.loadPage(String.format(getString(R.string.setmediaoverlaystyle),
                        HighlightImpl.HighlightStyle.classForStyle(
                                HighlightImpl.HighlightStyle.Normal)));

                String rangy = HighlightUtil.generateRangyString(getPageName());
                FolioPageFragment.this.rangy = rangy;
                if (!rangy.isEmpty())
                    loadRangy(mWebview, rangy);
                else
                    loadContent();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.isEmpty() && url.length() > 0) {
                if (Uri.parse(url).getScheme().startsWith("highlight")) {
                    final Pattern pattern = Pattern.compile(getString(R.string.pattern));
                    try {
                        String htmlDecode = URLDecoder.decode(url, "UTF-8");
                        Matcher matcher = pattern.matcher(htmlDecode.substring(12));
                        if (matcher.matches()) {
                            double left = Double.parseDouble(matcher.group(1));
                            double top = Double.parseDouble(matcher.group(2));
                            double width = Double.parseDouble(matcher.group(3));
                            double height = Double.parseDouble(matcher.group(4));
                            onHighlight((int) (UiUtil.convertDpToPixel((float) left,
                                    getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) top,
                                            getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) width,
                                            getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) height,
                                            getActivity())));
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.d(LOG_TAG, e.getMessage());
                    }
                } else {
                    if (url.contains("#")) {
                        mActivityCallback.setPagerToPosition(url);
                        if (isCurrentFragment()) {
                            if (!FolioActivity.anchor.isEmpty()) {
                                mWebview.loadPage("javascript:scrollToElement(\"" + FolioActivity.anchor + "\")");
                                FolioActivity.anchor = "";
                            }
                        }
                    } else if (url.endsWith(".xhtml") || url.endsWith(".html")) {
                        if (url.contains("img") || url.contains("tbl")) {
                            mActivityCallback.showSinglePage(url);
                        } else {
                            mActivityCallback.goToChapter(url);
                        }
                    } else {
                        // Otherwise, give the default behavior (open in browser)
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }

                }
            }
            return true;
        }

        // prevent favicon.ico to be loaded automatically
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.toLowerCase().contains("/favicon.ico")) {
                try {
                    return new WebResourceResponse("image/png", null, null);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "shouldInterceptRequest failed", e);
                }
            }
            return null;
        }

        // prevent favicon.ico to be loaded automatically
        @Override
        @SuppressLint("NewApi")
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (!request.isForMainFrame()
                    && request.getUrl().getPath() != null
                    && request.getUrl().getPath().endsWith("/favicon.ico")) {
                try {
                    return new WebResourceResponse("image/png", null, null);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "shouldInterceptRequest failed", e);
                }
            }
            return null;
        }
    };

    private void loadContent() {
        if (mIsPageReloaded) {

            if (isCurrentFragment()) {
                if (lastReadPosition == null) {
                    lastReadPosition = mActivityCallback.getLastReadPosition();
                }
                mWebview.loadPage(String.format(getString(R.string.go_to_span),
                        lastReadPosition.isUsingId(), lastReadPosition.getValue()));
            } else {
                if (mPosition == mActivityCallback.getChapterPosition() - 1) {
                    // Scroll to last, the page before current page
                    mWebview.loadPage("javascript:scrollToLast()");
                } else {
                    // Make loading view invisible for all other fragments
                    loadingView.hide();
                    setEnableDirectionConfig();
                }
            }

            mIsPageReloaded = false;

        } else if (!TextUtils.isEmpty(mAnchorId)) {
            mWebview.loadPage(String.format(getString(R.string.go_to_anchor), mAnchorId));
            mAnchorId = null;

        } else if (!TextUtils.isEmpty(highlightId)) {
            mWebview.loadPage(String.format(getString(R.string.go_to_highlight), highlightId));
            highlightId = null;

        } else if (isCurrentFragment()) {

            ReadPosition readPosition;
            if (savedInstanceState == null) {
                Log.v(LOG_TAG, "-> onPageFinished -> took from getEntryReadPosition");
                readPosition = mActivityCallback.getEntryReadPosition();
            } else {
                Log.v(LOG_TAG, "-> onPageFinished -> took from bundle");
                readPosition = savedInstanceState.getParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE);
                savedInstanceState.remove(BUNDLE_READ_POSITION_CONFIG_CHANGE);
            }

            if (readPosition != null) {
                Log.v(LOG_TAG, "-> scrollToSpan -> " + readPosition.getValue());
                mWebview.loadPage(String.format(getString(R.string.go_to_span),
                        readPosition.isUsingId(), readPosition.getValue()));
            } else {
                loadingView.hide();
                setEnableDirectionConfig();
            }

        } else {

            if (mPosition == mActivityCallback.getChapterPosition() - 1) {
                // Scroll to last, the page before current page
                mWebview.loadPage("javascript:scrollToLast()");
            } else {
                // Make loading view invisible for all other fragments
                loadingView.hide();
                if (FolioActivity.mIsDirectionChanged) {
                    FolioActivity.mIsDirectionChanged = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setEnableDirectionConfig();
                        }
                    }, 1000);
                }
            }
        }
    }

    private void setEnableDirectionConfig() {
        synchronized (FolioPageFragment.class) {
            mConfig.setEnableDirection(true);
            AppUtil.saveConfig(getContext(), mConfig);
        }
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int progress) {
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            if (FolioPageFragment.this.isVisible()) {
                String rangyPattern = "\\d+\\$\\d+\\$\\d+\\$\\w+\\$";
                Pattern pattern = Pattern.compile(rangyPattern);
                Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    HighlightImpl highlightImpl = HighLightTable.getHighlightForRangy(message);
                    if (HighLightTable.deleteHighlight(message)) {
                        String rangy = HighlightUtil.generateRangyString(getPageName());
                        loadRangy(view, rangy);
                        mTextSelectionSupport.endSelectionMode();
                        if (highlightImpl != null) {
                            HighlightUtil.sendHighlightBroadcastEvent(
                                    FolioPageFragment.this.getActivity().getApplicationContext(),
                                    highlightImpl,
                                    HighLight.HighLightAction.DELETE);
                        }
                    }
                } else if (TextUtils.isDigitsOnly(message)) {
                    try {
                        mTotalMinutes = Integer.parseInt(message);
                    } catch (NumberFormatException e) {
                        mTotalMinutes = 0;
                    }
                } else {
                    pattern = Pattern.compile(getString(R.string.pattern));
                    matcher = pattern.matcher(message);
                    if (matcher.matches()) {
                        double left = Double.parseDouble(matcher.group(1));
                        double top = Double.parseDouble(matcher.group(2));
                        double width = Double.parseDouble(matcher.group(3));
                        double height = Double.parseDouble(matcher.group(4));
                        showTextSelectionMenu((int) (UiUtil.convertDpToPixel((float) left,
                                getActivity())),
                                (int) (UiUtil.convertDpToPixel((float) top,
                                        getActivity())),
                                (int) (UiUtil.convertDpToPixel((float) width,
                                        getActivity())),
                                (int) (UiUtil.convertDpToPixel((float) height,
                                        getActivity())));
                    } else {
                        // to handle TTS playback when highlight is deleted.
                        Pattern p = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
                        if (!p.matcher(message).matches() && (!message.equals("undefined")) && isCurrentFragment()) {
                            mediaController.speakAudio(message);
                        }
                    }
                }
                result.confirm();
            }
            return true;
        }
    };

    /**
     * Calls the /assets/js/Bridge.js#getFirstVisibleSpan(boolean)
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "-> onStop -> " + spineItem.originalHref + " -> " + isCurrentFragment());

        mediaController.stop();
        //TODO save last media overlay item

        if (isCurrentFragment())
            getLastReadPosition();
    }

    public ReadPosition getLastReadPosition() {
        Log.v(LOG_TAG, "-> getLastReadPosition -> " + spineItem.originalHref);

        try {
            synchronized (this) {
                boolean isHorizontal = mActivityCallback.getDirection() ==
                        Config.Direction.HORIZONTAL;
                mWebview.loadPage("javascript:getFirstVisibleSpan(" + isHorizontal + ")");

                wait(1000);
            }
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "-> " + e);
        }

        return lastReadPosition;
    }

    /**
     * Callback method called from /assets/js/Bridge.js#getFirstVisibleSpan(boolean)
     * and then ReadPositionImpl is broadcast to {@link FolioReader#readPositionReceiver}
     *
     * @param usingId if span tag has id then true or else false
     * @param value   if usingId true then span id else span index
     */
    @SuppressWarnings("unused")
    @JavascriptInterface
    public void storeFirstVisibleSpan(boolean usingId, String value) {

        synchronized (this) {
            lastReadPosition = new ReadPositionImpl(mBookId, spineItem.getId(),
                    spineItem.getOriginalHref(), mPosition, usingId, value);
            Intent intent = new Intent(FolioReader.ACTION_SAVE_READ_POSITION);
            intent.putExtra(FolioReader.EXTRA_READ_POSITION, lastReadPosition);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            notify();
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setHorizontalPageCount(int horizontalPageCount) {
        Log.v(LOG_TAG, "-> setHorizontalPageCount = " + horizontalPageCount
                + " -> " + spineItem.originalHref);

        mWebview.setHorizontalPageCount(horizontalPageCount);
    }

    private void loadRangy(final WebView view, final String rangy) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                synchronized (FolioPageFragment.this) {
                    ((FolioWebView) view).loadPage(String.format("javascript:if(typeof window.ssReader !== \"undefined\"){window.ssReader.setHighlights('%s');} else {console.log(\">>>>>>ssReader is undefined !\")}", rangy));
                    loadContent();
                }
            }
        }, 600);
    }

    private void setupScrollBar() {
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), mScrollSeekbar.getProgressDrawable());
        Drawable thumbDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.icons_sroll);
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), (thumbDrawable));
        mScrollSeekbar.setThumb(thumbDrawable);
    }

    private void initSeekbar() {
        mScrollSeekbar = (VerticalSeekbar) mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable()
                .setColorFilter(getResources()
                                .getColor(R.color.app_green),
                        PorterDuff.Mode.SRC_IN);
    }

    private void updatePagesLeftTextBg() {

        if (mConfig.isNightMode()) {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.parseColor("#131313"));
        } else {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.WHITE);
        }
    }

    private void updatePagesLeftText(int scrollY) {
        try {
            int currentPage = (int) (Math.ceil((double) scrollY / mWebview.getWebViewHeight()) + 1);
            int totalPages =
                    (int) Math.ceil((double) mWebview.getContentHeightVal()
                            / mWebview.getWebViewHeight());
            int pagesRemaining = totalPages - currentPage;
            String pagesRemainingStrFormat =
                    pagesRemaining > 1 ?
                            getString(R.string.pages_left) : getString(R.string.page_left);
            String pagesRemainingStr = String.format(Locale.US,
                    pagesRemainingStrFormat, pagesRemaining);

            int minutesRemaining =
                    (int) Math.ceil((double) (pagesRemaining * mTotalMinutes) / totalPages);
            String minutesRemainingStr;
            if (minutesRemaining > 1) {
                minutesRemainingStr =
                        String.format(Locale.US, getString(R.string.minutes_left),
                                minutesRemaining);
            } else if (minutesRemaining == 1) {
                minutesRemainingStr =
                        String.format(Locale.US, getString(R.string.minute_left),
                                minutesRemaining);
            } else {
                minutesRemainingStr = getString(R.string.less_than_minute);
            }

            mMinutesLeftTextView.setText(minutesRemainingStr);
            mPagesLeftTextView.setText(pagesRemainingStr);
        } catch (java.lang.ArithmeticException | IllegalStateException exp) {
            Log.d("divide error", exp.toString());
        }
    }

    private void initAnimations() {
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        mFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mScrollSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOutSeekBarIfVisible();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScrollSeekbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void fadeInSeekBarIfInvisible() {
        if (mScrollSeekbar.getVisibility() == View.INVISIBLE ||
                mScrollSeekbar.getVisibility() == View.GONE) {
            mScrollSeekbar.startAnimation(mFadeInAnimation);
        }
    }

    public void fadeOutSeekBarIfVisible() {
        if (mScrollSeekbar.getVisibility() == View.VISIBLE) {
            mScrollSeekbar.startAnimation(mFadeOutAnimation);
        }
    }

    @Override
    public void onDestroyView() {
        mFadeInAnimation.setAnimationListener(null);
        mFadeOutAnimation.setAnimationListener(null);
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    /**
     * If called, this method will occur after onStop() for applications targeting platforms
     * starting with Build.VERSION_CODES.P. For applications targeting earlier platform versions
     * this method will occur before onStop() and there are no guarantees about whether it will
     * occur before or after onPause()
     *
     * @see Activity#onSaveInstanceState(Bundle) of Build.VERSION_CODES.P
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.outState = outState;

        if (isCurrentFragment())
            Log.v(LOG_TAG, "-> onSaveInstanceState");

        outState.putInt(KEY_FRAGMENT_FOLIO_POSITION, mPosition);
        outState.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, mBookTitle);
        outState.putString(KEY_FRAGMENT_EPUB_FILE_NAME, mEpubFileName);
        outState.putSerializable(SPINE_ITEM, spineItem);
    }

    public void highlight(HighlightImpl.HighlightStyle style, boolean isCreated) {
        if (isCreated) {
            mWebview.loadPage(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", HighlightImpl.HighlightStyle.classForStyle(style)));
        } else {
            mWebview.loadPage(String.format("javascript:setHighlightStyle('%s')", "highlight_" + HighlightImpl.HighlightStyle.classForStyle(style)));
        }
    }

    public void highlightRemove() {
        mWebview.loadPage("javascript:alert(removeThisHighlight())");
    }

    public void showTextSelectionMenu(int x, int y, final int width, final int height) {
        final ViewGroup root =
                (ViewGroup) getActivity().getWindow()
                        .getDecorView().findViewById(android.R.id.content);
        final View view = new View(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setBackgroundColor(Color.TRANSPARENT);

        root.addView(view);

        view.setX(x);
        view.setY(y);
        final QuickAction quickAction =
                new QuickAction(getActivity(), QuickAction.HORIZONTAL);
//  Remove Copy function in version 1.0
//        quickAction.addActionItem(new ActionItem(ACTION_ID_COPY,
//                getString(R.string.copy)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT,
                getString(R.string.highlight)));
        if (!mSelectedText.trim().contains(" ")) {
            quickAction.addActionItem(new ActionItem(ACTION_ID_DEFINE,
                    getString(R.string.define)));
        }
//  Remove Share function in version 1.0
//        quickAction.addActionItem(new ActionItem(ACTION_ID_SHARE,
//                getString(R.string.share)));
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                root.removeView(view);
                onTextSelectionActionItemClicked(actionId, view, width, height);
            }
        });
        quickAction.show(view, width, height);
    }

    private void onTextSelectionActionItemClicked(int actionId, View view, int width, int height) {
        if (actionId == ACTION_ID_COPY) {
            UiUtil.copyToClipboard(getActivity(), mSelectedText);
            Toast.makeText(getActivity(), getString(R.string.copied), Toast.LENGTH_SHORT).show();
            mTextSelectionSupport.endSelectionMode();
        } else if (actionId == ACTION_ID_SHARE) {
            UiUtil.share(getActivity(), mSelectedText);
        } else if (actionId == ACTION_ID_DEFINE) {
            showDictDialog(mSelectedText);
            mTextSelectionSupport.endSelectionMode();
        } else if (actionId == ACTION_ID_HIGHLIGHT) {
            onHighlight(view, width, height, true);
        }
    }

    private void showDictDialog(String mSelectedText) {
        DictionaryFragment dictionaryFragment = new DictionaryFragment();
        Bundle b = new Bundle();
        b.putString(Constants.SELECTED_WORD, mSelectedText);
        dictionaryFragment.setArguments(b);
        dictionaryFragment.show(getFragmentManager(), DictionaryFragment.class.getName());
    }

    private void onHighlight(int x, int y, int width, int height) {
        final View view = new View(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setX(x);
        view.setY(y);
        onHighlight(view, width, height, false);
    }

    private void onHighlight(final View view, int width, int height, final boolean isCreated) {
        ViewGroup root =
                (ViewGroup) getActivity().getWindow().
                        getDecorView().findViewById(android.R.id.content);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            root.addView(view);
        } else {
            final int index = parent.indexOfChild(view);
            parent.removeView(view);
            parent.addView(view, index);
        }

        final QuickAction quickAction = new QuickAction(getActivity(), QuickAction.HORIZONTAL);
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_COLOR,
                getResources().getDrawable(R.drawable.colors_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_DELETE,
                getResources().getDrawable(R.drawable.ic_action_discard)));
//  Remove Share function in version 1.0
//        quickAction.addActionItem(new ActionItem(ACTION_ID_SHARE,
//                getResources().getDrawable(R.drawable.ic_action_share)));
        final ViewGroup finalRoot = root;
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                finalRoot.removeView(view);
                onHighlightActionItemClicked(actionId, view, isCreated);
            }
        });
        quickAction.show(view, width, height);
    }

    private void onHighlightActionItemClicked(int actionId, View view, boolean isCreated) {
        if (actionId == ACTION_ID_HIGHLIGHT_COLOR) {
            onHighlightColors(view, isCreated);
        } else if (actionId == ACTION_ID_SHARE) {
            UiUtil.share(getActivity(), mSelectedText);
            mTextSelectionSupport.endSelectionMode();
        } else if (actionId == ACTION_ID_DELETE) {
            highlightRemove();
        }
    }

    private void onHighlightColors(final View view, final boolean isCreated) {
        ViewGroup root =
                (ViewGroup) getActivity().getWindow()
                        .getDecorView().findViewById(android.R.id.content);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            root.addView(view);
        } else {
            final int index = parent.indexOfChild(view);
            parent.removeView(view);
            parent.addView(view, index);
        }

        final QuickAction quickAction = new QuickAction(getActivity(), QuickAction.HORIZONTAL);
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_YELLOW,
                getResources().getDrawable(R.drawable.ic_yellow_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_GREEN,
                getResources().getDrawable(R.drawable.ic_green_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_BLUE,
                getResources().getDrawable(R.drawable.ic_blue_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_PINK,
                getResources().getDrawable(R.drawable.ic_pink_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_UNDERLINE,
                getResources().getDrawable(R.drawable.ic_underline_marker)));
        final ViewGroup finalRoot = root;
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                finalRoot.removeView(view);
                onHighlightColorsActionItemClicked(actionId, view, isCreated);
            }
        });
        quickAction.show(view);
    }

    private void onHighlightColorsActionItemClicked(int actionId, View view, boolean isCreated) {
        if (actionId == ACTION_ID_HIGHLIGHT_YELLOW) {
            highlight(HighlightImpl.HighlightStyle.Yellow, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_GREEN) {
            highlight(HighlightImpl.HighlightStyle.Green, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_BLUE) {
            highlight(HighlightImpl.HighlightStyle.Blue, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_PINK) {
            highlight(HighlightImpl.HighlightStyle.Pink, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_UNDERLINE) {
            highlight(HighlightImpl.HighlightStyle.Underline, isCreated);
        }
        mTextSelectionSupport.endSelectionMode();
    }

    @Override
    public void resetCurrentIndex() {
        if (isCurrentFragment()) {
            mWebview.loadPage("javascript:rewindCurrentIndex()");
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void onReceiveHighlights(String html) {
        if (html != null) {
            rangy = HighlightUtil.createHighlightRangy(getActivity().getApplicationContext(),
                    html,
                    mBookId,
                    getPageName(),
                    mPosition,
                    rangy);
        }
    }

    private String getPageName() {
        return mBookTitle + "$" + spineItem.href;
    }

    @Override
    public void highLightText(String fragmentId) {
        mWebview.loadPage(String.format(getString(R.string.audio_mark_id), fragmentId));
    }

    @Override
    public void highLightTTS() {
        mWebview.loadPage("javascript:alert(getSentenceWithIndex('epub-media-overlay-playing'))");
    }

    @JavascriptInterface
    public void getUpdatedHighlightId(String id, String style) {
        if (id != null) {
            HighlightImpl highlightImpl = HighLightTable.updateHighlightStyle(id, style);
            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(
                        getActivity().getApplicationContext(),
                        highlightImpl,
                        HighLight.HighLightAction.MODIFY);
            }
            final String rangyString = HighlightUtil.generateRangyString(getPageName());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    loadRangy(mWebview, rangyString);
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isCurrentFragment()) {
            if (outState != null)
                outState.putParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE, lastReadPosition);
            mActivityCallback.storeLastReadPosition(lastReadPosition);
        }
        if (mWebview != null) mWebview.destroy();
    }

    private boolean isCurrentFragment() {
        return isAdded() && mActivityCallback.getChapterPosition() == mPosition;
    }

    @Override
    public void onError() {
    }

    public void scrollToHighlightId(String highlightId) {
        this.highlightId = highlightId;

        if (loadingView != null && loadingView.getVisibility() != View.VISIBLE) {
            loadingView.show();
            mWebview.loadPage(String.format(getString(R.string.go_to_highlight), highlightId));
            this.highlightId = null;
        }
    }

    @Override
    public void showLoading() {
        if (loadingView != null)
            loadingView.visible();
    }
}
