package com.folioreader.ui.folio.presenter;

import com.folioreader.ui.base.BaseMvpView;
import com.folioreader.ui.custom.EpubPublicationCustom;

/**
 * @author gautam chibde on 8/6/17.
 */

public interface MainMvpView extends BaseMvpView {
    void onLoadPublication(EpubPublicationCustom publication);
}
