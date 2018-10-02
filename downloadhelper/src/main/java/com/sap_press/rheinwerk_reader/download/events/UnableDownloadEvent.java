package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import java.util.List;

/**
 * Created by hale on 7/19/2018.
 */
public class UnableDownloadEvent {

    public enum DownloadErrorType {
        DISCONNECTED,
        NOT_ENOUGH_SPACE
    }

    private DownloadErrorType errorType;
    private List<Ebook> ebookList;

    public UnableDownloadEvent(List<Ebook> downloadingEbookList, DownloadErrorType errorType) {
        this.ebookList = downloadingEbookList;
        this.errorType = errorType;
    }

    public UnableDownloadEvent(DownloadErrorType errorType) {
        this.errorType = errorType;
    }

    public DownloadErrorType getErrorType() {
        return errorType;
    }

    public List<Ebook> getEbookList() {
        return ebookList;
    }
}
