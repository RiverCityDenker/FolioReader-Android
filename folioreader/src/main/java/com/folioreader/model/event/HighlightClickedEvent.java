package com.folioreader.model.event;

import com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2;

public class HighlightClickedEvent {
    private final HighlightV2 highlightV2;

    public HighlightClickedEvent(HighlightV2 highlightV2) {
        this.highlightV2 = highlightV2;
    }

    public HighlightV2 getHighlight() {
        return highlightV2;
    }

}


