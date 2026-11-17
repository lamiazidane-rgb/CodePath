package com.pylo.domain.learning;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.user.Enseignant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Niveau de difficulté (Débutant, Intermédiaire, Avancé...).
 * Composition : un niveau contient 1..* cours, 1..* exercices et possède exactement 1 test.
 */
public class Niveau {

    private final int idNiveau;
    private String nom;
    private String description;
    private int ordre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double seuilReussite;   // en pourcentage (0 < seuil <= 100)
    private int duree;              // en jours

    private final List<Cours> cours = new ArrayList<>();           // «contient» 1 -> 1..*
    private final List<Exercice> exercices = new ArrayList<>();    // «contient» 1 -> 1..*
    private Test test;                                             // «possede» 1 -> 1

    private Enseignant enseignant;                                 // «organiser» Enseignant 1 -> 0..* Niveau

    public Niveau(int idNiveau, String nom, String description, int ordre,
                  LocalDate dateDebut, LocalDate dateFin, double seuilReussite, int duree) {
        this.idNiveau = idNiveau;
        setNom(nom);
        this.description = description;
        setOrdre(ordre);
        setPeriode(dateDebut, dateFin);
        setSeuilReussite(seuilReussite);
        setDuree(duree);
    }

    /* ---------- Comportements ---------- */

    public boolean verifierConditionsValidation(double scoreObtenu) {
        if (scoreObtenu < 0 || scoreObtenu > 100) {
            throw new IllegalArgumentException("Le score doit être compris entre 0 et 100.");
        }
        return scoreObtenu >= seuilReussite;
    }

    /** Renvoie le niveau dont l'ordre suit celui-ci (vide si c'est le dernier). */
    public Optional<Niveau> debloquerNiveauSuivant(List<Niveau> tousLesNiveaux) {
        Objects.requireNonNull(tousLesNiveaux, "La liste des niveaux est obligatoire.");
        return tousLesNiveaux.stream().filter(n -> n.ordre == ordre + 1).findFirst();
    }

    /** Valide si conforme au diagramme : 1..* cours, 1..* exercices, 1 test, seuil et dates cohérents. */
    public boolean estValide() {
        return nom != null && !nom.isBlank()
                && !cours.isEmpty()
                && !exercices.isEmpty()
                && test != null
                && seuilReussite > 0 && seuilReussite <= 100
                && dateDebut != null && dateFin != null && !dateFin.isBefore(dateDebut);
    }

    /* ---------- Gestion du contenu ---------- */

    public void ajouterCours(Cours c) {
        Objects.requireNonNull(c, "Le cours est obligatoire.");
        if (!cours.contains(c)) {
            cours.add(c);
            c.setNiveau(this);
        }
    }

    public void retirerCours(Cours c) {
        if (cours.remove(c)) {
            c.setNiveau(null);
        }
    }

    public void ajouterExercice(Exercice e) {
        Objects.requireNonNull(e, "L'exercice est obligatoire.");
        if (!exercices.contains(e)) {
            exercices.add(e);
            e.setNiveau(this);
        }
    }

    public void retirerExercice(Exercice e) {
        if (exercices.remove(e)) {
            e.setNiveau(null);
        }
    }

    /** Un niveau possède exactement un test (multiplicité 1) : on le remplace, on ne le retire pas. */
    public void definirTest(Test nouveauTest) {
        Objects.requireNonNull(nouveauTest, "Un niveau doit avoir un test (multiplicité 1).");
        if (test == nouveauTest) {
            return;
        }
        if (test != null) {
            test.setNiveau(null);
        }
        test = nouveauTest;
        nouveauTest.setNiveau(this);
    }

    public void trierCoursParOrdre() {
        cours.sort(Comparator.comparingInt(Cours::getOrdre));
    }

    /* ---------- Accesseurs ---------- */

    public int getIdNiveau() { return idNiveau; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public int getOrdre() { return ordre; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public double getSeuilReussite() { return seuilReussite; }
    public int getDuree() { return duree; }
    public List<Cours> getCours() { return Collections.unmodifiableList(cours); }
    public List<Exercice> getExercices() { return Collections.unmodifiableList(exercices); }
    public Test getTest() { return test; }
    public Enseignant getEnseignant() { return enseignant; }

    /** Public car Enseignant est dans un autre package. Appelé par Enseignant.organiserNiveau. */
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }

    /* ---------- Mutateurs (avec validation) ---------- */

    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du niveau est obligatoire.");
        }
        this.nom = nom;
    }

    public void setDescription(String description) { this.description = description; }

    public void setOrdre(int ordre) {
        if (ordre < 1) {
            throw new IllegalArgumentException("L'ordre doit être >= 1.");
        }
        this.ordre = ordre;
    }

    public void setPeriode(LocalDate dateDebut, LocalDate dateFin) {
        Objects.requireNonNull(dateDebut, "La date de début est obligatoire.");
        Objects.requireNonNull(dateFin, "La date de fin est obligatoire.");
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin précède la date de début.");
        }
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public void setSeuilReussite(double seuilReussite) {
        if (seuilReussite <= 0 || seuilReussite > 100) {
            throw new IllegalArgumentException("Le seuil de réussite doit être dans ]0, 100].");
        }
        this.seuilReussite = seuilReussite;
    }

    public void setDuree(int duree) {
        if (duree <= 0) {
            throw new IllegalArgumentException("La durée doit être > 0 (en jours).");
        }
        this.duree = duree;
    }

    /* ---------- Identité ---------- */

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Niveau n && n.idNiveau == idNiveau);
    }

    @Override
    public int hashCode() { return Integer.hashCode(idNiveau); }

    @Override
    public String toString() { return "Niveau[" + idNiveau + ", " + nom + "]"; }
}