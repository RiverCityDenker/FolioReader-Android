package com.folioreader.model.event;

/**
 * Created by hale on 7/10/2018.
 */
public class OfflineExpiredEvent {
    boolean isExpiredOffline;

    public OfflineExpiredEvent(boolean isExpiredOffline) {
        this.isExpiredOffline = isExpiredOffline;
    }

    public boolean isExpiredOffline() {
        return isExpiredOffline;
    }

    public void setExpiredOffline(boolean expiredOffline) {
        isExpiredOffline = expiredOffline;
    }
}
