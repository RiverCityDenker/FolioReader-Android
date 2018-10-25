package com.folioreader.ui.folio.views;

public interface ImageViewerView {
    void showLoading();
    void hideLoading();

    void showImage(String downloadResult);
}
