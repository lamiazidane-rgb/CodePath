package com.pylo.domain.user;

import java.util.Objects;

/** Classe de base : Administrateur, Enseignant et Apprenant en héritent. */
public class Utilisateur {

    private final int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String mdp;   // en vrai projet : stocker un hash (BCrypt), jamais le mot de passe en clair

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String mdp) {
        this.idUtilisateur = idUtilisateur;
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setMdp(mdp);
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }

    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        if (prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalide.");
        }
        this.email = email;
    }

    public void setMdp(String mdp) {
        if (mdp == null || mdp.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        this.mdp = mdp;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Utilisateur u && u.idUtilisateur == idUtilisateur);
    }

    @Override
    public int hashCode() { return Objects.hash(idUtilisateur); }

    @Override
    public String toString() { return getClass().getSimpleName() + "[" + idUtilisateur + ", " + prenom + " " + nom + "]"; }
}