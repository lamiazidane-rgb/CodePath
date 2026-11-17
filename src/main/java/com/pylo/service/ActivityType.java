package com.pylo.service;

public enum ActivityType {
    COURSE("fth-book-open"),
    EXERCISE("fth-code"),
    TEST("fth-check-square");

    private final String icon;

    ActivityType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
