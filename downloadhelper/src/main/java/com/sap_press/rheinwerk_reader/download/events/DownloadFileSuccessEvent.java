package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class DownloadFileSuccessEvent {
    private final String href;
    private final Ebook ebook;

    public DownloadFileSuccessEvent(Ebook ebook, String href) {
        this.ebook = ebook;
        this.href = href;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public String getHref() {
        return href;
    }
}
