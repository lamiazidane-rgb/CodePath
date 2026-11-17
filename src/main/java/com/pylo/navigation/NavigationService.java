package com.pylo.navigation;

import com.pylo.service.SessionService;
import com.pylo.view.pages.PlaceholderPage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.function.Supplier;

/** Remplace UNIQUEMENT le contenu de la zone centrale. */
public final class NavigationService {

    private static final NavigationService INSTANCE = new NavigationService();

    private final ObjectProperty<Route> currentRoute = new SimpleObjectProperty<>();
    private Role currentRole;
    private Pane contentHost;

    private NavigationService() {}

    public static NavigationService getInstance() { return INSTANCE; }

    /** Appelé une seule fois par MainLayout. */
    public void setContentHost(Pane contentHost) { this.contentHost = contentHost; }

    public ReadOnlyObjectProperty<Route> currentRouteProperty() { return currentRoute; }

    public Route getCurrentRoute() { return currentRoute.get(); }

    public void navigateTo(Route route) {
        if (contentHost == null) {
            throw new IllegalStateException("La zone centrale n'est pas initialisée.");
        }
        Role role = roleOf(SessionService.getInstance().getCurrentUser());
        // On recharge si la route OU le rôle a changé
        if (route == currentRoute.get() && role == currentRole) {
            return;
        }
        contentHost.getChildren().setAll(createPage(route, role));
        currentRole = role;
        currentRoute.set(route);
    }

    private Node createPage(Route route, Role role) {
        Supplier<? extends Node> factory = PageRegistry.find(route, role);
        return factory != null ? factory.get() : new PlaceholderPage(route.getTitle());
    }

    private Role roleOf(com.pylo.domain.user.Utilisateur user) {
        for (Role role : Role.values()) {
            if (role.matches(user)) {
                return role;
            }
        }
        throw new IllegalStateException("Utilisateur sans rôle reconnu.");
    }
}