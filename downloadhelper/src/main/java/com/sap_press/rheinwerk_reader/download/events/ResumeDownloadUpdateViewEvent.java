package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class ResumeDownloadUpdateViewEvent {
    private final Ebook ebook;

    public ResumeDownloadUpdateViewEvent(Ebook ebook) {
        this.ebook = ebook;
    }


    public Ebook getEbook() {
        return ebook;
    }
}
