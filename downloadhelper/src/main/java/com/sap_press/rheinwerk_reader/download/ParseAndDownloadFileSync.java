package com.sap_press.rheinwerk_reader.download;

import android.annotation.SuppressLint;
import android.util.Log;

import com.sap_press.rheinwerk_reader.crypto.CryptoManager;
import com.sap_press.rheinwerk_reader.utils.FileUtil;

import org.readium.r2_streamer.parser.EpubParser;
import org.readium.r2_streamer.parser.EpubParserException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hale on 11/1/2018.
 */
public class ParseAndDownloadFileSync {
    private String apiKey;
    private String folderPath;
    private String originalHref;
    private String baseUrl;
    private String ebookId;
    private String token;
    private String appVersion;
    private AtomicInteger downloadedCount = new AtomicInteger();
    private ThreadPoolExecutor poolExecutor = ParallelExecutorTask.createPool();
    private static final String TAG = ParseAndDownloadFileAsyn.class.getSimpleName();

    public interface DownloadFinishCallback {
        void downloadFinish();
    }

    ParseAndDownloadFileSync(String apiKey, String folderPath,
                             String originalHref, String baseUrl,
                             String ebookId, String token,
                             String appVersion) {
        this.apiKey = apiKey;
        this.folderPath = folderPath;
        this.originalHref = originalHref;
        this.baseUrl = baseUrl;
        this.ebookId = ebookId;
        this.token = token;
        this.appVersion = appVersion;
    }

    public void parseAndDownload(String contentKey, DownloadFinishCallback callback) {
        final String html = CryptoManager.decryptContentKey(contentKey, apiKey, getFilePath(folderPath, originalHref));
        try {
            parseHtml(html, callback);
        } catch (EpubParserException e) {
            e.printStackTrace();
        }
    }

    private void parseHtml(String html, DownloadFinishCallback callback) throws EpubParserException {
        Document document = EpubParser.xmlParser(html);
        if (document == null) {
            throw new EpubParserException("Error while parsing");
        }
        ArrayList<String> filesToLoad = new ArrayList<>();

        NodeList itemNodes = document.getElementsByTagNameNS("*", "img");
        if (itemNodes != null) {
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element itemElement = (Element) itemNodes.item(i);

                NamedNodeMap nodeMap = itemElement.getAttributes();
                for (int j = 0; j < nodeMap.getLength(); j++) {
                    Attr attr = (Attr) nodeMap.item(j);
                    switch (attr.getNodeName()) {
                        case "src":
                            final String src = attr.getNodeValue();
                            final String href = FileUtil.reformatHref(src);
                            filesToLoad.add(href);
                            break;
                    }
                }
            }
        }

        NodeList linkNodes = document.getElementsByTagNameNS("*", "a");
        if (linkNodes != null) {
            List<String> srcList = new ArrayList<>();
            for (int i = 0; i < linkNodes.getLength(); i++) {
                Element itemElement = (Element) linkNodes.item(i);
                NamedNodeMap nodeMap = itemElement.getAttributes();
                for (int j = 0; j < nodeMap.getLength(); j++) {
                    Attr attr = (Attr) nodeMap.item(j);
                    if (attr.getNodeName().equalsIgnoreCase("href")) {
                        final String src = attr.getNodeValue();
                        if (!srcList.contains(src) && src.contains(".html")) {
                            final String href = FileUtil.reformatHref(src);
                            srcList.add(src);
                            filesToLoad.add(href);
                        }
                        break;
                    }
                }
            }
        }

        if (filesToLoad.isEmpty()) {
            callback.downloadFinish();
            return;
        }

        for (String s : filesToLoad) {
            @SuppressLint("StaticFieldLeak") ParallelExecutorTask task = new ParallelExecutorTask(poolExecutor) {
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {
                        final String fileUrl = baseUrl + "ebooks/" + ebookId
                                + "/download?app_version=" + appVersion
                                + "&file_path=" + s;
                        HTTPDownloader.downloadFile(fileUrl, token, folderPath, s, appVersion);
                        checkDownloadFinished();
                    } catch (Exception e) {
                        checkDownloadFinished();
                        Log.e(TAG, "parseHtml:parse Link >>>" + e.getMessage());
                    }

                    return null;
                }

                private void checkDownloadFinished() {
                    int current = downloadedCount.incrementAndGet();
                    if (current == filesToLoad.size()) {
                        //all download finished
                        callback.downloadFinish();
                    }
                }
            };

            task.executeParallel();
        }
    }

    private String getFilePath(String folderPath, String originalHref) {
        folderPath = folderPath.endsWith("/")
                ? folderPath.substring(0, folderPath.length() - 1)
                : folderPath;
        originalHref = originalHref.startsWith("/")
                ? originalHref.substring(1, originalHref.length())
                : originalHref;
        return folderPath + "/" + originalHref;
    }
}
