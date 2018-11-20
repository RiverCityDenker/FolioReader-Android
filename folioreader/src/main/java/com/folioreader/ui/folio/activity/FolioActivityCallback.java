package com.folioreader.ui.folio.activity;

import com.folioreader.Config;
import com.folioreader.model.ReadPosition;
import com.folioreader.view.LoadingView;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public interface FolioActivityCallback {

    int getChapterPosition();

    void setPagerToPosition(String href);

    ReadPosition getEntryReadPosition();

    void goToChapter(String href);

    Config.Direction getDirection();

    void onDirectionChange(Config.Direction newDirection);

    void storeLastReadPosition(ReadPosition lastReadPosition);

    ReadPosition getLastReadPosition();

    void showSinglePage(String url);

    LoadingView getLoadingView();

    void updateEbook(Ebook ebook);

    DownloadInfo getDownloadInfo();

    boolean wasScrollLeft();
}
