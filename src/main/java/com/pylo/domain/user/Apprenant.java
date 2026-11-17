package com.pylo.domain.user;

import com.pylo.domain.assessment.Tentative;
import com.pylo.domain.assessment.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Apprenant : effectue des tentatives de test («effectue» : Apprenant 1 -> 0..* Tentative). */
public class Apprenant extends Utilisateur {

    private final List<Tentative> tentatives = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();

    public Apprenant(int idUtilisateur, String nom, String prenom, String email, String mdp) {
        super(idUtilisateur, nom, prenom, email, mdp);
    }

    /** Démarre une nouvelle tentative sur un test, si le nombre de tentatives autorisées n'est pas atteint. */
    public Tentative effectuerTentative(Test test, int idTentative) {
        Objects.requireNonNull(test, "Le test est obligatoire.");
        if (!test.demarrerTest(this)) {
            throw new IllegalStateException("Nombre maximal de tentatives atteint pour ce test.");
        }
        Tentative tentative = new Tentative(idTentative, this, test);
        tentatives.add(tentative);
        return tentative;
    }

    /** Appelé par Notification.envoyerNotification(). */
    void recevoirNotification(Notification notification) {
        notifications.add(notification);
    }

    public List<Tentative> getTentatives() { return Collections.unmodifiableList(tentatives); }
    public List<Notification> getNotifications() { return Collections.unmodifiableList(notifications); }
}