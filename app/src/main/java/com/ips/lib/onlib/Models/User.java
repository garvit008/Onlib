package com.ips.lib.onlib.Models;

import java.util.ArrayList;

public class User {
    private String user_id;
    private String computer_code;
    private String name;
    private String email;
    private ArrayList<Book> issued_books;
    private ArrayList<Book> wishlist;
    private String profile_pic;

    public User(String user_id, String computer_code, String name, String email, ArrayList<Book> issued_books, ArrayList<Book> wishlist, String profile_pic) {
        this.user_id = user_id;
        this.computer_code = computer_code;
        this.name = name;
        this.email = email;
        this.issued_books = issued_books;
        this.wishlist = wishlist;
        this.profile_pic = profile_pic;
    }

    public User() {
    }


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

    public ArrayList<Book> getIssued_books() {
        return issued_books;
    }

    public void setIssued_books(ArrayList<Book> issued_books) {
        this.issued_books = issued_books;
    }

    public ArrayList<Book> getWishlist() {
        return wishlist;
    }

    public void setWishlist(ArrayList<Book> wishlist) {
        this.wishlist = wishlist;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
