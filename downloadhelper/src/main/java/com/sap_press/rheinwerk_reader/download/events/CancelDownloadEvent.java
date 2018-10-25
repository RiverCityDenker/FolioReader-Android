package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class CancelDownloadEvent {
    private final Ebook ebook;
    private final boolean isFullDelete;

    public CancelDownloadEvent(Ebook ebook, boolean isFullDelete) {
        this.ebook = ebook;
        this.isFullDelete = isFullDelete;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public boolean isFullDelete() {
        return isFullDelete;
    }
}
