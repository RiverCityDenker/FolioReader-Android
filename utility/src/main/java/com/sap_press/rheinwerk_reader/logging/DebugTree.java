package com.sap_press.rheinwerk_reader.logging;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class DebugTree extends FolioLogging.DebugTree {
    @Override
    protected @Nullable
    String createStackElementTag(@NotNull StackTraceElement element) {
        return String.format("C:%s:%s",
                super.createStackElementTag(element),
                element.getLineNumber());
    }
}