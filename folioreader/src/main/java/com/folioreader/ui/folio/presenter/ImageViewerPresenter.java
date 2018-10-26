package com.folioreader.ui.folio.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.folioreader.R;
import com.folioreader.ui.folio.views.ImageViewerView;
import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.mod.models.apiinfo.ApiInfo;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;

import org.readium.r2_streamer.parser.EpubParser;
import org.readium.r2_streamer.parser.EpubParserException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;

import static com.sap_press.rheinwerk_reader.download.DownloadService.downloadFile;
import static com.sap_press.rheinwerk_reader.download.DownloadService.onDownloadSingleFileError;
import static com.sap_press.rheinwerk_reader.utils.FileUtil.getEbookPath;
import static com.sap_press.rheinwerk_reader.utils.FileUtil.isFileExist;
import static com.sap_press.rheinwerk_reader.utils.Util.isOnline;

public class ImageViewerPresenter {
    private static final String TAG = ImageViewerPresenter.class.getSimpleName();
    static final String DOWNLOAD_IMAGE_SUCCESS = "download_success";
    static final String TABLE_TYPE = "table_type";
    private static ImageViewerView mvpView;
    private static DownloadDataManager dataManager;

    public ImageViewerPresenter(ImageViewerView mvpView) {
        this.mvpView = mvpView;
        this.dataManager = DownloadDataManager.getInstance();
    }

    public void downloadLinkedFile(Context context, DownloadInfo downloadInfo, String ebookId, String href) {
        showLoading();
        startDownloadFile(context, downloadInfo, ebookId, href);
    }

    private void startDownloadFile(Context context, DownloadInfo downloadInfo, String ebookId, String href) {
        final Ebook ebook = dataManager.getEbookById(Integer.parseInt(ebookId));
        final ApiInfo apiInfo = new ApiInfo(downloadInfo.getmBaseUrl(),
                dataManager.getAccessToken(),
                downloadInfo.getmApiKey(),
                downloadInfo.getmAppVersion());

        final String folderPath = getEbookPath(context, String.valueOf(ebook.getId()));
        new DownloadService.DownloadFileTask(context, ebook, apiInfo, folderPath, false).execute(href);
    }

    private void showLoading() {
        if (this.mvpView != null)
            this.mvpView.showLoading();
    }

    public void downloadImage(Context context, DownloadInfo downloadInfo, String ebookId, String html) {
        new XmlParserAsyn(context, downloadInfo, ebookId).execute(html);
    }

    static class XmlParserAsyn extends AsyncTask<String, Void, String> {
        private DownloadInfo downloadInfo;
        private String eBookId;
        private WeakReference<Context> contextWeakReference;

        XmlParserAsyn(Context context, DownloadInfo downloadInfo, String eBookId) {
            this.contextWeakReference = new WeakReference<>(context);
            this.downloadInfo = downloadInfo;
            this.eBookId = eBookId;
        }

        @Override
        protected String doInBackground(String... strings) {
            Document document = null;
            try {
                document = EpubParser.xmlParser(strings[0]);
            } catch (EpubParserException e) {
                e.printStackTrace();
            }
            if (document != null && contextWeakReference != null) {
                NodeList itemNodes = document.getElementsByTagNameNS("*", "img");
                if (itemNodes != null) {
                    for (int i = 0; i < itemNodes.getLength(); i++) {
                        Element itemElement = (Element) itemNodes.item(i);

                        NamedNodeMap nodeMap = itemElement.getAttributes();
                        for (int j = 0; j < nodeMap.getLength(); j++) {
                            Attr attr = (Attr) nodeMap.item(j);
                            if (attr.getNodeName().equalsIgnoreCase("src")) {
                                final String src = attr.getNodeValue();
                                final String href = FileUtil.reformatHref(src);
                                if (!isFileExist(contextWeakReference.get(), eBookId, href)) {
                                    try {
                                        final String fileUrl = downloadInfo.getmBaseUrl() + "ebooks/" + eBookId
                                                + "/download?app_version=" + downloadInfo.getmAppVersion()
                                                + "&file_path=" + href;
                                        final String folderPath = getEbookPath(contextWeakReference.get(), eBookId);
                                        downloadFile(fileUrl, dataManager.getAccessToken(), folderPath, href, downloadInfo.getmAppVersion());
                                        return DOWNLOAD_IMAGE_SUCCESS;
                                    } catch (Exception e) {
                                        Log.e(TAG, "parseHtml:parse Image >>>" + e.getMessage());
                                        showErrorPage(e);
                                        return null;
                                    }
                                } else {
                                    Log.e(TAG, "doInBackground: >>>File is already exist");
                                    return DOWNLOAD_IMAGE_SUCCESS;
                                }

                            }
                        }
                    }
                }
                NodeList tableNodes = document.getElementsByTagNameNS("*", "table");
                if (tableNodes != null && tableNodes.getLength() > 0) {
                    return TABLE_TYPE;
                }
            } else {
                showErrorPage(new FileNotFoundException());
            }

            return null;
        }

        private void showErrorPage(Exception e) {
            String title;
            String message;
            final Context context = contextWeakReference.get();
            if (e instanceof UnknownHostException) {
                title = context.getResources().getString(R.string.download_error_from_offline_title);
                message = context.getResources().getString(R.string.download_error_from_offline_message);
            } else {
                title = context.getResources().getString(R.string.download_error_from_file_title);
                message = context.getResources().getString(R.string.download_error_from_file_message);
            }
            if (mvpView != null) {
                mvpView.showErrorWhenLoadImage(title, message);
            }
        }

        @Override
        protected void onPostExecute(String downloadResult) {
            if (mvpView != null) {
                mvpView.hideLoading();
                Log.e(TAG, "onPostExecute: >>>downloadResult = " + downloadResult);
                if (!TextUtils.isEmpty(downloadResult)) {
                    mvpView.showImage(downloadResult);
                }
            }

        }
    }
}
