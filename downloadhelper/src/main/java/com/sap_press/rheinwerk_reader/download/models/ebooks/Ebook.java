
package com.sap_press.rheinwerk_reader.download.models.ebooks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sap_press.rheinwerk_reader.download.util.Converters;
import com.sap_press.rheinwerk_reader.download.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Ebook implements Parcelable {

    @SerializedName("isbn")
    @Expose
    private String isbn;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("last_read")
    @Expose
    private List<LastRead> lastRead = null;
    @SerializedName("is_standalone")
    @Expose
    private boolean isStandalone;
    @SerializedName("claim")
    @Expose
    private String claim;
    @SerializedName("edition_text")
    @Expose
    private String editionText;
    @SerializedName("page_number")
    @Expose
    private int pageNumber;
    @SerializedName("highlights")
    @Expose
    private List<String> highlights = null;
    @SerializedName("edition_number")
    @Expose
    private int editionNumber;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("subscription_ids")
    @Expose
    private List<Long> subscriptionIds = null;
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("keywords")
    @Expose
    private List<String> keywords = null;
    @SerializedName("file_size")
    @Expose
    private int fileSize;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("topic_ids")
    @Expose
    private List<Long> topicIds = null;
    @SerializedName("covers")
    @Expose
    private List<Cover> covers = null;
    @SerializedName("publisher")
    @Expose
    private String publisher;
    @SerializedName("copyright_year")
    @Expose
    private int copyrightYear;
    @SerializedName("usps")
    @Expose
    private List<String> usps = null;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("authors")
    @Expose
    private List<Author> authors = null;
    @SerializedName("_links")
    @Expose
    private List<Link> links = null;
    private int progress = -1;
    private String filePath;
    private String contentKey = "";
    private boolean isFavoriten = false;
    private boolean needSyncToServer = false;
    private String lastReadTime;
    private Long downloadTimeStamp = 0L;
    private int total;

    public boolean isNeedSyncToServer() {
        return needSyncToServer;
    }

    public void setNeedSyncToServer(boolean needSyncToServer) {
        this.needSyncToServer = needSyncToServer;
    }

    public String getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(String lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<LastRead> getLastRead() {
        return lastRead;
    }

    public String getLastReadString() {
        return Converters.fromArrayList(lastRead);
    }

    public void setLastRead(List<LastRead> lastRead) {
        this.lastRead = lastRead;
    }

    public boolean isStandalone() {
        return isStandalone;
    }

    public boolean getStandalone() {
        return isStandalone;
    }

    public void setIsStandalone(boolean isStandalone) {
        this.isStandalone = isStandalone;
    }

    public void setStandalone(boolean isStandalone) {
        this.isStandalone = isStandalone;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getEditionText() {
        return editionText;
    }

    public void setEditionText(String editionText) {
        this.editionText = editionText;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public String getHighlightsString() {
        return Converters.fromArrayList(highlights);
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public void setHighlightString(String highlights) {
        this.highlights = StringUtil.isJSONValid(highlights) ? Converters.fromString(highlights, String[].class) : new ArrayList<>();
    }

    public int getEditionNumber() {
        return editionNumber;
    }

    public void setEditionNumber(int editionNumber) {
        this.editionNumber = editionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getSubscriptionIds() {
        return subscriptionIds;
    }

    public String getSubscriptionIdsString() {
        return Converters.fromArrayList(subscriptionIds);
    }

    public void setSubscriptionIds(List<Long> subscriptionIds) {
        this.subscriptionIds = subscriptionIds;
    }

    public void setSubscriptionIdsString(String subscriptionIds) {
        this.subscriptionIds = StringUtil.isJSONValid(subscriptionIds) ? Converters.fromString(subscriptionIds, Long[].class) : new ArrayList<>();
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getKeywordsString() {
        return Converters.fromArrayList(keywords);
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setKeywordsString(String keywords) {
        this.keywords = StringUtil.isJSONValid(keywords) ? Converters.fromString(keywords, String[].class) : new ArrayList<>();
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Long> getTopicIds() {
        return topicIds;
    }

    public String getTopicIdsString() {
        return Converters.fromArrayList(topicIds);
    }

    public void setTopicIds(List<Long> topicIds) {
        this.topicIds = topicIds;
    }

    public void setTopicIdsString(String topicIds) {
        this.topicIds = StringUtil.isJSONValid(topicIds) ? Converters.fromString(topicIds, Long[].class) : new ArrayList<>();
    }

    public List<Cover> getCovers() {
        return covers;
    }

    public String getCoversString() {
        return Converters.fromArrayList(covers);
    }

    public void setCovers(List<Cover> covers) {
        this.covers = covers;
    }

    public void setCoversString(String covers) {
        this.covers = new ArrayList<>();
        if (StringUtil.isJSONValid(covers)) {
            this.covers.addAll(Converters.fromString(covers, Cover[].class));
        }
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getCopyrightYear() {
        return copyrightYear;
    }

    public void setCopyrightYear(int copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public List<String> getUsps() {
        return usps;
    }

    public void setUsps(List<String> usps) {
        this.usps = usps;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public String getAuthorsString() {
        return Converters.fromArrayList(authors);
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void setAuthorsString(String authors) {
        this.authors = StringUtil.isJSONValid(authors) ? Converters.fromString(authors, Author[].class) : new ArrayList<>();
    }

    public List<Link> getLinks() {
        return links;
    }

    public String getLinksString() {
        return Converters.fromArrayList(links);
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public void setLinksString(String links) {
        this.links = StringUtil.isJSONValid(links) ? Converters.fromString(links, Link[].class) : new ArrayList<>();
    }

    public boolean isDownloaded() {
        return progress == 100;
    }

    public int getDownloadProgress() {
        return progress;
    }

    public void setDownloadProgress(int progress) {
        this.progress = progress;
    }

    public void setLastRead(String string) {
        this.lastRead = StringUtil.isJSONValid(string) ? Converters.fromString(string, LastRead[].class) : new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void resetInfo() {
        setDownloadProgress(-1);
        setFilePath(null);
        setContentKey("");
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getContentKey() {
        return contentKey;
    }

    public boolean isFavoriten() {
        return isFavoriten;
    }

    public void setFavoriten(boolean favoriten) {
        isFavoriten = favoriten;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.isbn);
        dest.writeString(this.title);
        dest.writeTypedList(this.lastRead);
        dest.writeByte(this.isStandalone ? (byte) 1 : (byte) 0);
        dest.writeString(this.claim);
        dest.writeString(this.editionText);
        dest.writeInt(this.pageNumber);
        dest.writeStringList(this.highlights);
        dest.writeInt(this.editionNumber);
        dest.writeString(this.description);
        dest.writeList(this.subscriptionIds);
        dest.writeString(this.subtitle);
        dest.writeStringList(this.keywords);
        dest.writeInt(this.fileSize);
        dest.writeInt(this.id);
        dest.writeList(this.topicIds);
        dest.writeTypedList(this.covers);
        dest.writeString(this.publisher);
        dest.writeInt(this.copyrightYear);
        dest.writeStringList(this.usps);
        dest.writeString(this.releaseDate);
        dest.writeTypedList(this.authors);
        dest.writeTypedList(this.links);
        dest.writeInt(this.progress);
        dest.writeString(this.filePath);
        dest.writeString(this.contentKey);
        dest.writeByte(this.isFavoriten ? (byte) 1 : (byte) 0);
        dest.writeByte(this.needSyncToServer ? (byte) 1 : (byte) 0);
        dest.writeString(this.lastReadTime);
    }

    public Ebook() {
    }

    protected Ebook(Parcel in) {
        this.isbn = in.readString();
        this.title = in.readString();
        this.lastRead = in.createTypedArrayList(LastRead.CREATOR);
        this.isStandalone = in.readByte() != 0;
        this.claim = in.readString();
        this.editionText = in.readString();
        this.pageNumber = in.readInt();
        this.highlights = in.createStringArrayList();
        this.editionNumber = in.readInt();
        this.description = in.readString();
        this.subscriptionIds = new ArrayList<Long>();
        in.readList(this.subscriptionIds, Long.class.getClassLoader());
        this.subtitle = in.readString();
        this.keywords = in.createStringArrayList();
        this.fileSize = in.readInt();
        this.id = in.readInt();
        this.topicIds = new ArrayList<Long>();
        in.readList(this.topicIds, Long.class.getClassLoader());
        this.covers = in.createTypedArrayList(Cover.CREATOR);
        this.publisher = in.readString();
        this.copyrightYear = in.readInt();
        this.usps = in.createStringArrayList();
        this.releaseDate = in.readString();
        this.authors = in.createTypedArrayList(Author.CREATOR);
        this.links = in.createTypedArrayList(Link.CREATOR);
        this.progress = in.readInt();
        this.filePath = in.readString();
        this.contentKey = in.readString();
        this.isFavoriten = in.readByte() != 0;
        this.needSyncToServer = in.readByte() != 0;
        this.lastReadTime = in.readString();
    }

    public static final Creator<Ebook> CREATOR = new Creator<Ebook>() {
        @Override
        public Ebook createFromParcel(Parcel source) {
            return new Ebook(source);
        }

        @Override
        public Ebook[] newArray(int size) {
            return new Ebook[size];
        }
    };

    public void resetDownloadProgress(boolean needResetDownloadProgress) {
        if (needResetDownloadProgress) {
            setDownloadProgress(-1);
            setContentKey("");
        }
    }

    public Long getDownloadTimeStamp() {
        return downloadTimeStamp;
    }

    public void setDownloadTimeStamp(Long downloadTimeStamp) {
        this.downloadTimeStamp = downloadTimeStamp;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
