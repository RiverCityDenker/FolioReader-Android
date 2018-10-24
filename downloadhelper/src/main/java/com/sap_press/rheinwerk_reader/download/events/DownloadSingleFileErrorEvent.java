package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class DownloadSingleFileErrorEvent {
    private final String title;
    private final String message;
    private Ebook ebook;

    public DownloadSingleFileErrorEvent(Ebook ebook, String title, String message) {
        this.ebook = ebook;
        this.title = title;
        this.message = message;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
