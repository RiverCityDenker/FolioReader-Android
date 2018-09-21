package com.sap_press.rheinwerk_reader.download.datamanager;

import android.content.Context;
import android.content.SharedPreferences;

public class DownloadSharedPref {


    public static final String PREF_KEY_API_KEY = "api-key";
    public static final String PREF_KEY_READING_TYPE_KEY = "reading_type";
    public static String PREF_KEY_ACCESS_TOKEN = "access-token";
    public static String PREF_KEY_SUBTOPIC = "sub-topic";
    public static String PREF_KEY_SUBTOPIC_ID = "SUBTOPIC_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String PASS_WORD = "PASS_WORD";
    public static String PREF_KEY_NUMBER_DOWNLOAD = "NUMBER_DOWNLOAD";
    public static String PREF_KEY_TIME_STAMP_LOGIN = "TIME_STAMP_LOGIN";
    public static String PREF_KEY_AUTO_LOGIN = "AUTO_LOGIN";
    public static String PREF_KEY_USER_INFO_NAME = "USER_INFO_NAME";
    public static String PREF_KEY_APP_MODE = "PREF_KEY_APP_MODE";
    public static String PREF_KEY_BUILD_DEBUG = "PREF_KEY_BUILD_DEBUG";
    public static String PREF_KEY_DIALOG_SKIP = "PREF_KEY_DIALOG_SKIP";
    private static final String PREF_NAME = "download-prefs";

    public static String PREF_KEY_TIME_STAMP_DOWNLOAD(String key) {
        return "PREF_KEY_TIME_STAMP_DOWNLOAD_" + key;
    }

    private static final DownloadSharedPref instance = new DownloadSharedPref();
    private SharedPreferences mSharedPreferences;

    public static DownloadSharedPref getInstance() {
        return instance;
    }

    private DownloadSharedPref() {
    }

    public void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void put(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public void put(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    public void put(String key, float value) {
        mSharedPreferences.edit().putFloat(key, value).apply();
    }

    public void put(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void put(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    public String get(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public Integer get(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public Float get(String key, float defaultValue) {
        return mSharedPreferences.getFloat(key, defaultValue);
    }

    public Boolean get(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public Long get(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public void deleteSavedData(String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public void deleteAllData() {
        mSharedPreferences.edit().clear().apply();
    }
}