package com.folioreader.util;

import com.sap_press.rheinwerk_reader.mod.models.highlight.Note;

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */

public interface OnHighlightListener {

    /**
     * This method will be invoked when a highlight is created, deleted or modified.
     *
     * @param highlight meta-data for created highlight {@link Note}.
     * @param type      type of event e.g new,edit or delete {@link Note.HighLightAction}.
     */
    void onHighlight(Note highlight, Note.HighLightAction type);
}
