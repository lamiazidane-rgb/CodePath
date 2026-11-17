package com.pylo.domain.user;

import java.time.LocalDateTime;
import java.util.Objects;

/** Notification envoyée à un apprenant (ex. résultat d'une tentative). */
public class Notification {

    private final int idNotification;
    private String message;
    private LocalDateTime dateEnvoi;
    private String type;          // ex. : "RESULTAT", "RAPPEL"
    private boolean estLue;

    private final Apprenant destinataire;

    public Notification(int idNotification, String message, String type, Apprenant destinataire) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message est obligatoire.");
        }
        this.idNotification = idNotification;
        this.message = message;
        this.type = type;
        this.destinataire = Objects.requireNonNull(destinataire, "Le destinataire est obligatoire.");
        this.estLue = false;
    }

    public void envoyerNotification() {
        if (dateEnvoi != null) {
            throw new IllegalStateException("Notification déjà envoyée.");
        }
        dateEnvoi = LocalDateTime.now();
        destinataire.recevoirNotification(this);
    }

    public void marquerCommeLue() { estLue = true; }

    public int getIdNotification() { return idNotification; }
    public String getMessage() { return message; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public String getType() { return type; }
    public boolean isEstLue() { return estLue; }
    public Apprenant getDestinataire() { return destinataire; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Notification n && n.idNotification == idNotification);
    }

    @Override
    public int hashCode() { return Objects.hash(idNotification); }
}