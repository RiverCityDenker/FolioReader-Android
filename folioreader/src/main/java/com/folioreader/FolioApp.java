package com.folioreader;

import android.app.Application;
import android.content.Context;

/**
 * Created by hale on 11/22/2018.
 */
public abstract class FolioApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
