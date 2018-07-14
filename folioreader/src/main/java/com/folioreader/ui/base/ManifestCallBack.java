package com.folioreader.ui.base;

import com.folioreader.ui.custom.EpubPublicationCustom;

/**
 * @author by gautam chibde on 12/6/17.
 */

public interface ManifestCallBack extends BaseMvpView {

    void onReceivePublication(EpubPublicationCustom publication);
}
