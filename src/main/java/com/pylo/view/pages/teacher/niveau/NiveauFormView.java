package com.pylo.view.pages.teacher.niveau;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Niveau;
import com.pylo.domain.user.Enseignant;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.NiveauStatus;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Formulaire création / modification d'un niveau — layout maquette
 * (colonne principale + panneau latéral + barre d'actions).
 */
class NiveauFormView extends VBox {

    private static final int DEFAULT_DURATION_DAYS = 7;
    private static final int DEFAULT_THRESHOLD = 70;
    private static final DateTimeFormatter LAST_EDIT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH);

    private final CatalogueService service = CatalogueService.getInstance();
    private final Niveau level;
    private final Runnable onClose;
    private final Consumer<String> onSaved;
    private final Label error = TeacherUi.messageLabel();

    private final TextField name = new TextField();
    private final TextField subtitle = new TextField();
    private final TextArea description = new TextArea();
    private final Spinner<Integer> order;
    private final Spinner<Integer> duration;
    private final Spinner<Integer> threshold;
    private final Spinner<Integer> attempts;
    private final DatePicker startDate;
    private final DatePicker endDate;
    private final ComboBox<String> language = new ComboBox<>();
    private final ComboBox<String> status = new ComboBox<>();
    private final ComboBox<String> owner = new ComboBox<>();
    private final ComboBox<Niveau> previousLevel = new ComboBox<>();
    private final ComboBox<String> validationTest = new ComboBox<>();
    private final CheckBox showInPath = new CheckBox("Afficher dans le parcours");
    private final CheckBox requirePrevious = new CheckBox("Niveau précédent obligatoire");
    private final CheckBox requireTest = new CheckBox("Test de validation obligatoire");

    private final ObservableList<String> objectives = FXCollections.observableArrayList();
    private final VBox objectivesBox = new VBox(8);
    private final VBox coursesBox = new VBox(0);

    NiveauFormView(Niveau level, int nextOrder, Runnable onClose, Consumer<String> onSaved) {
        super(0);
        this.level = level;
        this.onClose = onClose;
        this.onSaved = onSaved;
        getStyleClass().addAll("page", "t-page", "le-root");

        URL css = getClass().getResource("/css/pages/teacher-level-edit.css");
        if (css != null) {
            getStylesheets().add(css.toExternalForm());
        }

        LocalDate today = LocalDate.now();
        order = TeacherUi.spinner(1, 99, level == null ? Math.max(1, nextOrder) : level.getOrdre());
        duration = TeacherUi.spinner(1, 365, level == null ? DEFAULT_DURATION_DAYS : level.getDuree());
        threshold = TeacherUi.spinner(1, 100,
                level == null ? DEFAULT_THRESHOLD : (int) Math.round(level.getSeuilReussite()));
        attempts = TeacherUi.spinner(1, 10, 3);
        startDate = new DatePicker(level == null ? today : level.getDateDebut());
        endDate = new DatePicker(level == null ? today.plusDays(DEFAULT_DURATION_DAYS) : level.getDateFin());

        configureFields();
        loadExistingValues();
        refreshObjectives();
        refreshCourses();

        HBox body = new HBox(20, buildMainColumn(), buildSideColumn());
        HBox.setHgrow(body.getChildren().get(0), Priority.ALWAYS);
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(buildBreadcrumb(), buildHeader(), error, body, buildActionBar());
        VBox.setMargin(error, new Insets(8, 0, 0, 0));
        VBox.setMargin(body, new Insets(16, 0, 0, 0));
    }

    /* ===================== HEADER ===================== */

    private HBox buildBreadcrumb() {
        Label crumb = new Label("Espace enseignant  /  Niveaux  /  "
                + (level == null ? "Nouveau niveau" : "Modifier le niveau " + level.getNom()));
        crumb.getStyleClass().add("le-breadcrumb");
        HBox box = new HBox(crumb);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private HBox buildHeader() {
        String titleText = level == null
                ? "Ajouter un niveau"
                : "Modifier le niveau " + level.getNom();
        Label title = new Label(titleText);
        title.getStyleClass().add("le-title");

        Label sub = new Label(level == null
                ? "Créez un niveau du parcours : identité, objectifs, publication et validation."
                : "Mettez à jour l’identité, les objectifs et les règles de validation de ce niveau.");
        sub.getStyleClass().add("le-subtitle");
        sub.setWrapText(true);

        VBox copy = new VBox(4, title, sub);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Button preview = TeacherUi.secondaryButton("Aperçu du niveau", "fth-eye", () -> { });
        preview.setDisable(level == null);

        HBox header = new HBox(16, copy, preview);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(10, 0, 0, 0));
        return header;
    }

    /* ===================== COLONNE GAUCHE ===================== */

    private VBox buildMainColumn() {
        VBox col = new VBox(18, buildIdentityCard(), buildObjectivesCard(), buildContentCard());
        HBox.setHgrow(col, Priority.ALWAYS);
        return col;
    }

    private VBox buildIdentityCard() {
        HBox top = cardHeader("fth-bookmark", "Identité du niveau",
                "Nom, ordre, présentation et durée estimée.");

        HBox row1 = TeacherUi.row(
                TeacherUi.field("Nom du niveau *", name),
                TeacherUi.field("Ordre du parcours", order)
        );
        VBox subField = TeacherUi.field("Sous-titre", subtitle);
        VBox descField = TeacherUi.field("Présentation", description);

        HBox row2 = TeacherUi.row(
                TeacherUi.field("Durée estimée (jours)", duration),
                TeacherUi.field("Langage enseigné", language)
        );
        HBox dates = TeacherUi.row(
                TeacherUi.field("Date de début", startDate),
                TeacherUi.field("Date de fin", endDate)
        );

        return card(top, row1, subField, descField, row2, dates);
    }

    private VBox buildObjectivesCard() {
        Button add = new Button("+ Ajouter");
        add.getStyleClass().add("le-add-button");
        add.setCursor(Cursor.HAND);
        add.setOnAction(e -> {
            objectives.add("Nouvel objectif pédagogique");
            refreshObjectives();
        });

        HBox top = cardHeader("fth-target", "Objectifs pédagogiques",
                "Ce que l’apprenant saura faire à la fin du niveau.");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(spacer, add);

        return card(top, objectivesBox);
    }

    private void refreshObjectives() {
        objectivesBox.getChildren().clear();
        if (objectives.isEmpty()) {
            Label empty = new Label("Aucun objectif pour le moment. Cliquez sur « + Ajouter ».");
            empty.getStyleClass().add("le-hint");
            empty.setWrapText(true);
            objectivesBox.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < objectives.size(); i++) {
            final int index = i;
            FontIcon drag = new FontIcon("fth-more-vertical");
            drag.getStyleClass().add("le-drag-icon");

            TextField field = new TextField(objectives.get(i));
            field.getStyleClass().add("t-input");
            field.textProperty().addListener((o, a, b) -> objectives.set(index, b == null ? "" : b));
            HBox.setHgrow(field, Priority.ALWAYS);

            Button remove = TeacherUi.iconButton("fth-x", "Supprimer", true, () -> {
                objectives.remove(index);
                refreshObjectives();
            });

            HBox row = new HBox(10, drag, field, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("le-objective-row");
            objectivesBox.getChildren().add(row);
        }
    }

    private VBox buildContentCard() {
        HBox top = cardHeader("fth-book-open", "Contenu associé",
                "Cours rattachés à ce niveau (lecture seule ici).");

        Hyperlink manage = new Hyperlink("Gérer les cours et leur ordre →");
        manage.getStyleClass().add("le-manage-link");
        manage.setOnAction(e -> onClose.run());

        VBox card = card(top, coursesBox, manage);
        return card;
    }

    private void refreshCourses() {
        coursesBox.getChildren().clear();
        if (level == null || level.getCours().isEmpty()) {
            Label empty = new Label(level == null
                    ? "Enregistrez d’abord le niveau pour y rattacher des cours."
                    : "Aucun cours associé. Ajoutez-en depuis Gestion des cours.");
            empty.getStyleClass().add("le-hint");
            empty.setWrapText(true);
            coursesBox.getChildren().add(empty);
            return;
        }
        String[] tones = {"tone-violet", "tone-blue", "tone-orange", "tone-rose"};
        int i = 0;
        for (Cours cours : level.getCours()) {
            coursesBox.getChildren().add(courseRow(cours, tones[i % tones.length]));
            i++;
        }
    }

    private HBox courseRow(Cours cours, String tone) {
        StackPane thumb = new StackPane(icon("fth-code", "le-thumb-icon"));
        thumb.getStyleClass().addAll("le-thumb", tone);

        Label title = new Label(cours.getTitre());
        title.getStyleClass().add("le-row-title");
        Label meta = new Label("Ordre " + cours.getOrdre() + " · Cours du niveau");
        meta.getStyleClass().add("le-row-sub");
        VBox copy = new VBox(2, title, meta);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Label badge = new Label("Publié");
        badge.getStyleClass().addAll("le-chip", "le-chip-ok");

        HBox row = new HBox(12, thumb, copy, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("le-content-row");
        return row;
    }

    /* ===================== COLONNE DROITE ===================== */

    private VBox buildSideColumn() {
        VBox col = new VBox(16, buildPublicationCard(), buildPrereqCard(), buildWarning());
        col.setPrefWidth(300);
        col.setMinWidth(280);
        col.setMaxWidth(320);
        return col;
    }

    private VBox buildPublicationCard() {
        HBox top = cardHeader("fth-upload-cloud", "Publication", null);

        Label badge = new Label(level != null && NiveauStatus.isComplete(level) ? "Complet" : "Brouillon");
        badge.getStyleClass().addAll("le-badge",
                level != null && NiveauStatus.isComplete(level) ? "le-badge-ok" : "le-badge-draft");

        showInPath.getStyleClass().add("le-check");
        showInPath.setSelected(true);

        return card(top, badge,
                TeacherUi.field("Statut", status),
                TeacherUi.field("Responsable pédagogique", owner),
                showInPath);
    }

    private VBox buildPrereqCard() {
        HBox top = cardHeader("fth-shield", "Prérequis et validation", null);

        requirePrevious.getStyleClass().add("le-check");
        requireTest.getStyleClass().add("le-check");
        requirePrevious.setSelected(true);
        requireTest.setSelected(true);

        HBox validationRow = TeacherUi.row(
                TeacherUi.field("Seuil (%)", threshold),
                TeacherUi.field("Tentatives", attempts)
        );

        return card(top,
                TeacherUi.field("Niveau précédent", previousLevel),
                requirePrevious,
                requireTest,
                TeacherUi.field("Test de validation associé", validationTest),
                validationRow);
    }

    private HBox buildWarning() {
        FontIcon ic = icon("fth-alert-triangle", "le-warn-icon");
        Label text = new Label(
                "Les modifications peuvent impacter les apprenants déjà inscrits à ce niveau "
                        + "(progression, seuil et accès au contenu).");
        text.setWrapText(true);
        text.getStyleClass().add("le-warn-text");
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox box = new HBox(10, ic, text);
        box.getStyleClass().add("le-warn");
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    /* ===================== ACTION BAR ===================== */

    private HBox buildActionBar() {
        Enseignant teacher = TeacherUi.currentTeacher();
        String who = teacher.getPrenom() + " " + teacher.getNom();
        Label info = new Label(level == null
                ? "Nouveau niveau · non enregistré"
                : "Dernière modification par " + who + " · " + LocalDateTime.now().format(LAST_EDIT));
        info.getStyleClass().add("le-action-info");
        HBox.setHgrow(info, Priority.ALWAYS);

        Button cancel = TeacherUi.secondaryButton("Annuler", "fth-x", onClose);
        Button save = TeacherUi.primaryButton(
                level == null ? "Enregistrer le niveau" : "Enregistrer les modifications",
                "fth-check",
                this::save);

        HBox bar = new HBox(12, info, cancel, save);
        bar.getStyleClass().add("le-actionbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(bar, new Insets(20, -28, -30, -28));
        return bar;
    }

    /* ===================== DATA ===================== */

    private void configureFields() {
        name.getStyleClass().add("t-input");
        name.setPromptText("Ex. : Intermédiaire");

        subtitle.getStyleClass().add("t-input");
        subtitle.setPromptText("Ex. : Structures de données");

        description.getStyleClass().add("t-input");
        description.setPromptText("Présentez le niveau, les notions abordées et le public visé.");
        description.setWrapText(true);
        description.setPrefRowCount(4);

        startDate.getStyleClass().add("t-input");
        endDate.getStyleClass().add("t-input");

        language.getStyleClass().add("t-input");
        language.getItems().setAll("Python 3.12", "Python 3.11", "Python 3.10");
        language.getSelectionModel().selectFirst();

        status.getStyleClass().add("t-input");
        status.getItems().setAll("Publié", "Brouillon", "Archivé");
        status.getSelectionModel().select(
                level != null && NiveauStatus.isComplete(level) ? "Publié" : "Brouillon");

        Enseignant teacher = TeacherUi.currentTeacher();
        String teacherName = teacher.getPrenom() + " " + teacher.getNom();
        owner.getStyleClass().add("t-input");
        owner.getItems().setAll(teacherName);
        owner.getSelectionModel().selectFirst();

        List<Niveau> levels = service.findNiveaux(teacher);
        previousLevel.getStyleClass().add("t-input");
        previousLevel.setPromptText("Aucun");
        previousLevel.setConverter(levelConverter());
        previousLevel.getItems().setAll(levels.stream()
                .filter(n -> level == null || n.getIdNiveau() != level.getIdNiveau())
                .toList());
        if (level != null) {
            levels.stream()
                    .filter(n -> n.getOrdre() == level.getOrdre() - 1)
                    .findFirst()
                    .ifPresent(previousLevel.getSelectionModel()::select);
        } else if (!previousLevel.getItems().isEmpty()) {
            previousLevel.getSelectionModel().selectLast();
        }

        validationTest.getStyleClass().add("t-input");
        validationTest.setPromptText("Aucun test");
        if (level != null && level.getTest() != null) {
            Test test = level.getTest();
            String label = "Test · " + test.getQuestions().size() + " question(s)";
            validationTest.getItems().setAll(label);
            validationTest.getSelectionModel().selectFirst();
        } else {
            validationTest.getItems().setAll("À définir après création du test");
            validationTest.getSelectionModel().selectFirst();
        }
    }

    private void loadExistingValues() {
        if (level == null) {
            objectives.setAll(
                    "Comprendre les notions fondamentales du niveau",
                    "Réussir les exercices associés",
                    "Valider le test de fin de niveau"
            );
            return;
        }
        name.setText(level.getNom());
        description.setText(level.getDescription() == null ? "" : level.getDescription());
        subtitle.setText(firstLineHint(level));
        objectives.setAll(
                "Maîtriser le contenu du niveau « " + level.getNom() + " »",
                "Compléter les " + level.getCours().size() + " cours associés",
                "Atteindre le seuil de " + Math.round(level.getSeuilReussite()) + " %"
        );
    }

    private String firstLineHint(Niveau niveau) {
        String d = niveau.getDescription();
        if (d == null || d.isBlank()) {
            return "";
        }
        String first = d.strip().split("\\R", 2)[0];
        return first.length() > 60 ? first.substring(0, 57) + "…" : first;
    }

    private void save() {
        try {
            onSaved.accept(submit());
        } catch (IllegalArgumentException e) {
            TeacherUi.show(error, e.getMessage(), true);
        }
    }

    private String submit() {
        String title = name.getText() == null ? "" : name.getText().trim();
        String text = description.getText() == null ? "" : description.getText().trim();
        if (!subtitle.getText().isBlank() && !text.contains(subtitle.getText().trim())) {
            text = subtitle.getText().trim() + (text.isBlank() ? "" : "\n" + text);
        }

        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        int niveauOrder = TeacherUi.intValue(order);
        int days = TeacherUi.intValue(duration);
        int passMark = TeacherUi.intValue(threshold);

        validate(title, start, end, niveauOrder, days, passMark);

        if (level == null) {
            service.creerNiveau(TeacherUi.currentTeacher(), title, text, niveauOrder, start, end, passMark, days);
            return "Niveau « " + title + " » créé.";
        }
        service.modifierNiveau(level, title, text, niveauOrder, start, end, passMark, days);
        return "Niveau « " + title + " » modifié.";
    }

    private void validate(String title, LocalDate start, LocalDate end, int niveauOrder, int days, int passMark) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Le nom du niveau est obligatoire.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure ou égale à la date de début.");
        }
        if (niveauOrder < 1) {
            throw new IllegalArgumentException("L'ordre du niveau doit être supérieur à zéro.");
        }
        if (days < 1) {
            throw new IllegalArgumentException("La durée doit être supérieure à zéro.");
        }
        if (passMark < 1 || passMark > 100) {
            throw new IllegalArgumentException("Le seuil de réussite doit être compris entre 1 et 100 %.");
        }
    }

    /* ===================== HELPERS ===================== */

    private VBox card(Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("le-card");
        return card;
    }

    private HBox cardHeader(String iconCode, String title, String subtitle) {
        FontIcon ic = icon(iconCode, "le-card-icon");
        Label t = new Label(title);
        t.getStyleClass().add("le-card-title");
        VBox copy = new VBox(2, t);
        if (subtitle != null) {
            Label s = new Label(subtitle);
            s.getStyleClass().add("le-card-sub");
            s.setWrapText(true);
            copy.getChildren().add(s);
        }
        HBox row = new HBox(10, ic, copy);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(copy, Priority.ALWAYS);
        return row;
    }

    private FontIcon icon(String code, String style) {
        FontIcon icon = new FontIcon(code);
        icon.getStyleClass().add(style);
        return icon;
    }

    private StringConverter<Niveau> levelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Niveau n) {
                return n == null ? "" : TeacherUi.levelLabel(n);
            }

            @Override
            public Niveau fromString(String s) {
                return null;
            }
        };
    }
}
