package me.ronygomes.ums.api.exception;

import java.net.URI;

public enum ExceptionType {

    ENTITY_NOT_FOUND("entity-not-found", "Requested object not found"),
    DATA_VALIDATION_FAILED("data-validation-failed", "Provided data is not valid");

    private final String id;
    private final String title;

    ExceptionType(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public URI getDocumentationUrl() {
        return URI.create(Constants.DOCUMENTATION_ROOT + id);
    }

    private static class Constants {
        public static final String DOCUMENTATION_ROOT = "https://documentation.com/errors/";
    }
}
