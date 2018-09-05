package com.sap_press.rheinwerk_reader.download.events;

public class DownloadingErrorEvent {
    private int ebookId;

    public DownloadingErrorEvent(int ebookId) {
        this.ebookId = ebookId;
    }

    public int getEbookId() {
        return ebookId;
    }

    public void setEbookId(int ebookId) {
        this.ebookId = ebookId;
    }
}
