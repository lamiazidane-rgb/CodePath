package com.pylo.navigation;

import com.pylo.service.SessionService;

import java.util.List;

/** Fournit le menu latéral selon le rôle. Aucune entrée n'est écrite dans les FXML. */
public final class MenuProvider {

    private MenuProvider() {}

    public static List<MenuEntry> getMenu(Role role) {
        return switch (role) {
            case ADMIN -> List.of(
                    new MenuEntry("Tableau de bord", "fth-grid", Route.DASHBOARD),
                    new MenuEntry("Gestion des utilisateurs", "fth-users", Route.USERS_MANAGEMENT),
                    new MenuEntry("Gestion des enseignants", "fth-briefcase", Route.TEACHERS_MANAGEMENT),
                    new MenuEntry("Gestion des apprenants", "fth-user-check", Route.LEARNERS_MANAGEMENT),
                    new MenuEntry("Paramètres", "fth-settings", Route.SETTINGS));

            case TEACHER -> List.of(
                    new MenuEntry("Tableau de bord", "fth-grid", Route.DASHBOARD),
                    new MenuEntry("Gestion des niveaux", "fth-layers", Route.LEVELS_MANAGEMENT),
                    new MenuEntry("Gestion des cours", "fth-book-open", Route.COURSES_MANAGEMENT),
                    new MenuEntry("Gestion des exercices", "fth-code", Route.EXERCISES_MANAGEMENT),
                    new MenuEntry("Gestion des tests", "fth-check-square", Route.TESTS_MANAGEMENT));
            case LEARNER -> List.of(
                    new MenuEntry("Tableau de bord", "fth-grid", Route.DASHBOARD),
                    new MenuEntry("Mes cours", "fth-book-open", Route.MY_COURSES),
                    new MenuEntry("Exercices", "fth-code", Route.MY_EXERCISES),
                    new MenuEntry("Évaluations", "fth-check-square", Route.MY_EVALUATIONS),
                    new MenuEntry("Ma progression", "fth-bar-chart-2", Route.MY_PROGRESSION),
                    new MenuEntry("Mes badges", "fth-award", Route.MY_BADGES),
                    new MenuEntry("Notifications", "fth-bell", Route.NOTIFICATIONS,
                            SessionService.getInstance().unreadNotificationsProperty()),
                    new MenuEntry("Mon profil", "fth-user", Route.PROFILE));
        };
    }
}