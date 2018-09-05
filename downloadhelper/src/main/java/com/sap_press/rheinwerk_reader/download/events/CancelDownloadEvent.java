package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;

public class CancelDownloadEvent {
    Ebook ebook;

    public CancelDownloadEvent(Ebook ebook) {
        this.ebook = ebook;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }
}
