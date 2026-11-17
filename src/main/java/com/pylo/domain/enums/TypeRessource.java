package com.pylo.domain.enums;

/** Nature d'une ressource jointe à une leçon. */
public enum TypeRessource {
    PDF("PDF"), IMAGE("Image"), VIDEO("Vidéo"), LIEN("Lien externe"), FICHIER("Fichier");

    private final String libelle;

    TypeRessource(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}