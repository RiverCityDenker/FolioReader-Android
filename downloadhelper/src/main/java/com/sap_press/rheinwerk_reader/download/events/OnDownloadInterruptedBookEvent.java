package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class OnDownloadInterruptedBookEvent {
    private final Ebook ebook;

    public OnDownloadInterruptedBookEvent(Ebook ebook) {
        this.ebook = ebook;
    }

    public Ebook getEbook() {
        return ebook;
    }
}
