package com.ips.lib.onlib.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Parcelable {
    private String user_id;
    private String computer_code;
    private String name;
    private String email;
    private String issued_books;
    private String wishlist;
    private String profile_pic;
    private long books_issued_count;
    private String messaging_token;

    public User(String user_id, String computer_code, String name, String email, String issued_books, String wishlist, String profile_pic, long books_issued_count, String messaging_token) {
        this.user_id = user_id;
        this.computer_code = computer_code;
        this.name = name;
        this.email = email;
        this.issued_books = issued_books;
        this.wishlist = wishlist;
        this.profile_pic = profile_pic;
        this.books_issued_count = books_issued_count;
        this.messaging_token = messaging_token;
    }

    public User() {
    }

    protected User(Parcel in) {
        user_id = in.readString();
        computer_code = in.readString();
        name = in.readString();
        email = in.readString();
        profile_pic = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComputer_code() {
        return computer_code;
    }

    public void setComputer_code(String computer_code) {
        this.computer_code = computer_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIssued_books() {
        return issued_books;
    }

    public void setIssued_books(String issued_books) {
        this.issued_books = issued_books;
    }

    public void setBooks_issued_count(long books_issued_count) {
        this.books_issued_count = books_issued_count;
    }

    public String getWishlist() {
        return wishlist;
    }

    public void setWishlist(String wishlist) {
        this.wishlist = wishlist;
    }

    public long getBooks_issued_count() {
        return books_issued_count;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(computer_code);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(profile_pic);
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", computer_code='" + computer_code + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", issued_books=" + issued_books +
                ", wishlist='" + wishlist + '\'' +
                ", profile_pic='" + profile_pic + '\'' +
                ", books_issued_count='" + books_issued_count + '\'' +
                '}';
    }
}
