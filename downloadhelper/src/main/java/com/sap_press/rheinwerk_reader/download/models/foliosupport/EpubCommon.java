package com.sap_press.rheinwerk_reader.download.models.foliosupport;

import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class EpubCommon {
    private String mContentPath;
    private String mBasePath;
    private String mBaseUrl;

    public EpubCommon(String contentPath) {
        mContentPath = contentPath;
        // 3. get base url
        mBaseUrl = mBasePath = mContentPath.substring(0,
                mContentPath.lastIndexOf("/"));

    }

    /**
     * Parses content.opf
     *
     * @return
     * @throws Exception
     */
    public EpubBook parseBookInfo() {
        EpubBook bookInfo = new EpubBook();

        BookInfoSAXParser bookInfoParser = new BookInfoSAXParser();
        try {
            bookInfo = bookInfoParser.getBookInfo(mContentPath);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bookInfo;
    }

    /**
     * Get Base URL
     *
     * @return
     */
    public String getBaseUrl() {
        return mBaseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public String getBasePath() {
        return mBasePath;
    }

    public void setBasePath(String basePath) {
        mBasePath = basePath;
    }

    /**
     * Get Content Path
     *
     * @return
     */
    public String getContentPath() {
        return mContentPath;
    }

}