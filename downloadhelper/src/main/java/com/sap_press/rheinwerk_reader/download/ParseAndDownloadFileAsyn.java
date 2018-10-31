package com.sap_press.rheinwerk_reader.download;

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

/**
 * Created by hale on 31.10.2018.
 */
public class ParseAndDownloadFileAsyn extends ParallelExecutorTask<String, Void, Void> {
    private String apiKey;
    private String folderPath;
    private String originalHref;
    private String baseUrl;
    private String ebookId;
    private String token;
    private String appVersion;
    private static final String TAG = ParseAndDownloadFileAsyn.class.getSimpleName();

    ParseAndDownloadFileAsyn(String apiKey, String folderPath,
                             String originalHref, String baseUrl,
                             String ebookId, String token,
                             String appVersion, ThreadPoolExecutor poolExecutor) {
        super(poolExecutor);
        this.apiKey = apiKey;
        this.folderPath = folderPath;
        this.originalHref = originalHref;
        this.baseUrl = baseUrl;
        this.ebookId = ebookId;
        this.token = token;
        this.appVersion = appVersion;
    }

    @Override
    protected Void doInBackground(String... strings) {
        final String contentKey = strings[0];
        final String html = CryptoManager.decryptContentKey(contentKey, apiKey, getFilePath(folderPath, originalHref));
        try {
            parseHtml(html);
        } catch (EpubParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseHtml(String html) throws EpubParserException {
        Document document = EpubParser.xmlParser(html);
        if (document == null) {
            throw new EpubParserException("Error while parsing");
        }
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
                            try {
                                final String fileUrl = baseUrl + "ebooks/" + ebookId
                                        + "/download?app_version=" + appVersion
                                        + "&file_path=" + href;
                                HTTPDownloader.downloadFile(fileUrl, token, folderPath, href, appVersion);
                            } catch (Exception e) {
                                Log.e(TAG, "parseHtml:parse Image >>>" + e.getMessage());
                            }
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
                            try {
                                final String fileUrl = baseUrl + "ebooks/" + ebookId
                                        + "/download?app_version=" + appVersion
                                        + "&file_path=" + href;
                                HTTPDownloader.downloadFile(fileUrl, token, folderPath, href, appVersion);
                            } catch (Exception e) {
                                Log.e(TAG, "parseHtml:parse Link >>>" + e.getMessage());
                            }
                        }
                        break;
                    }
                }
            }
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