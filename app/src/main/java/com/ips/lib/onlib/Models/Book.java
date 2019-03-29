package com.ips.lib.onlib.Models;

public class Book {

    private String name;
    private String book_id;
    private String author;
    private String edition;
    private String branch;
    private String cover;
    private String is_issued;
    private String issue_date;
    private String issued_to;

    public Book(String name, String book_id, String author, String edition, String branch, String cover, String is_issued, String issue_date, String issued_to) {
        this.name = name;
        this.book_id = book_id;
        this.author = author;
        this.edition = edition;
        this.branch = branch;
        this.cover = cover;
        this.is_issued = is_issued;
        this.issue_date = issue_date;
        this.issued_to = issued_to;
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

    public String getIs_issued() {
        return is_issued;
    }

    public void setIs_issued(String is_issued) {
        this.is_issued = is_issued;
    }

    public String getIssue_date() {
        return issue_date;
    }

    public void setIssue_date(String issue_date) {
        this.issue_date = issue_date;
    }

    public String getIssued_to() {
        return issued_to;
    }

    public void setIssued_to(String issued_to) {
        this.issued_to = issued_to;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", book_id='" + book_id + '\'' +
                ", author='" + author + '\'' +
                ", edition='" + edition + '\'' +
                ", branch='" + branch + '\'' +
                ", cover='" + cover + '\'' +
                ", is_issued='" + is_issued + '\'' +
                ", issue_date='" + issue_date + '\'' +
                ", issued_to='" + issued_to + '\'' +
                '}';
    }
}
