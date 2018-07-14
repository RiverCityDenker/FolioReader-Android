package com.folioreader.model.event;

public class TOCClickedEvent {

    private final String href;
    private final String bookTitle;

    public TOCClickedEvent(String href, String bookTitle) {
        this.href = href;
        this.bookTitle = bookTitle;
    }

    public String getHref() {
        return href;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
