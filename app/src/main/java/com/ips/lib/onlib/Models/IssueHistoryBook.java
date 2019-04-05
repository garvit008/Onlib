package com.ips.lib.onlib.Models;

public class IssueHistoryBook {

    private String book_id;
    private String issue_date;

    public IssueHistoryBook(String book_id, String issue_date) {
        this.book_id = book_id;
        this.issue_date = issue_date;
    }

    public IssueHistoryBook() {
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getIssue_date() {
        return issue_date;
    }

    public void setIssue_date(String issue_date) {
        this.issue_date = issue_date;
    }
}
