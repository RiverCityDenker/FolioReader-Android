package com.folioreader.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;
import com.folioreader.util.UiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zweidenker.rheinwerk_reader.crypto.CryptoManager;

import static com.folioreader.ui.folio.activity.FolioActivity.EpubSourceType.ENCRYPTED_FILE;
import static com.folioreader.ui.folio.fragment.FolioPageFragment.SLASH_SIGN;
import static com.folioreader.ui.folio.fragment.FolioPageFragment.URL_PREFIX;

public class ImageViewerFragment extends DialogFragment {
    private static final String KEY_EXTRA = "key_extra";
    private static final String KEY_CONTENT = "key_content";
    private static final String KEY_USER = "key_user";
    private static final String TAG = ImageViewerFragment.class.getSimpleName();
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_BOOK_NAME = "book_name";
    private WebView imageWebView;
    private String imagePath;
    private String mContentKey;
    private String mUserKey;
    private String mMimeType;
    private String mBookName;

    public static void startShowImage(String extra, String bookName, String mimeType, String contentKey, String userKey, FragmentManager supportFragmentManager) {
        ImageViewerFragment imageViewerFragment = new ImageViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ImageViewerFragment.KEY_EXTRA, extra);
        bundle.putString(ImageViewerFragment.KEY_BOOK_NAME, bookName);
        bundle.putString(ImageViewerFragment.KEY_MIME_TYPE, mimeType);
        bundle.putString(ImageViewerFragment.KEY_CONTENT, contentKey);
        bundle.putString(ImageViewerFragment.KEY_USER, userKey);
        imageViewerFragment.setArguments(bundle);
        imageViewerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle);
        imageViewerFragment.show(supportFragmentManager, "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePath = getArguments().getString(KEY_EXTRA);
            mBookName = getArguments().getString(KEY_BOOK_NAME);
            mMimeType = getArguments().getString(KEY_MIME_TYPE);
            mContentKey = getArguments().getString(KEY_CONTENT);
            mUserKey = getArguments().getString(KEY_USER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.layout_image_viewer, container, false);
        imageWebView = view.findViewById(R.id.imageWebView);
        final String filePath = (imagePath.contains("file")) ? imagePath.replace("file://", "") : imagePath;
        final String baseFilePath = filePath.substring(0, filePath.indexOf(mBookName + "/") + mBookName.length() + 1);
        Log.e(TAG, "onCreateView: >>>" + filePath);
        final String htmlString = CryptoManager.decryptContentKey(mContentKey, mUserKey, filePath);

        final String baseUrl = URL_PREFIX + baseFilePath + SLASH_SIGN;
        Config mConfig = AppUtil.getSavedConfig(getContext());
        imageWebView.getSettings().setJavaScriptEnabled(true);
        imageWebView.getSettings().setAllowFileAccess(true);
        imageWebView.getSettings().setSupportZoom(true);
        imageWebView.getSettings().setBuiltInZoomControls(true);
        imageWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        imageWebView.getSettings().setDisplayZoomControls(false);
        imageWebView.setWebViewClient(webViewClient);
        imageWebView.setInitialScale(100);
        imageWebView.loadDataWithBaseURL(
                baseUrl,
                HtmlUtil.getHtmlContent(getContext(), htmlString, mConfig),
                mMimeType,
                "UTF-8",
                null);
        return view;
    }


    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
    };
}
