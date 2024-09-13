package me.ronygomes.ums.api.model;

import me.ronygomes.ums.api.validator.annotation.Email;

public class EmailData {

    @Email
    private String email;

    @Email(message = "Custom Message")
    private String email2;

    public EmailData(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }
}
