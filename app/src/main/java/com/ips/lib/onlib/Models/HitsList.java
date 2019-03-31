package com.ips.lib.onlib.Models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@IgnoreExtraProperties
public class HitsList {

    @SerializedName("hits")
    @Expose
    private List<BookSource> bookSources;

    public List<BookSource> getBookSources() {
        return bookSources;
    }

    public void setBookSources(List<BookSource> bookSources) {
        this.bookSources = bookSources;
    }
}
