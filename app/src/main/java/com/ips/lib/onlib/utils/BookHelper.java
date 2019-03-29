package com.ips.lib.onlib.utils;

import android.util.Log;

import com.ips.lib.onlib.Models.Book;

public class BookHelper {

    private static final String TAG = "BookHelper";
    public static String getRefinedKey(Book book)
    {
        String refinedKey = book.getName() + book.getAuthor() + book.getEdition();
        refinedKey = refinedKey.replace(" ","");
        refinedKey = refinedKey.replace(".","");
        Log.d(TAG, "getRefinedKey: refinedkey = " + refinedKey);
        return refinedKey.toLowerCase();
    }
}
