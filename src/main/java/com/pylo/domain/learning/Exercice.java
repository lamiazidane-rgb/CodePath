package com.pylo.domain.learning;

import java.util.Objects;

/** Exercice appartenant à un niveau (association «contient» : Niveau 1 -> 1..* Exercice). */
public class Exercice {

    private final int idExercice;
    private String titre;
    private String enonce;
    private String difficulte;   // ex. : "Facile", "Moyen", "Difficile"
    private double bareme;       // points attribués à l'exercice

    private Niveau niveau;       // niveau auquel appartient cet exercice

    public Exercice(int idExercice, String titre, String enonce, String difficulte, double bareme) {
        this.idExercice = idExercice;
        setTitre(titre);
        this.enonce = enonce;
        this.difficulte = difficulte;
        setBareme(bareme);
    }

    /* ---------- Accesseurs ---------- */

    public int getIdExercice() { return idExercice; }
    public String getTitre() { return titre; }
    public String getEnonce() { return enonce; }
    public String getDifficulte() { return difficulte; }
    public double getBareme() { return bareme; }
    public Niveau getNiveau() { return niveau; }

    /* ---------- Mutateurs ---------- */

    public void setTitre(String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre de l'exercice est obligatoire.");
        }
        this.titre = titre;
    }

    public void setEnonce(String enonce) { this.enonce = enonce; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }

    public void setBareme(double bareme) {
        if (bareme < 0) {
            throw new IllegalArgumentException("Le barème ne peut pas être négatif.");
        }
        this.bareme = bareme;
    }

    /** Appelé par Niveau (ajouterExercice / retirerExercice). Ne rappelle PAS Niveau pour éviter la récursion. */
    void setNiveau(Niveau niveau) { this.niveau = niveau; }

    /* ---------- Identité ---------- */

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Exercice e && e.idExercice == idExercice);
    }

    @Override
    public int hashCode() { return Objects.hash(idExercice); }

    @Override
    public String toString() { return "Exercice[" + idExercice + ", " + titre + "]"; }
}