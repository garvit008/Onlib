package com.ips.lib.onlib.Models;

public class BookNotification {

    private String content;
    private String date;

    public BookNotification(String content, String date) {
        this.content = content;
        this.date = date;
    }

    public BookNotification() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
