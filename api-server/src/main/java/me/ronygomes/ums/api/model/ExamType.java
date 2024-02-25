package me.ronygomes.ums.api.model;

public enum ExamType {

    SSC("Secondary School Certificate"),
    HSC("Higher Secondary Certificate"),
    A_LEVEL("A Level"),
    O_LEVEL("O Level");

    private final String displayName;

    ExamType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
