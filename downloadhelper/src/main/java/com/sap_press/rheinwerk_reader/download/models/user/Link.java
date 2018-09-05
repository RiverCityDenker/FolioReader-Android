package com.sap_press.rheinwerk_reader.download.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Link implements Serializable {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("rel")
    @Expose
    private String rel;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

}