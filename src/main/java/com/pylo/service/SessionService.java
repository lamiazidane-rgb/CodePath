package com.pylo.service;

import com.pylo.domain.user.Utilisateur;
import com.pylo.navigation.Role;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/** Session courante : utilisateur connecté (observable) et compteur de notifications. */
public final class SessionService {

    // Pour passer à une vraie base : remplacer MockUserService par une autre implémentation.
    private static final SessionService INSTANCE = new SessionService(UserService.inMemory());

    private final UserService userService;
    private final ObjectProperty<Utilisateur> currentUser = new SimpleObjectProperty<>();
    private final IntegerProperty unreadNotifications = new SimpleIntegerProperty(0);

    private SessionService(UserService userService) {
        this.userService = userService;
        this.currentUser.set(userService.findByRole(Role.LEARNER));
    }

    public static SessionService getInstance() { return INSTANCE; }

    public ObjectProperty<Utilisateur> currentUserProperty() { return currentUser; }

    public Utilisateur getCurrentUser() { return currentUser.get(); }

    public IntegerProperty unreadNotificationsProperty() { return unreadNotifications; }

    /** Simule la connexion avec un autre rôle, sans persistance. */
    public void switchRole(Role role) {
        currentUser.set(userService.findByRole(role));
    }
}