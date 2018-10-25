package com.folioreader.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.ui.folio.presenter.ImageViewerPresenter;
import com.folioreader.ui.folio.views.ImageViewerView;
import com.folioreader.util.AppUtil;
import com.sap_press.rheinwerk_reader.crypto.CryptoManager;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;

import static com.folioreader.ui.folio.fragment.FolioPageFragment.SLASH_SIGN;
import static com.folioreader.ui.folio.fragment.FolioPageFragment.URL_PREFIX;

public class ImageViewerFragment extends DialogFragment implements ImageViewerView {
    private static final String KEY_EXTRA = "key_extra";
    private static final String KEY_CONTENT = "key_content";
    private static final String KEY_DOWNLOAD_INFO = "key_user";
    private static final String TAG = ImageViewerFragment.class.getSimpleName();
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_BOOK_NAME = "book_name";
    private WebView imageWebView;
    private String imagePath;
    private String mContentKey;
    private DownloadInfo mDownloadInfo;
    private String mMimeType;
    private String mBookName;
    private TextView tvClose;
    private View loadingView;
    private ImageViewerPresenter mPresenter;
    private String configedHtml;
    private String baseUrl;

    public static void startShowImage(String extra,
                                      String bookName,
                                      String mimeType,
                                      String contentKey,
                                      DownloadInfo downloadInfo,
                                      FragmentManager supportFragmentManager) {
        ImageViewerFragment imageViewerFragment = new ImageViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ImageViewerFragment.KEY_EXTRA, extra);
        bundle.putString(ImageViewerFragment.KEY_BOOK_NAME, bookName);
        bundle.putString(ImageViewerFragment.KEY_MIME_TYPE, mimeType);
        bundle.putString(ImageViewerFragment.KEY_CONTENT, contentKey);
        bundle.putParcelable(ImageViewerFragment.KEY_DOWNLOAD_INFO, downloadInfo);
        imageViewerFragment.setArguments(bundle);
        imageViewerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
        imageViewerFragment.show(supportFragmentManager, "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new ImageViewerPresenter(this);
        if (getArguments() != null) {
            imagePath = getArguments().getString(KEY_EXTRA);
            mBookName = getArguments().getString(KEY_BOOK_NAME);
            mMimeType = getArguments().getString(KEY_MIME_TYPE);
            mContentKey = getArguments().getString(KEY_CONTENT);
            mDownloadInfo = getArguments().getParcelable(KEY_DOWNLOAD_INFO);
        }
    }

    private ImageViewerPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.layout_image_viewer, container, false);
        imageWebView = view.findViewById(R.id.imageWebView);
        loadingView = view.findViewById(R.id.loadingView);
        tvClose = view.findViewById(R.id.tv_close);
        tvClose.setOnClickListener(view1 -> dismiss());
        final String filePath = (imagePath.contains("file")) ? imagePath.replace("file://", "") : imagePath;
        final String baseFilePath = filePath.substring(0, filePath.indexOf(mBookName + "/") + mBookName.length() + 1);
        Log.e(TAG, "onCreateView: >>>" + filePath);
        final String htmlString = CryptoManager.decryptContentKey(mContentKey, mDownloadInfo.getmApiKey(), filePath);
        Config mConfig = AppUtil.getSavedConfig(getContext());
        configedHtml = HtmlUtil.reformatHtml(getContext(), htmlString, mConfig);
        baseUrl = URL_PREFIX + baseFilePath + (baseFilePath.endsWith(SLASH_SIGN) ? "" : SLASH_SIGN);
        imageWebView.getSettings().setJavaScriptEnabled(true);
        imageWebView.getSettings().setAllowFileAccess(true);
        imageWebView.getSettings().setSupportZoom(true);
        imageWebView.getSettings().setBuiltInZoomControls(true);
        imageWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        imageWebView.getSettings().setDisplayZoomControls(false);
        imageWebView.setWebViewClient(webViewClient);
        imageWebView.setInitialScale(100);


//        String idref = imagePath.substring(imagePath.indexOf(mBookName + "/") + mBookName.length() + 1, imagePath.lastIndexOf("."));
//        if (isFileExist(getActivity(), mBookName, idref)) {
//            showImage();
//        } else {
//            getPresenter().downloadLinkedFile(getActivity(), mDownloadInfo, mBookName, idref);
//        }

        getPresenter().downloadImage(getActivity(), mDownloadInfo, mBookName, configedHtml);

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
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

    @Override
    public void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void showImage(String downloadResult) {
        Log.e(TAG, "showImage: >>>");
        imageWebView.loadDataWithBaseURL(
                baseUrl,
                configedHtml,
                mMimeType,
                "UTF-8",
                null);
    }
}
