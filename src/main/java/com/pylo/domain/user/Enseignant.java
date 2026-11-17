package com.pylo.domain.user;

import com.pylo.domain.learning.Niveau;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Enseignant : organise des niveaux («organiser» : Enseignant 1 -> 0..* Niveau). */
public class Enseignant extends Utilisateur {

    private final List<Niveau> niveaux = new ArrayList<>();

    public Enseignant(int idUtilisateur, String nom, String prenom, String email, String mdp) {
        super(idUtilisateur, nom, prenom, email, mdp);
    }

    public void organiserNiveau(Niveau niveau) {
        Objects.requireNonNull(niveau, "Le niveau est obligatoire.");
        if (niveaux.contains(niveau)) {
            return;
        }
        Enseignant ancien = niveau.getEnseignant();
        if (ancien != null && ancien != this) {
            ancien.niveaux.remove(niveau);
        }
        niveaux.add(niveau);
        niveau.setEnseignant(this);
    }

    public void retirerNiveau(Niveau niveau) {
        if (niveaux.remove(niveau)) {
            niveau.setEnseignant(null);
        }
    }

    public List<Niveau> getNiveaux() { return Collections.unmodifiableList(niveaux); }
}