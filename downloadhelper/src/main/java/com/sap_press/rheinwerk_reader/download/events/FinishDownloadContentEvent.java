package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class FinishDownloadContentEvent {
    private Ebook ebook;

    public FinishDownloadContentEvent(Ebook ebook) {
        this.ebook = ebook;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }
}
