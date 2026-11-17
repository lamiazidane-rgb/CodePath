package com.pylo.domain.enums;


/** Cycle de vie d'un cours. */
public enum StatutCours {
    BROUILLON("Brouillon"), PUBLIE("Publié"), ARCHIVE("Archivé");

    private final String libelle;

    StatutCours(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}