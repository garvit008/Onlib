package com.ips.lib.onlib.Models;

public class BookRefined {

    private String name;
    private String author;
    private String edition;
    private String total;
    private String available;
    private String cover;

    public BookRefined(String name, String author, String edition, String total, String available, String cover) {
        this.name = name;
        this.author = author;
        this.edition = edition;
        this.total = total;
        this.available = available;
        this.cover = cover;
    }

    public BookRefined() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
