package com.pylo.service;

public enum Period {
    WEEK("Cette semaine"),
    MONTH("Ce mois"),
    YEAR("Cette année");

    private final String label;

    Period(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
