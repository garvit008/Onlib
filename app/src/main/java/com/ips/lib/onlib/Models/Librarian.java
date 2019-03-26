package com.ips.lib.onlib.Models;

public class Librarian {
    private String user_id;
    private String computer_code;
    private String name;
    private String email;
    private String profile_pic;
    private String fees_collected;

    public Librarian(String user_id, String computer_code, String name, String email, String profile_pic, String fees_collected) {
        this.user_id = user_id;
        this.computer_code = computer_code;
        this.name = name;
        this.email = email;
        this.profile_pic = profile_pic;
        this.fees_collected = fees_collected;
    }

    public Librarian() {
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

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getFees_collected() {
        return fees_collected;
    }

    public void setFees_collected(String fees_collected) {
        this.fees_collected = fees_collected;
    }
}
