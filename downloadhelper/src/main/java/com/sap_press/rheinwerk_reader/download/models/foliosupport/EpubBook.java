package com.sap_press.rheinwerk_reader.download.models.foliosupport;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class EpubBook implements Parcelable{
    public ArrayList<Manifest> manifestList = new ArrayList<Manifest>();
    public ArrayList<String> spineList = new ArrayList<String>();
    public String coverPath = null;
    public String tocPath = null;


    public int totalPage;
    public String title;
    public String author;
    public String subject;
    public String publisher;
    public String id;

    public EpubBook() {
    }

    protected EpubBook(Parcel in) {
        manifestList = in.createTypedArrayList(Manifest.CREATOR);
        spineList = in.createStringArrayList();
        coverPath = in.readString();
        tocPath = in.readString();
        totalPage = in.readInt();
        title = in.readString();
        author = in.readString();
        subject = in.readString();
        publisher = in.readString();
        id = in.readString();
    }

    public static final Creator<EpubBook> CREATOR = new Creator<EpubBook>() {
        @Override
        public EpubBook createFromParcel(Parcel in) {
            return new EpubBook(in);
        }

        @Override
        public EpubBook[] newArray(int size) {
            return new EpubBook[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(manifestList);
        dest.writeStringList(spineList);
        dest.writeString(coverPath);
        dest.writeString(tocPath);
        dest.writeInt(totalPage);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(subject);
        dest.writeString(publisher);
        dest.writeString(id);
    }


    public static class Manifest implements Parcelable {
        String _id;
        String href;
        String mediaType;

        public Manifest() {
        }

        public Manifest(String id, String href, String mediaType) {
            this._id = id;
            this.href = href;
            this.mediaType = mediaType;
        }

        protected Manifest(Parcel in) {
            _id = in.readString();
            href = in.readString();
            mediaType = in.readString();
        }

        public static final Creator<Manifest> CREATOR = new Creator<Manifest>() {
            @Override
            public Manifest createFromParcel(Parcel in) {
                return new Manifest(in);
            }

            @Override
            public Manifest[] newArray(int size) {
                return new Manifest[size];
            }
        };

        public String getHref() {
            return href;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(_id);
            dest.writeString(href);
            dest.writeString(mediaType);
        }
    }
}