package com.sap_press.rheinwerk_reader.download.events;

public class DownloadingEvent {
    int ebookId;
    int progress;

    public DownloadingEvent(int ebookId, int progress) {
        this.ebookId = ebookId;
        this.progress = progress;
    }

    public int getEbookId() {
        return ebookId;
    }

    public void setEbookId(int ebookId) {
        this.ebookId = ebookId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
