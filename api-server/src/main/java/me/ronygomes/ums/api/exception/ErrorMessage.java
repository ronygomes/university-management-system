package me.ronygomes.ums.api.exception;

import java.io.Serializable;

public class ErrorMessage implements Serializable {

    private String field;
    private String message;

    public ErrorMessage() {
    }

    public ErrorMessage(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
