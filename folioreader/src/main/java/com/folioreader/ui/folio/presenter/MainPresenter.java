package com.folioreader.ui.folio.presenter;

import android.content.Context;

import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;
import com.folioreader.ui.custom.EpubPublicationCustom;
import com.sap_press.rheinwerk_reader.download.DownloadManager;
import com.sap_press.rheinwerk_reader.download.events.UpdateBookUIEvent;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import org.greenrobot.eventbus.EventBus;

/**
 * @author gautam chibde on 8/6/17.
 */

public class MainPresenter implements ManifestCallBack {
    private MainMvpView mainMvpView;
    private final DownloadManager downloadManager;

    public MainPresenter(MainMvpView mainMvpView) {
        this.mainMvpView = mainMvpView;
        downloadManager = DownloadManager.getInstance();
    }

    public void parseManifest(String url) {
        new ManifestTask(this).execute(url);
    }

    @Override
    public void onReceivePublication(EpubPublicationCustom publication) {
        mainMvpView.onLoadPublication(publication);
    }

    @Override
    public void onError() {
        mainMvpView.onError();
    }


    public void deleteEbook(Context context, Ebook ebook,
                            GoogleAnalyticManager googleAnalyticManager, int currentPosition) {
        downloadManager.deleteEbook(context, ebook, googleAnalyticManager, new DownloadManager.EbookDeleteCallback() {
            @Override
            public void deleteEbookSuccess(Ebook ebook) {
                updateDetailUIAfterDelete(ebook);
                EventBus.getDefault().post(new UpdateBookUIEvent(currentPosition, ebook));
            }

            @Override
            public void deleteEbookTriggered() {
            }
        });
    }

    private void updateDetailUIAfterDelete(Ebook ebook) {
        if (mainMvpView != null) {
            mainMvpView.updateUIAfterDelete(ebook);
        }
    }
}
