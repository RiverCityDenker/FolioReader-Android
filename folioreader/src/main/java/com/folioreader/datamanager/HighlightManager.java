package com.folioreader.datamanager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.sap_press.rheinwerk_reader.logging.FolioLogging;
import com.sap_press.rheinwerk_reader.mod.aping.api.ApiClient;
import com.sap_press.rheinwerk_reader.mod.aping.api.ApiService;
import com.sap_press.rheinwerk_reader.mod.models.highlight.Note;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

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

    public void addHighlight(Note highlightItem) {
        FolioLogging.tag(TAG).d("SEND NOTE = " + new Gson().toJson(highlightItem));
        // Add to server
        final String token = dataManager.getAccessToken();
        final Disposable subscription = mApiService.addNote(token, highlightItem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::addHighlightSuccess, this::addHighlightFailed);
        compositeSubscription.add(subscription);
    }

    public Observable<List<Note>> getAllNotesByBookId(String productId) {
        final String token = dataManager.getAccessToken();
        return mApiService.getAllNotesByBookId(token, productId);
    }

    public Observable<Response<Void>> deleteNoteById(String noteId) {
        final String token = dataManager.getAccessToken();
        return mApiService.deleteNoteById(noteId, token);
    }


    private void addHighlightFailed(Throwable throwable) {
        Log.e(TAG, "addHighlightFailed: >>>" + throwable.getMessage());
    }

    private void addHighlightSuccess(Note note) {
        Log.e(TAG, "addHighlightSuccess: >>>" + new Gson().toJson(note));
    }
}
