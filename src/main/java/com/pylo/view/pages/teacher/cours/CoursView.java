package com.pylo.view.pages.teacher.cours;

import com.pylo.domain.learning.Cours.Chapitre;
import com.pylo.domain.learning.Cours.Lecon;
import com.pylo.domain.learning.Cours.StatutCours;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.DataTable.Column;
import com.pylo.view.pages.teacher.common.DataTable;
import com.pylo.view.pages.teacher.common.LevelFilter;
import com.pylo.view.pages.teacher.common.TeacherPage;
import com.pylo.view.pages.teacher.common.TeacherUi;
import com.pylo.view.pages.teacher.cours.ChapitreManagementView.LeconEditorView;
import com.pylo.view.pages.teacher.cours.CoursDetailView.LeconPreviewView;
import com.pylo.view.pages.teacher.cours.CoursFormView.ChapitreFormView;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

/** Gestion des cours : statistiques, recherche, filtres, tri et actions sur chaque cours. */
public class CoursView extends TeacherPage {

    private static final Map<String, Comparator<Cours>> SORTS = new LinkedHashMap<>();

    static {
        SORTS.put("Plus récents d'abord", Comparator.comparingInt(Cours::getIdCours).reversed());
        SORTS.put("Plus anciens d'abord", Comparator.comparingInt(Cours::getIdCours));
        SORTS.put("Titre (A → Z)", Comparator.comparing(c -> c.getTitre().toLowerCase()));
        SORTS.put("Dernière modification", Comparator.comparing(Cours::getDateModification).reversed());
    }

    private final CatalogueService service = CatalogueService.getInstance();
    private final CoursFlow flow = new CoursFlow(this::showView, this::showList, this::showList);

    public CoursView() {
        showList();
    }

    @Override
    protected Node buildList(Label message) {
        List<Niveau> levels = service.findNiveaux(TeacherUi.currentTeacher());
        List<Cours> courses = service.findCours(TeacherUi.currentTeacher());

        TextField search = Ui.input(new TextField(), "Rechercher un cours par titre");
        HBox.setHgrow(search, Priority.ALWAYS);
        ComboBox<StatutCours> status = statusCombo();
        LevelFilter level = new LevelFilter(levels);
        ComboBox<String> sort = new ComboBox<>(FXCollections.observableArrayList(SORTS.keySet()));
        sort.getStyleClass().add("t-input");
        sort.getSelectionModel().selectFirst();

        DataTable table = new DataTable(
                Column.grow("Cours"),
                Column.flex("Niveau", 120),
                Column.flex("Contenu", 190),
                Column.flex("Statut", 170),
                Column.flex("Modifié le", 100),
                Column.fixed("Actions", 200));

        String emptyMessage = levels.isEmpty()
                ? "Aucun niveau : créez d'abord un niveau dans « Gestion des niveaux »."
                : "Aucun cours ne correspond : modifiez les filtres ou cliquez sur « Nouveau cours ».";

        Runnable refresh = () -> table.setItems(
                courses.stream().filter(matching(search, status, level)).sorted(SORTS.get(sort.getValue())).toList(),
                course -> new Node[]{
                        titleCell(course),
                        DataTable.twoLines(TeacherUi.levelLabel(course.getNiveau()), "Rang " + course.getOrdre()),
                        DataTable.twoLines(course.nombreChapitres() + " chapitre(s) · " + course.nombreLecons() + " leçon(s)",
                                "Durée : " + Ui.duration(course.dureeTotaleMinutes())),
                        statusCell(course),
                        DataTable.text(Ui.date(course.getDateModification())),
                        actions(course)
                },
                emptyMessage);

        search.textProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        status.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        level.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        sort.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        refresh.run();

        HBox filters = new HBox(10, search, status, level, sort);
        filters.setAlignment(Pos.CENTER_LEFT);

        return new VBox(18,
                TeacherUi.header("Gestion des cours", "Créez et organisez vos contenus pédagogiques.",
                        TeacherUi.primaryButton("Ajouter un cours", "fth-plus", () -> flow.form(null))),
                message,
                statistics(courses),
                TeacherUi.card(filters, table));
    }

    /* ---------- Statistiques ---------- */

    private Node statistics(List<Cours> courses) {
        return new HBox(14,
                Ui.statCard("Total des cours", courses.size(), "fth-book", "t-tone-violet"),
                Ui.statCard("Publiés", count(courses, c -> c.getStatut() == StatutCours.PUBLIE), "fth-check-circle", "t-tone-blue"),
                Ui.statCard("Brouillons", count(courses, c -> c.getStatut() == StatutCours.BROUILLON), "fth-edit-3", "t-tone-orange"),
                Ui.statCard("En cours de création",
                        count(courses, c -> c.getStatut() == StatutCours.BROUILLON && !c.estPubliable()), "fth-clock", "t-tone-rose"));
    }

    private static long count(List<Cours> courses, Predicate<Cours> condition) {
        return courses.stream().filter(condition).count();
    }

    /* ---------- Filtres ---------- */

    private static ComboBox<StatutCours> statusCombo() {
        ComboBox<StatutCours> combo = new ComboBox<>();
        combo.getItems().add(null);                       // null = « tous les statuts »
        combo.getItems().addAll(StatutCours.values());
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(StatutCours s) { return s == null ? "Tous les statuts" : s.getLibelle(); }
            @Override public StatutCours fromString(String text) { return null; }
        });
        combo.getSelectionModel().selectFirst();
        combo.getStyleClass().add("t-input");
        return combo;
    }

    private static Predicate<Cours> matching(TextField search, ComboBox<StatutCours> status, LevelFilter level) {
        return course -> {
            String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
            return course.getTitre().toLowerCase().contains(query)
                    && (status.getValue() == null || course.getStatut() == status.getValue())
                    && level.selected().map(n -> n.equals(course.getNiveau())).orElse(true);
        };
    }

    /* ---------- Cellules ---------- */

    private Node titleCell(Cours course) {
        HBox cell = new HBox(10, Ui.cover(course.getImageCouverture(), 48, 40),
                DataTable.twoLines(course.getTitre(), Ui.orDefault(course.getDescription(), "Aucune description")));
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private Node statusCell(Cours course) {
        StatutCours current = course.getStatut();
        String tone = switch (current) {
            case PUBLIE -> "ok";
            case BROUILLON -> "warn";
            case ARCHIVE -> "neutral";
        };
        String toggleLabel = switch (current) {
            case PUBLIE -> "Dépublier";
            case BROUILLON -> "Publier";
            case ARCHIVE -> "Restaurer";
        };
        StatutCours target = current == StatutCours.BROUILLON ? StatutCours.PUBLIE : StatutCours.BROUILLON;

        HBox cell = new HBox(8, Ui.chip(current.getLibelle(), tone), Ui.linkButton(toggleLabel, () -> changeStatus(course, target)));
        cell.setAlignment(Pos.CENTER_LEFT);
        return cell;
    }

    private Node actions(Cours course) {
        HBox box = new HBox(2,
                Ui.iconButton("fth-eye", "Consulter", false, () -> flow.detail(course)),
                Ui.iconButton("fth-edit-2", "Modifier les informations", false, () -> flow.form(course)),
                Ui.iconButton("fth-layers", "Gérer le contenu (chapitres et leçons)", false, () -> flow.content(course, null)),
                Ui.iconButton("fth-archive", "Archiver", false, () -> changeStatus(course, StatutCours.ARCHIVE)),
                Ui.iconButton("fth-trash-2", "Supprimer", true, () -> delete(course)));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /* ---------- Actions ---------- */

    private void changeStatus(Cours course, StatutCours target) {
        String error = flow.changeStatus(course, target);
        if (error == null) {
            showList("Cours « " + course.getTitre() + " » : " + target.getLibelle().toLowerCase() + ".", false);
        } else {
            showList(error, true);
        }
    }

    private void delete(Cours course) {
        if (TeacherUi.confirm(this, "Supprimer le cours", "Supprimer le cours « " + course.getTitre()
                + " » avec ses chapitres et ses leçons ?", "Supprimer")) {
            service.supprimerCours(course);
            showList("Cours « " + course.getTitre() + " » supprimé.", false);
        }
    }

    /**
     * Point unique de navigation et d'enregistrement du module « Gestion des cours ».
     * Les pages ne se connaissent pas entre elles : elles demandent au flux d'ouvrir la page suivante.
     */
    static final class CoursFlow {

        private final CatalogueService service = CatalogueService.getInstance();
        private final Consumer<Node> viewer;
        private final Runnable toList;
        private final BiConsumer<String, Boolean> toListWithMessage;

        CoursFlow(Consumer<Node> viewer, Runnable toList, BiConsumer<String, Boolean> toListWithMessage) {
            this.viewer = viewer;
            this.toList = toList;
            this.toListWithMessage = toListWithMessage;
        }

        /* ---------- Navigation ---------- */

        void show(Node view) { viewer.accept(view); }

        void list() { toList.run(); }

        void list(String message, boolean error) { toListWithMessage.accept(message, error); }

        void detail(Cours course) { show(new CoursDetailView(this, course)); }

        /** Chapitres et leçons d'un cours ; {@code info} = message de succès affiché en haut (ou null). */
        void content(Cours course, String info) { show(new ChapitreManagementView(this, course, info)); }

        void chapterForm(Cours course, Chapitre chapter) { show(new ChapitreFormView(this, course, chapter)); }

        void lessonEditor(Cours course, Chapitre chapter, Lecon lesson) { show(new LeconEditorView(this, course, chapter, lesson)); }

        void preview(Lecon lesson, Runnable back) { show(new LeconPreviewView(lesson, back)); }

        /** Formulaire des informations générales (création si {@code course} est null). */
        void form(Cours course) {
            List<Niveau> levels = service.findNiveaux(TeacherUi.currentTeacher());
            if (levels.isEmpty()) {
                list("Créez d'abord un niveau dans « Gestion des niveaux » : un cours appartient toujours à un niveau.", true);
                return;
            }
            show(CoursFormView.open(this, course, levels));
        }

        /** Après l'étape 1 : un nouveau cours passe directement à l'organisation du contenu. */
        void courseSaved(Cours course, boolean created, String text) {
            if (created) {
                content(course, text + " Ajoutez maintenant les chapitres.");
            } else {
                list(text, false);
            }
        }

        /* ---------- Enregistrement ---------- */

        void save(Cours course) { service.enregistrerCours(course); }

        /** @return null si le changement a réussi, sinon le message d'erreur à afficher */
        String changeStatus(Cours course, StatutCours status) {
            try {
                course.changerStatut(status);
                save(course);
                return null;
            } catch (IllegalStateException e) {
                return e.getMessage();
            }
        }
    }

    /** Petits composants réutilisés par toutes les pages du module Gestion des cours. */
    static final class Ui {

        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private Ui() { }

        /* ---------- Boutons ---------- */

        static Button iconButton(String icon, String tooltip, boolean danger, Runnable action) {
            Button button = new Button();
            button.setGraphic(new FontIcon(icon));
            button.getStyleClass().add(danger ? "t-icon-danger" : "t-icon-btn");
            button.setTooltip(new Tooltip(tooltip));
            button.setOnAction(e -> action.run());
            return button;
        }

        /** @param icon code Ikonli ou null */
        static Button secondaryButton(String text, String icon, Runnable action) {
            Button button = new Button(text);
            if (icon != null) {
                button.setGraphic(new FontIcon(icon));
            }
            button.getStyleClass().add("t-btn-secondary");
            button.setOnAction(e -> action.run());
            return button;
        }

        static Button linkButton(String text, Runnable action) {
            Button button = new Button(text);
            button.getStyleClass().add("t-link");
            button.setOnAction(e -> action.run());
            return button;
        }

        /* ---------- Texte et mise en page ---------- */

        static Label label(String text, String styleClass) {
            Label label = new Label(text);
            label.getStyleClass().add(styleClass);
            label.setWrapText(true);
            return label;
        }

        /** @param tone ok, warn, danger ou neutral */
        static Label chip(String text, String tone) {
            Label chip = new Label(text);
            chip.getStyleClass().addAll("t-chip", "t-chip-" + tone);
            return chip;
        }

        static Region spacer() {
            Region region = new Region();
            HBox.setHgrow(region, Priority.ALWAYS);
            return region;
        }

        static <T extends TextInputControl> T input(T control, String prompt) {
            control.getStyleClass().add("t-input");
            control.setPromptText(prompt);
            return control;
        }

        static String orDefault(String text, String fallback) {
            return text == null || text.isBlank() ? fallback : text;
        }

        static String blankToNull(String text) {
            return text == null || text.isBlank() ? null : text.trim();
        }

        /** Carte avec titre de section. */
        static Node section(String title, Node... content) {
            VBox box = new VBox(10, label(title, "t-label"));
            box.getChildren().addAll(content);
            return com.pylo.view.pages.teacher.common.TeacherUi.card(box);
        }

        static Node empty(String text, String icon) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.getStyleClass().add("t-empty-icon");
            VBox box = new VBox(8, fontIcon, label(text, "t-hint"));
            box.setAlignment(Pos.CENTER);
            box.getStyleClass().add("t-empty");
            return box;
        }

        /** Affiche (ou masque si text est vide) un message de succès ou d'erreur. */
        static void show(Label message, String text, boolean error) {
            message.getStyleClass().setAll("label", error ? "t-error" : "t-success");
            message.setWrapText(true);
            message.setText(text);
            message.setVisible(text != null && !text.isBlank());
            message.setManaged(message.isVisible());
        }

        static Node statCard(String label, long value, String icon, String tone) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.getStyleClass().add("t-stat-icon");
            StackPane box = new StackPane(fontIcon);
            box.getStyleClass().addAll("t-stat-box", tone);

            HBox card = new HBox(14, box, new VBox(2, label(String.valueOf(value), "t-stat-value"), label(label, "t-stat-label")));
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().addAll("card", "t-stat");
            card.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(card, Priority.ALWAYS);
            return card;
        }

        /** Image de couverture miniature, ou pastille par défaut. */
        static Node cover(String path, double width, double height) {
            StackPane box = new StackPane();
            box.setMinSize(width, height);
            box.setPrefSize(width, height);
            box.setMaxSize(width, height);
            box.setStyle("-fx-background-color: #F0EBFF; -fx-background-radius: 8;");
            if (path != null && !path.isBlank()) {
                Image image = new Image(new File(path).toURI().toString(), width, height, false, true);
                if (!image.isError()) {
                    box.getChildren().add(new ImageView(image));
                    return box;
                }
            }
            FontIcon icon = new FontIcon("fth-book");
            icon.setStyle("-fx-icon-color: #6C3CE9; -fx-icon-size: 18px;");
            box.getChildren().add(icon);
            return box;
        }

        /* ---------- Formats ---------- */

        static String duration(int minutes) {
            if (minutes <= 0) {
                return "—";
            }
            int hours = minutes / 60;
            int rest = minutes % 60;
            if (hours == 0) {
                return rest + " min";
            }
            return rest == 0 ? hours + " h" : hours + " h " + String.format("%02d", rest);
        }

        static String date(LocalDate date) {
            return date == null ? "—" : DATE.format(date);
        }

        /* ---------- Fichiers ---------- */

        static Optional<File> chooseFile(Node owner, String title, String... patterns) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(title);
            if (patterns.length > 0) {
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers acceptés", patterns));
            }
            return Optional.ofNullable(chooser.showOpenDialog(owner.getScene().getWindow()));
        }
    }
}