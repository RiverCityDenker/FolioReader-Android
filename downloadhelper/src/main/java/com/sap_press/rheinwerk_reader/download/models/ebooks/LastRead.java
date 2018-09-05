
package com.sap_press.rheinwerk_reader.download.models.ebooks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LastRead implements Parcelable {

    @SerializedName("position")
    @Expose
    private String position;
    @SerializedName("product_id")
    @Expose
    private int productId;
    @SerializedName("file_name")
    @Expose
    private String fileName;
    @SerializedName("user_id")
    @Expose
    private String userId;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.position);
        dest.writeInt(this.productId);
        dest.writeString(this.fileName);
        dest.writeString(this.userId);
    }

    public LastRead() {
    }

    protected LastRead(Parcel in) {
        this.position = in.readString();
        this.productId = in.readInt();
        this.fileName = in.readString();
        this.userId = in.readString();
    }

    public static final Creator<LastRead> CREATOR = new Creator<LastRead>() {
        @Override
        public LastRead createFromParcel(Parcel source) {
            return new LastRead(source);
        }

        @Override
        public LastRead[] newArray(int size) {
            return new LastRead[size];
        }
    };
}
