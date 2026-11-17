package com.pylo.navigation;

import com.pylo.view.pages.learner.LearnerDashboardPage;
import com.pylo.view.pages.teacher.cours.CoursView;
import com.pylo.view.pages.teacher.dashboard.DashboardView;
import com.pylo.view.pages.teacher.exercice.ExerciceView;
import com.pylo.view.pages.teacher.niveau.NiveauView;
import com.pylo.view.pages.teacher.test.TestView;

import javafx.scene.Node;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registre des pages : associe (rôle, route) à une fabrique de page.
 * Route non enregistrée = page temporaire (PlaceholderPage).
 */
public final class PageRegistry {

    private static final Map<Role, Map<Route, Supplier<? extends Node>>> PAGES = new EnumMap<>(Role.class);

    static {
        for (Role role : Role.values()) {
            PAGES.put(role, new EnumMap<>(Route.class));
        }

        // ======== APPRENANT ========
        register(Role.LEARNER, Route.DASHBOARD, LearnerDashboardPage::new);

        // ======== ENSEIGNANT ========
        register(Role.TEACHER, Route.DASHBOARD, DashboardView::new);
        register(Role.TEACHER, Route.LEVELS_MANAGEMENT, NiveauView::new);
        register(Role.TEACHER, Route.COURSES_MANAGEMENT, CoursView::new);
        register(Role.TEACHER, Route.EXERCISES_MANAGEMENT, ExerciceView::new);
        register(Role.TEACHER, Route.TESTS_MANAGEMENT, TestView::new);

        // ======== ADMINISTRATEUR ========
        // register(Role.ADMIN, Route.USERS_MANAGEMENT, UsersManagementPage::new);
    }

    private PageRegistry() {}

    private static void register(Role role, Route route, Supplier<? extends Node> factory) {
        PAGES.get(role).put(route, factory);
    }

    /** Renvoie la fabrique de la page, ou null si la page n'existe pas encore. */
    public static Supplier<? extends Node> find(Route route, Role role) {
        return PAGES.get(role).get(route);
    }
}