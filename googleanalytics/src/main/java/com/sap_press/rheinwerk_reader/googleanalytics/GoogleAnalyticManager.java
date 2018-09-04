package com.sap_press.rheinwerk_reader.googleanalytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

/**
 * Created by hale on 4/24/2018.
 */

public class GoogleAnalyticManager {
    private Tracker tracker;
    private final Context context;
    /*TRACKER_ID TEST*/
//    private String TRACKER_ID = "UA-118010828-1";

    public GoogleAnalyticManager(Context context) {
        this.context = context;
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        tracker = analytics.newTracker("UA-57584712-7");
        tracker.enableAutoActivityTracking(false);
        tracker.setAnonymizeIp(true);
        tracker.enableAdvertisingIdCollection(false);
        if (BuildConfig.DEBUG) {
            tracker.setSessionTimeout(5);
        }
    }

    public void sendScreen(String name) {
        boolean isAnalyticEnable = isAnalyticEnable();
        if (isAnalyticEnable) {
            tracker.setScreenName(name);
            Map<String, String> params = new HitBuilders.ScreenViewBuilder().build();
            tracker.send(params);
        }
    }

    public boolean isAnalyticEnable() {
        return KeyValueStoreUtil.getSharedPreferencesBoolean(context, PrefKeys.IS_ANALYTIC_ENABLE, true);
    }

    public void setAnalyticEnable(boolean enable) {
        KeyValueStoreUtil.putSharedPreferencesBoolean(context, PrefKeys.IS_ANALYTIC_ENABLE, enable);
    }

    public void sendEvent(String category, String action) {
        sendEvent(category, action, null);
    }

    public void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, -1);
    }

    public void sendEvent(String category, String action, String label, long value) {
        boolean isAnalyticEnable = isAnalyticEnable();
        if (isAnalyticEnable) {
            HitBuilders.EventBuilder builders = new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action);
            if (label != null) {
                builders.setLabel(label);
            }
            if (value != -1) {
                builders.setValue(value);
            }

            tracker.send(builders.build());
        }
    }
}
