
package com.sap_press.rheinwerk_reader.download.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sap_press.rheinwerk_reader.download.util.DateUtil;

public class Subscription {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("end_date")
    @Expose
    private String endDate;
    @SerializedName("grace_period_end_date")
    @Expose
    private String gracePeriodEndDate;
    @SerializedName("is_cancelled")
    @Expose
    private boolean isCancelled;
    private boolean isExpired = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getGracePeriodEndDate() {
        return gracePeriodEndDate;
    }

    public void setGracePeriodEndDate(String gracePeriodEndDate) {
        this.gracePeriodEndDate = gracePeriodEndDate;
    }

    public boolean isIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public boolean isValid() {
        return !isCancelled && !DateUtil.isAfterEndDate(endDate);
    }
}
