
package com.sap_press.rheinwerk_reader.download.models.ebooks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Author implements Parcelable {

    @SerializedName("images")
    @Expose
    private List<Image> images = null;
    @SerializedName("info")
    @Expose
    private String info;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("first_name")
    @Expose
    private String firstName;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.images);
        dest.writeString(this.info);
        dest.writeString(this.lastName);
        dest.writeInt(this.id);
        dest.writeString(this.firstName);
    }

    public Author() {
    }

    protected Author(Parcel in) {
        this.images = new ArrayList<Image>();
        in.readList(this.images, Image.class.getClassLoader());
        this.info = in.readString();
        this.lastName = in.readString();
        this.id = in.readInt();
        this.firstName = in.readString();
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel source) {
            return new Author(source);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
}
