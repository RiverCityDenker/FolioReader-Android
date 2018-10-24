package com.sap_press.rheinwerk_reader.download.events;

public class DownloadBasicFileErrorEvent {
    private final boolean isHttpError;

    public DownloadBasicFileErrorEvent(boolean isHttpError) {
        this.isHttpError = isHttpError;
    }

    public boolean isHttpError() {
        return isHttpError;
    }
}
