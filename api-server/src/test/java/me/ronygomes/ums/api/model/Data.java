package me.ronygomes.ums.api.model;

import jakarta.validation.constraints.Size;

public class Data {

    @Size(min = 1, max = 5)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}