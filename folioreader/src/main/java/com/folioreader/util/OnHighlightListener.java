package com.folioreader.util;

import com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2;

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */

public interface OnHighlightListener {

    /**
     * This method will be invoked when a highlight is created, deleted or modified.
     *
     * @param highlight meta-data for created highlight {@link HighlightV2}.
     * @param type      type of event e.g new,edit or delete {@link com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2.HighLightAction}.
     */
    void onHighlight(HighlightV2 highlight, HighlightV2.HighLightAction type);
}
