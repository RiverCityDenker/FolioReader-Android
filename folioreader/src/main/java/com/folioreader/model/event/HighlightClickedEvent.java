package com.folioreader.model.event;

import com.sap_press.rheinwerk_reader.mod.models.highlight.Note;

public class HighlightClickedEvent {
    private final Note note;

    public HighlightClickedEvent(Note note) {
        this.note = note;
    }

    public Note getHighlight() {
        return note;
    }

}


