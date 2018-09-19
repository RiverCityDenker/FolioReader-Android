package com.sap_press.rheinwerk_reader.download.events;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

/**
 * Created by hale on 5/9/2018.
 */

public class UpdateBookUIEvent {
    private int position;
    private Ebook ebook;

    public UpdateBookUIEvent(int position, Ebook ebook) {
        this.position = position;
        this.ebook = ebook;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }
}
