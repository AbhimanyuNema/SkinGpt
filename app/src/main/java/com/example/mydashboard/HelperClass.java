package com.example.mydashboard;

public class HelperClass {

    String name, email, password;
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HelperClass(String name, String email,  String password) {

        this.email = email;
        this.name = name;
        this.password = password;
    }

    public HelperClass() {
    }
}