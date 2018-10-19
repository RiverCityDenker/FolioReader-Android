package com.folioreader.ui.folio.views;

import com.folioreader.ui.base.BaseMvpView;
import com.folioreader.ui.custom.EpubPublicationCustom;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

/**
 * @author gautam chibde on 8/6/17.
 */

public interface MainMvpView extends BaseMvpView {
    void onLoadPublication(EpubPublicationCustom publication);

    void updateUIAfterDelete(Ebook ebook);

    void updateDownloadProgress(int id, int progress);

    void showDownloadError(String message);

    void showLoading();

    void hideLoading();

    void exitReader();
}
