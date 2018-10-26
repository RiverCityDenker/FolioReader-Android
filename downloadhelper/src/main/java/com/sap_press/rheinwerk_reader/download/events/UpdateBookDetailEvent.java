package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class UpdateBookDetailEvent {
    private final Ebook ebook;

    public UpdateBookDetailEvent(Ebook ebook) {
        this.ebook = ebook;
    }

    public Ebook getEbook() {
        return ebook;
    }
}
