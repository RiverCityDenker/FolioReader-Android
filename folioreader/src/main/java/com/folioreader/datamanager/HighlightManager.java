package com.folioreader.datamanager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.sap_press.rheinwerk_reader.mod.aping.api.ApiClient;
import com.sap_press.rheinwerk_reader.mod.aping.api.ApiService;
import com.sap_press.rheinwerk_reader.mod.models.notes.AddNoteResponse;
import com.sap_press.rheinwerk_reader.mod.models.notes.NoteRequestBody;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HighlightManager {

    private static final String TAG = HighlightManager.class.getSimpleName();
    private final FolioDataManager dataManager;
    private ApiService mApiService;
    private String mBaseUrl;
    CompositeDisposable compositeSubscription;

    public HighlightManager(Context context, String baseUrl) {
        this.mBaseUrl = baseUrl;
        this.compositeSubscription = new CompositeDisposable();
        this.dataManager = FolioDataManager.getInstance();
        this.mApiService = ApiClient.getClient(context, mBaseUrl).create(ApiService.class);
    }

    public void addHighlight(NoteRequestBody noteRequestBody) {
        // Add to server
        final String token = dataManager.getAccessToken();
        final Disposable subscription = mApiService.addNote(token, noteRequestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::addHighlightSuccess, this::addHighlightFailed);
        compositeSubscription.add(subscription);
    }

    private void addHighlightFailed(Throwable throwable) {
        Log.e(TAG, "addHighlightFailed: >>>" + throwable.getMessage());
    }

    private void addHighlightSuccess(AddNoteResponse addNoteResponse) {
        Log.e(TAG, "addHighlightSuccess: >>>" + new Gson().toJson(addNoteResponse));
    }
}
