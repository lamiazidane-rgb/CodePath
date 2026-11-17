package com.pylo.service;

public enum LevelStatus {
    LOCKED("Verrouillé"),
    IN_PROGRESS("En cours"),
    VALIDATED("Validé");

    private final String label;

    LevelStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
