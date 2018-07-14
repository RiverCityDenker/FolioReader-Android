package com.folioreader.model;

import android.os.Parcelable;

import com.folioreader.FolioReader;
import com.folioreader.ui.folio.presenter.MainMvpView;

/**
 * Interface contract for last read position
 *
 * @author Hrishikesh Kadam on 20/04/2018.
 */
public interface ReadPosition extends Parcelable {

    /**
     * Returns the bookId if sent in {@link FolioReader}'s openBook() else logic defined in
     * {@link MainMvpView#onLoadPublication(com.folioreader.ui.custom.EpubPublicationCustom)} will return the bookId.
     */
    String getBookId();

    /**
     * Returns the idref of the spine item.
     */
    String getChapterId();

    /**
     * Returns the href of the manifest item.
     */
    String getChapterHref();

    /**
     * Returns the chapter index from spine tag
     */
    int getChapterIndex();

    /**
     * Returns true if span tag has id
     */
    boolean isUsingId();

    /**
     * Returns the span id, if usingId true or else returns span index
     */
    String getValue();

    /**
     * Returns the json format of this object
     */
    String toJson();
}
