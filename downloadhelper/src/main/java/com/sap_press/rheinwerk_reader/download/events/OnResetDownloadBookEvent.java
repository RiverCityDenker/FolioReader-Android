package com.sap_press.rheinwerk_reader.download.events;

public class OnResetDownloadBookEvent {
    private final int bookId;
    private final boolean isDownloadFailed;

    public OnResetDownloadBookEvent(int bookId, boolean isDownloadFailed) {
        this.bookId = bookId;
        this.isDownloadFailed = isDownloadFailed;
    }

    public boolean isDownloadFailed() {
        return isDownloadFailed;
    }

    public int getBookId() {
        return bookId;
    }
}
