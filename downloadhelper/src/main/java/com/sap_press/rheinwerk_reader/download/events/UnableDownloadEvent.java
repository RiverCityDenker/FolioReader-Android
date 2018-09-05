package com.sap_press.rheinwerk_reader.download.events;

/**
 * Created by hale on 7/19/2018.
 */
public class UnableDownloadEvent {

    public enum DownloadErrorType {
        DISCONNECTED,
        NOT_ENOUGH_SPACE
    }

    private DownloadErrorType errorType;

    public UnableDownloadEvent() {
    }

    public UnableDownloadEvent(DownloadErrorType errorType) {
        this.errorType = errorType;
    }

    public DownloadErrorType getErrorType() {
        return errorType;
    }
}
