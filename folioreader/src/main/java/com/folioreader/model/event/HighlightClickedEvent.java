package com.folioreader.model.event;

import com.folioreader.model.HighlightImpl;

public class HighlightClickedEvent {
    private final HighlightImpl highlightImpl;

    public HighlightClickedEvent(HighlightImpl highlightImpl) {
        this.highlightImpl = highlightImpl;
    }

    public HighlightImpl getHighlightImpl() {
        return highlightImpl;
    }

}


