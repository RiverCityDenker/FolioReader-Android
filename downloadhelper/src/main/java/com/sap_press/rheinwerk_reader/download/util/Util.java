package com.sap_press.rheinwerk_reader.download.util;

import android.app.ActivityManager;
import android.content.Context;

public class Util {

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        if (context != null) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
