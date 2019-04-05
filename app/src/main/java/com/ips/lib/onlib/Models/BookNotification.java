package com.ips.lib.onlib.Models;

public class BookNotification {

    private String content;
    private String date;
    private String type;
    public BookNotification(String content, String date, String type) {
        this.content = content;
        this.date = date;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
