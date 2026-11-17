package com.pylo.domain.assessment;

import com.pylo.domain.user.Apprenant;

import java.time.LocalDateTime;
import java.util.Objects;

/** Tentative d'un apprenant sur un test («concerne» : Test 1 -> 1..* Tentative). */
public class Tentative {

    public enum Statut { EN_COURS, REUSSIE, ECHOUEE }

    private final int idTentative;
    private final LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private double noteObtenue;
    private Statut statut;

    private final Apprenant apprenant;
    private final Test test;

    public Tentative(int idTentative, Apprenant apprenant, Test test) {
        this.idTentative = idTentative;
        this.apprenant = Objects.requireNonNull(apprenant, "L'apprenant est obligatoire.");
        this.test = Objects.requireNonNull(test, "Le test est obligatoire.");
        this.dateDebut = LocalDateTime.now();
        this.statut = Statut.EN_COURS;
        test.enregistrerTentative(this);
    }

    /** Termine la tentative avec la note obtenue et détermine le statut. */
    public void terminer(double note) {
        if (statut != Statut.EN_COURS) {
            throw new IllegalStateException("Tentative déjà terminée.");
        }
        boolean reussi = test.evaluerResultat(note);   // vérifie aussi que la note est valide
        this.noteObtenue = note;
        this.dateFin = LocalDateTime.now();
        this.statut = reussi ? Statut.REUSSIE : Statut.ECHOUEE;
    }

    public int getIdTentative() { return idTentative; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public double getNoteObtenue() { return noteObtenue; }
    public Statut getStatut() { return statut; }
    public Apprenant getApprenant() { return apprenant; }
    public Test getTest() { return test; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Tentative t && t.idTentative == idTentative);
    }

    @Override
    public int hashCode() { return Objects.hash(idTentative); }
}