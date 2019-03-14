package com.ips.lib.onlib.Models;

public class Book {

    private String name;
    private String book_id;
    private String author;
    private String edition;
    private String branch;
    private String cover;

    public Book(String name, String book_id, String author, String edition, String branch, String cover) {
        this.name = name;
        this.book_id = book_id;
        this.author = author;
        this.edition = edition;
        this.branch = branch;
        this.cover = cover;
    }

    public Book() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
