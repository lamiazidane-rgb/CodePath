package com.pylo.view.pages.teacher.dashboard;

import com.pylo.domain.assessment.Tentative;
import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Niveau;
import com.pylo.domain.user.Apprenant;
import com.pylo.domain.user.Enseignant;
import com.pylo.navigation.NavigationService;
import com.pylo.navigation.Route;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.NiveauStatus;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

/** Tableau de bord enseignant : indicateurs, activité récente des apprenants et niveaux à compléter. */
public class DashboardView extends VBox {

    private static final int MAX_ROWS = 5;

    private final Enseignant teacher = TeacherUi.currentTeacher();
    private final List<Niveau> levels = CatalogueService.getInstance().findNiveaux(teacher);

    public DashboardView() {
        super(18);
        getStyleClass().addAll("page", "t-page");
        getChildren().addAll(
                TeacherUi.header("Bonjour, " + teacher.getPrenom() + " !", "Voici l'état de votre parcours.", null),
                statistics(),
                panels());
    }

    /* ---------- Indicateurs ---------- */

    private HBox statistics() {
        int courses = levels.stream().mapToInt(n -> n.getCours().size()).sum();
        int exercises = levels.stream().mapToInt(n -> n.getExercices().size()).sum();
        long complete = levels.stream().filter(NiveauStatus::isComplete).count();
        List<Tentative> attempts = attempts();
        long learners = attempts.stream().map(Tentative::getApprenant).distinct().count();
        long tests = levels.stream().filter(n -> n.getTest() != null).count();

        HBox row = new HBox(16,
                TeacherUi.statCard("fth-layers", "violet", "Niveaux", String.valueOf(levels.size()), complete + " complet(s)"),
                TeacherUi.statCard("fth-book-open", "blue", "Cours", String.valueOf(courses), "sur " + levels.size() + " niveau(x)"),
                TeacherUi.statCard("fth-code", "orange", "Exercices", String.valueOf(exercises), "sur " + levels.size() + " niveau(x)"),
                TeacherUi.statCard("fth-check-square", "rose", "Tests", String.valueOf(tests),
                        attempts.size() + " tentative(s) · " + learners + " apprenant(s)"));
        return row;
    }

    private List<Tentative> attempts() {
        return levels.stream()
                .map(Niveau::getTest)
                .filter(test -> test != null)
                .flatMap(test -> test.getTentatives().stream())
                .toList();
    }

    /* ---------- Panneaux ---------- */

    private HBox panels() {
        HBox row = new HBox(16, recentActivity(), incompleteLevels());
        row.getChildren().forEach(panel -> HBox.setHgrow(panel, Priority.ALWAYS));
        return row;
    }

    private VBox panel(String title, String linkText, Route route, VBox rows) {
        Hyperlink link = new Hyperlink(linkText);
        link.getStyleClass().add("t-link");
        link.setOnAction(e -> NavigationService.getInstance().navigateTo(route));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox head = new HBox(TeacherUi.label(title, "card-title"), spacer, link);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox panel = TeacherUi.card(head, rows);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setPrefWidth(1);
        return panel;
    }

    private VBox recentActivity() {
        VBox rows = new VBox();
        List<Tentative> recent = attempts().stream()
                .sorted(Comparator.comparing(Tentative::getDateDebut).reversed())
                .limit(MAX_ROWS)
                .toList();
        if (recent.isEmpty()) {
            rows.getChildren().add(TeacherUi.label("Aucun apprenant n'a encore passé de test.", "t-hint"));
        }
        recent.forEach(attempt -> rows.getChildren().add(activityRow(attempt)));
        return panel("Activité récente", "Voir les tests", Route.TESTS_MANAGEMENT, rows);
    }

    private HBox activityRow(Tentative attempt) {
        Apprenant learner = attempt.getApprenant();
        Test test = attempt.getTest();
        Label avatar = TeacherUi.label(initials(learner), "t-avatar");
        avatar.setAlignment(Pos.CENTER);

        VBox identity = new VBox(2, TeacherUi.label(learner.getPrenom() + " " + learner.getNom(), "t-cell-strong"),
                TeacherUi.label(test.getTitre(), "t-cell-sub"));
        HBox.setHgrow(identity, Priority.ALWAYS);
        VBox status = new VBox(4, TeacherUi.attemptChip(attempt.getStatut()),
                TeacherUi.label(attempt.getDateDebut().format(TeacherUi.DATE_TIME), "t-cell-sub"));
        status.setAlignment(Pos.CENTER_RIGHT);
        return listRow(avatar, identity, status);
    }

    private VBox incompleteLevels() {
        VBox rows = new VBox();
        List<Niveau> incomplete = levels.stream().filter(n -> !NiveauStatus.isComplete(n)).limit(MAX_ROWS).toList();
        if (levels.isEmpty()) {
            rows.getChildren().add(TeacherUi.label("Aucun niveau : créez-en un pour démarrer le parcours.", "t-hint"));
        } else if (incomplete.isEmpty()) {
            rows.getChildren().add(TeacherUi.label("Tous vos niveaux sont complets.", "t-hint"));
        }
        incomplete.forEach(level -> rows.getChildren().add(levelRow(level)));
        return panel("Niveaux à compléter", "Gérer les niveaux", Route.LEVELS_MANAGEMENT, rows);
    }

    private HBox levelRow(Niveau level) {
        Label number = TeacherUi.label("N" + level.getOrdre(), "t-avatar");
        number.setAlignment(Pos.CENTER);
        Label missing = TeacherUi.label("Il manque : " + String.join(", ", NiveauStatus.missing(level)), "t-cell-sub");
        missing.setWrapText(true);
        VBox identity = new VBox(2, TeacherUi.label(level.getNom(), "t-cell-strong"), missing);
        HBox.setHgrow(identity, Priority.ALWAYS);
        return listRow(number, identity, NiveauStatus.chip(level));
    }

    private HBox listRow(Node... cells) {
        HBox row = new HBox(12, cells);
        row.getStyleClass().add("t-list-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String initials(Apprenant learner) {
        return ("" + learner.getPrenom().charAt(0) + learner.getNom().charAt(0)).toUpperCase();
    }
}
