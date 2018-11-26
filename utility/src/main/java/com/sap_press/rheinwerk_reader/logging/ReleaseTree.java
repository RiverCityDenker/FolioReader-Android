package com.sap_press.rheinwerk_reader.logging;

import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class ReleaseTree extends FolioLogging.Tree {
    @Override
    protected void log(int priority, @Nullable String tag,
                       @NotNull String message, @Nullable Throwable t) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            Log.e(tag, message);
        }
    }
}