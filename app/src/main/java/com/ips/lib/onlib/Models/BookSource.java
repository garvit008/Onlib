package com.ips.lib.onlib.Models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class BookSource {

    @SerializedName("_source")
    @Expose
    private BookRefined book;

    public BookRefined getBook() {
        return book;
    }

    public void setBook(BookRefined book) {
        this.book = book;
    }
}
