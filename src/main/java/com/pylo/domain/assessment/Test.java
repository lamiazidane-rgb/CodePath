package com.pylo.domain.assessment;


import com.pylo.domain.learning.Niveau;

import com.pylo.domain.user.Apprenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Test de fin de niveau (association «possede» : Niveau 1 -> 1 Test). */
public class Test {

    private final int idTest;
    private String titre;
    private String description;
    private int duree;                       // en minutes
    private double noteMaximale;
    private double seuilReussite;            // en pourcentage (0 < seuil <= 100)
    private int nombreTentativesAutorisees;

    private Niveau niveau;                   // niveau auquel appartient ce test

    private final List<Question> questions = new ArrayList<>();     // «contient» 1 -> 1..*
    private final List<Tentative> tentatives = new ArrayList<>();   // «concerne» 1 -> 1..*

    public Test(int idTest, String titre, String description, int duree,
                double noteMaximale, double seuilReussite, int nombreTentativesAutorisees) {
        this.idTest = idTest;
        setTitre(titre);
        this.description = description;
        setDuree(duree);
        setNoteMaximale(noteMaximale);
        setSeuilReussite(seuilReussite);
        setNombreTentativesAutorisees(nombreTentativesAutorisees);
    }

    /* ---------- Comportements du diagramme ---------- */

    /** Un apprenant peut démarrer le test tant qu'il n'a pas atteint le nombre de tentatives autorisées. */
    public boolean demarrerTest(Apprenant apprenant) {
        Objects.requireNonNull(apprenant, "L'apprenant est obligatoire.");
        long dejaEffectuees = tentatives.stream().filter(t -> t.getApprenant().equals(apprenant)).count();
        return dejaEffectuees < nombreTentativesAutorisees;
    }

    /** Calcule la note sur noteMaximale à partir du nombre de bonnes réponses. */
    public double calculerNote(int bonnesReponses, int totalQuestions) {
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("Le nombre de questions doit être > 0.");
        }
        if (bonnesReponses < 0 || bonnesReponses > totalQuestions) {
            throw new IllegalArgumentException("Nombre de bonnes réponses invalide.");
        }
        return (double) bonnesReponses / totalQuestions * noteMaximale;
    }

    /** Renvoie true si la note atteint le seuil de réussite. */
    public boolean evaluerResultat(double note) {
        if (note < 0 || note > noteMaximale) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et " + noteMaximale + ".");
        }
        return note / noteMaximale * 100 >= seuilReussite;
    }

    /* ---------- Gestion des questions et tentatives ---------- */

    public void ajouterQuestion(Question q) {
        Objects.requireNonNull(q, "La question est obligatoire.");
        if (!questions.contains(q)) {
            questions.add(q);
            q.setTest(this);
        }
    }

    public void retirerQuestion(Question q) {
        if (questions.remove(q)) {
            q.setTest(null);
        }
    }

    /** Appelé par le constructeur de Tentative. */
    void enregistrerTentative(Tentative t) {
        if (!tentatives.contains(t)) {
            tentatives.add(t);
        }
    }

    public List<Question> getQuestions() { return Collections.unmodifiableList(questions); }
    public List<Tentative> getTentatives() { return Collections.unmodifiableList(tentatives); }

    /* ---------- Accesseurs ---------- */

    public int getIdTest() { return idTest; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public int getDuree() { return duree; }
    public double getNoteMaximale() { return noteMaximale; }
    public double getSeuilReussite() { return seuilReussite; }
    public int getNombreTentativesAutorisees() { return nombreTentativesAutorisees; }
    public Niveau getNiveau() { return niveau; }

    /* ---------- Mutateurs ---------- */

    public void setTitre(String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre du test est obligatoire.");
        }
        this.titre = titre;
    }

    public void setDescription(String description) { this.description = description; }

    public void setDuree(int duree) {
        if (duree <= 0) {
            throw new IllegalArgumentException("La durée doit être > 0.");
        }
        this.duree = duree;
    }

    public void setNoteMaximale(double noteMaximale) {
        if (noteMaximale <= 0) {
            throw new IllegalArgumentException("La note maximale doit être > 0.");
        }
        this.noteMaximale = noteMaximale;
    }

    public void setSeuilReussite(double seuilReussite) {
        if (seuilReussite <= 0 || seuilReussite > 100) {
            throw new IllegalArgumentException("Le seuil de réussite doit être dans ]0, 100].");
        }
        this.seuilReussite = seuilReussite;
    }

    public void setNombreTentativesAutorisees(int nombre) {
        if (nombre < 1) {
            throw new IllegalArgumentException("Au moins une tentative doit être autorisée.");
        }
        this.nombreTentativesAutorisees = nombre;
    }

    /** Public car Niveau est dans un autre package. Appelé par Niveau.definirTest. */
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }

    /* ---------- Identité ---------- */

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Test t && t.idTest == idTest);
    }

    @Override
    public int hashCode() { return Objects.hash(idTest); }

    @Override
    public String toString() { return "Test[" + idTest + ", " + titre + "]"; }
}