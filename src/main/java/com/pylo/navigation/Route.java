package com.pylo.navigation;

/**
 * Identifie chaque page de l'application.
 * Pour ajouter une page : ajouter une constante ici, puis une ligne dans MenuProvider.
 */
public enum Route {
    // Communes / Apprenant
    DASHBOARD("Tableau de bord"),
    MY_COURSES("Mes cours"),
    MY_EXERCISES("Exercices"),
    MY_EVALUATIONS("Évaluations"),
    MY_PROGRESSION("Ma progression"),
    MY_BADGES("Mes badges"),
    NOTIFICATIONS("Notifications"),
    PROFILE("Mon profil"),
    HELP("Centre d'aide"),

    // Administrateur
    USERS_MANAGEMENT("Gestion des utilisateurs"),
    TEACHERS_MANAGEMENT("Gestion des enseignants"),
    LEARNERS_MANAGEMENT("Gestion des apprenants"),
    SETTINGS("Paramètres"),

    // Enseignant
    LEVELS_MANAGEMENT("Gestion des niveaux"),
    COURSES_MANAGEMENT("Gestion des cours"),
    EXERCISES_MANAGEMENT("Gestion des exercices"),
    TESTS_MANAGEMENT("Gestion des tests");

    private final String title;

    Route(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
}