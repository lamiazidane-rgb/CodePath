package com.pylo.view.pages.teacher.cours;

import com.pylo.domain.learning.Cours.Chapitre;
import com.pylo.domain.learning.Cours.Lecon;
import com.pylo.domain.learning.Cours.Ressource;
import com.pylo.domain.learning.Cours.TypeRessource;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Exemple;
import com.pylo.view.pages.teacher.common.TeacherUi;
import com.pylo.view.pages.teacher.cours.CoursView.CoursFlow;
import com.pylo.view.pages.teacher.cours.CoursView.Ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Étapes 2 et 3 : chapitres du cours, et leçons de chaque chapitre (ajout, modification, suppression, ordre). */
class ChapitreManagementView extends VBox {

    private final CoursFlow flow;
    private final Cours course;
    private final Label message = new Label();
    private final VBox chapters = new VBox(14);

    ChapitreManagementView(CoursFlow flow, Cours course, String info) {
        super(18);
        this.flow = flow;
        this.course = course;

        HBox navigation = new HBox(10,
                Ui.secondaryButton("Retour aux cours", "fth-arrow-left", flow::list),
                Ui.secondaryButton("Vérifier et publier", "fth-check-circle", () -> flow.detail(course)));
        getChildren().addAll(
                TeacherUi.header("Contenu : " + course.getTitre(), "Organisez les chapitres et les leçons du cours.",
                        TeacherUi.primaryButton("Ajouter un chapitre", "fth-plus", () -> flow.chapterForm(course, null))),
                navigation, message, chapters);
        Ui.show(message, info, false);
        render();
    }

    private void render() {
        chapters.getChildren().clear();
        if (course.getChapitres().isEmpty()) {
            chapters.getChildren().add(Ui.empty("Aucun chapitre : cliquez sur « Ajouter un chapitre ».", "fth-layers"));
            return;
        }
        for (Chapitre chapter : course.getChapitres()) {
            chapters.getChildren().add(chapterCard(chapter));
        }
    }

    /** Applique une modification, enregistre le cours puis redessine la page. */
    private void change(Runnable action, String success) {
        try {
            action.run();
            flow.save(course);
            Ui.show(message, success, false);
        } catch (IllegalArgumentException e) {
            Ui.show(message, e.getMessage(), true);
        }
        render();
    }

    /* ---------- Chapitres ---------- */

    private Node chapterCard(Chapitre chapter) {
        int index = course.getChapitres().indexOf(chapter);
        boolean first = index == 0;
        boolean last = index == course.getChapitres().size() - 1;

        Label number = Ui.label(String.valueOf(chapter.getOrdre()), "t-avatar");
        VBox titles = new VBox(2, Ui.label(chapter.getTitre(), "t-cell-strong"),
                Ui.label(Ui.orDefault(chapter.getDescription(), "Aucune description"), "t-cell-sub"));
        HBox header = new HBox(10, number, titles, Ui.spacer(),
                Ui.chip(Ui.duration(chapter.getDureeMinutes()), "neutral"),
                Ui.chip(chapter.getLecons().size() + " leçon(s)", "neutral"),
                move("Monter le chapitre", "fth-arrow-up", first, () -> change(() -> course.deplacerChapitre(chapter, -1), null)),
                move("Descendre le chapitre", "fth-arrow-down", last, () -> change(() -> course.deplacerChapitre(chapter, 1), null)),
                Ui.iconButton("fth-edit-2", "Modifier le chapitre", false, () -> flow.chapterForm(course, chapter)),
                Ui.iconButton("fth-trash-2", "Supprimer le chapitre", true, () -> deleteChapter(chapter)));
        header.setAlignment(Pos.CENTER_LEFT);

        VBox lessons = new VBox();
        for (Lecon lesson : chapter.getLecons()) {
            lessons.getChildren().add(lessonRow(chapter, lesson));
        }
        if (chapter.getLecons().isEmpty()) {
            lessons.getChildren().add(Ui.label("Aucune leçon dans ce chapitre.", "t-hint"));
        }
        Button addLesson = Ui.secondaryButton("Ajouter une leçon", "fth-plus", () -> flow.lessonEditor(course, chapter, null));

        return TeacherUi.card(header, lessons, addLesson);
    }

    private void deleteChapter(Chapitre chapter) {
        if (TeacherUi.confirm(this, "Supprimer le chapitre", "Supprimer le chapitre « " + chapter.getTitre()
                + " » et ses leçons ?", "Supprimer")) {
            change(() -> course.retirerChapitre(chapter), "Chapitre « " + chapter.getTitre() + " » supprimé.");
        }
    }

    /* ---------- Leçons ---------- */

    private Node lessonRow(Chapitre chapter, Lecon lesson) {
        int index = chapter.getLecons().indexOf(lesson);
        String summary = lesson.getExemples().size() + " exemple(s) · " + lesson.getRessources().size()
                + " ressource(s) · " + Ui.duration(lesson.getDureeMinutes());

        HBox row = new HBox(6,
                new VBox(2, Ui.label("Leçon " + lesson.getOrdre() + " : " + lesson.getTitre(), "t-cell-strong"),
                        Ui.label(summary, "t-cell-sub")),
                Ui.spacer(),
                Ui.iconButton("fth-eye", "Prévisualiser", false, () -> flow.preview(lesson, () -> flow.content(course, null))),
                move("Monter la leçon", "fth-arrow-up", index == 0, () -> change(() -> chapter.deplacerLecon(lesson, -1), null)),
                move("Descendre la leçon", "fth-arrow-down", index == chapter.getLecons().size() - 1,
                        () -> change(() -> chapter.deplacerLecon(lesson, 1), null)),
                Ui.iconButton("fth-edit-2", "Modifier la leçon", false, () -> flow.lessonEditor(course, chapter, lesson)),
                Ui.iconButton("fth-trash-2", "Supprimer la leçon", true, () -> {
                    if (TeacherUi.confirm(this, "Supprimer la leçon", "Supprimer la leçon « " + lesson.getTitre() + " » ?", "Supprimer")) {
                        change(() -> chapter.retirerLecon(lesson), "Leçon « " + lesson.getTitre() + " » supprimée.");
                    }
                }));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("t-list-row");
        return row;
    }

    private static Button move(String tooltip, String icon, boolean disabled, Runnable action) {
        Button button = Ui.iconButton(icon, tooltip, false, action);
        button.setDisable(disabled);
        return button;
    }

    /**
     * Éditeur de leçon : informations, contenu théorique, exemples de code et ressources.
     * L'éditeur travaille sur des copies : rien n'est modifié tant que « Enregistrer » n'est pas cliqué.
     */
    static class LeconEditorView extends VBox {

        private static final String TABLE = "\n| Colonne 1 | Colonne 2 |\n| --- | --- |\n| Valeur A | Valeur B |\n";

        private final CoursFlow flow;
        private final Cours course;
        private final Chapitre chapter;
        private Lecon lesson;                       // null tant que la leçon n'est pas enregistrée

        private final TextField title = Ui.input(new TextField(), "Ex. : Déclaration des variables");
        private final TextField description = Ui.input(new TextField(), "Ex. : Apprendre à déclarer une variable");
        private final Spinner<Integer> minutes = TeacherUi.spinner(0, 600, 15);
        private final TextArea content = Ui.input(new TextArea(), "Écrivez le contenu théorique de la leçon");
        private final List<Exemple> examples;
        private final List<Ressource> resources;
        private final VBox examplesBox = new VBox(8);
        private final VBox resourcesBox = new VBox(8);
        private final Label message = new Label();

        LeconEditorView(CoursFlow flow, Cours course, Chapitre chapter, Lecon lesson) {
            super(18);
            this.flow = flow;
            this.course = course;
            this.chapter = chapter;
            this.lesson = lesson;

            examples = lesson == null ? new ArrayList<>() : new ArrayList<>(lesson.getExemples());
            resources = lesson == null ? new ArrayList<>() : new ArrayList<>(lesson.getRessources());
            content.setWrapText(true);
            content.setPrefRowCount(16);
            if (lesson != null) {
                title.setText(lesson.getTitre());
                description.setText(Ui.orDefault(lesson.getDescription(), ""));
                content.setText(Ui.orDefault(lesson.getContenu(), ""));
                minutes.getValueFactory().setValue(lesson.getDureeMinutes());
            }

            HBox navigation = new HBox(10,
                    Ui.secondaryButton("Retour au contenu", "fth-arrow-left", () -> flow.content(course, null)),
                    Ui.secondaryButton("Prévisualiser", "fth-eye", this::preview));
            getChildren().addAll(
                    TeacherUi.header(lesson == null ? "Nouvelle leçon" : "Modifier la leçon", "Chapitre : " + chapter.getTitre(),
                            TeacherUi.primaryButton("Enregistrer", "fth-save", this::save)),
                    navigation, message,
                    TeacherUi.card(TeacherUi.row(TeacherUi.field("Titre *", title), TeacherUi.field("Durée estimée (minutes)", minutes)),
                            TeacherUi.field("Description", description)),
                    Ui.section("Contenu théorique", toolbar(),
                            Ui.label("Syntaxe : ## titre · **gras** · *italique* · - liste · [texte](lien) · ![image](chemin)", "t-hint"),
                            content),
                    Ui.section("Exemples de code", examplesBox,
                            Ui.secondaryButton("Ajouter un exemple de code", "fth-code", this::addExample)),
                    Ui.section("Ressources", resourcesBox,
                            Ui.secondaryButton("Ajouter une ressource", "fth-paperclip", this::addResource)));
            Ui.show(message, null, false);
            renderExamples();
            renderResources();
        }

        /* ---------- Enregistrement et aperçu ---------- */

        private void save() {
            try {
                String name = title.getText() == null ? "" : title.getText().trim();
                String summary = Ui.blankToNull(description.getText());
                int duration = TeacherUi.intValue(minutes);
                if (lesson == null) {
                    lesson = new Lecon(Cours.nouvelId(), name, summary, content.getText(), chapter.getLecons().size() + 1, duration);
                    chapter.ajouterLecon(lesson);
                } else {
                    lesson.setTitre(name);
                    lesson.setDescription(summary);
                    lesson.setContenu(content.getText());
                    lesson.setDureeMinutes(duration);
                }
                lesson.definirExemples(examples);
                lesson.definirRessources(resources);
                flow.save(course);
                Ui.show(message, "Leçon « " + name + " » enregistrée. Elle sera visible des apprenants à la publication du cours.", false);
            } catch (IllegalArgumentException e) {
                Ui.show(message, e.getMessage(), true);
            }
        }

        /** Aperçu de ce qui est à l'écran, même non enregistré. */
        private void preview() {
            String name = title.getText() == null || title.getText().isBlank() ? "Sans titre" : title.getText().trim();
            Lecon draft = new Lecon(0, name, description.getText(), content.getText(), 1, TeacherUi.intValue(minutes));
            draft.definirExemples(examples);
            draft.definirRessources(resources);
            flow.preview(draft, () -> flow.show(this));
        }

        /* ---------- Barre d'outils du texte ---------- */

        private Node toolbar() {
            FlowPane bar = new FlowPane(6, 6,
                    tool("Titre", () -> insert("\n## ")),
                    tool("Sous-titre", () -> insert("\n### ")),
                    tool("Gras", () -> wrap("**", "**")),
                    tool("Italique", () -> wrap("*", "*")),
                    tool("Liste", () -> insert("\n- ")),
                    tool("Lien", () -> wrap("[", "](https://)")),
                    tool("Tableau", () -> insert(TABLE)),
                    tool("Image", this::insertImage));
            bar.setAlignment(Pos.CENTER_LEFT);
            return bar;
        }

        private Node tool(String text, Runnable action) {
            return Ui.secondaryButton(text, null, action);
        }

        private void insert(String text) {
            content.insertText(content.getCaretPosition(), text);
            content.requestFocus();
        }

        private void wrap(String before, String after) {
            String selected = content.getSelectedText();
            content.replaceSelection(before + (selected.isEmpty() ? "texte" : selected) + after);
            content.requestFocus();
        }

        private void insertImage() {
            Ui.chooseFile(this, "Choisir une image", "*.png", "*.jpg", "*.jpeg", "*.gif")
                    .ifPresent(file -> insert("\n![" + file.getName() + "](" + file.getAbsolutePath() + ")\n"));
        }

        /* ---------- Exemples ---------- */

        private void addExample() {
            LeconDialogs.example(this, null).ifPresent(example -> {
                examples.add(example);
                renderExamples();
            });
        }

        private void renderExamples() {
            examplesBox.getChildren().clear();
            if (examples.isEmpty()) {
                examplesBox.getChildren().add(Ui.label("Aucun exemple de code pour cette leçon.", "t-hint"));
            }
            for (Exemple example : examples) {
                HBox row = new HBox(8,
                        new VBox(2, Ui.label(example.getTitre(), "t-cell-strong"),
                                Ui.label(Ui.orDefault(example.getDescription(), "Sans description"), "t-cell-sub")),
                        Ui.spacer(), Ui.chip(Ui.orDefault(example.getLangage(), "Code"), "neutral"),
                        Ui.iconButton("fth-edit-2", "Modifier", false, () -> LeconDialogs.example(this, example).ifPresent(updated -> {
                            examples.set(examples.indexOf(example), updated);
                            renderExamples();
                        })),
                        Ui.iconButton("fth-trash-2", "Retirer", true, () -> {
                            examples.remove(example);
                            renderExamples();
                        }));
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("t-list-row");
                examplesBox.getChildren().add(row);
            }
        }

        /* ---------- Ressources ---------- */

        private void addResource() {
            LeconDialogs.resource(this, null).ifPresent(resource -> {
                resources.add(resource);
                renderResources();
            });
        }

        private void renderResources() {
            resourcesBox.getChildren().clear();
            if (resources.isEmpty()) {
                resourcesBox.getChildren().add(Ui.label("Aucune ressource jointe à cette leçon.", "t-hint"));
            }
            for (Ressource resource : resources) {
                HBox row = new HBox(8,
                        new VBox(2, Ui.label(resource.getTitre(), "t-cell-strong"), Ui.label(resource.getEmplacement(), "t-cell-sub")),
                        Ui.spacer(), Ui.chip(resource.getType().getLibelle(), "neutral"),
                        Ui.iconButton("fth-edit-2", "Modifier", false, () -> LeconDialogs.resource(this, resource).ifPresent(updated -> {
                            resources.set(resources.indexOf(resource), updated);
                            renderResources();
                        })),
                        Ui.iconButton("fth-trash-2", "Retirer", true, () -> {
                            resources.remove(resource);
                            renderResources();
                        }));
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("t-list-row");
                resourcesBox.getChildren().add(row);
            }
        }
    }

    /** Fenêtres d'ajout / modification d'un exemple de code et d'une ressource. */
    static final class LeconDialogs {

        private static final List<String> LANGUAGES = List.of("Java", "Python", "C", "C++", "JavaScript", "SQL", "HTML", "Autre");

        private LeconDialogs() { }

        /** @param initial exemple à modifier, ou null pour en créer un */
        static Optional<Exemple> example(Node owner, Exemple initial) {
            TextField title = Ui.input(new TextField(), "Ex. : Déclarer un entier");
            TextField description = Ui.input(new TextField(), "Ce que l'exemple illustre");
            ComboBox<String> language = new ComboBox<>(FXCollections.observableArrayList(LANGUAGES));
            language.getStyleClass().add("t-input");
            language.setValue("Java");
            TextArea code = Ui.input(new TextArea(), "Code source");
            code.setStyle("-fx-font-family: monospace;");
            code.setPrefRowCount(9);
            TextArea result = Ui.input(new TextArea(), "Résultat attendu");
            result.setPrefRowCount(2);
            TextArea explanation = Ui.input(new TextArea(), "Explication du code");
            explanation.setPrefRowCount(3);
            explanation.setWrapText(true);

            if (initial != null) {
                title.setText(initial.getTitre());
                description.setText(Ui.orDefault(initial.getDescription(), ""));
                language.setValue(Ui.orDefault(initial.getLangage(), "Java"));
                code.setText(initial.getCodeSource());
                result.setText(Ui.orDefault(initial.getResultatAttendu(), ""));
                explanation.setText(Ui.orDefault(initial.getExplication(), ""));
            }

            return ask(owner, initial == null ? "Ajouter un exemple de code" : "Modifier l'exemple", () -> {
                        if (title.getText().isBlank()) {
                            throw new IllegalArgumentException("Le titre de l'exemple est obligatoire.");
                        }
                        if (code.getText().isBlank()) {
                            throw new IllegalArgumentException("Le code source est obligatoire.");
                        }
                        return new Exemple(initial == null ? Cours.nouvelId() : initial.getIdExemple(), title.getText().trim(), code.getText(),
                                Ui.blankToNull(description.getText()), language.getValue(),
                                Ui.blankToNull(result.getText()), Ui.blankToNull(explanation.getText()));
                    }, TeacherUi.field("Titre de l'exemple *", title), TeacherUi.field("Description", description),
                    TeacherUi.field("Langage", language), TeacherUi.field("Code source *", code),
                    TeacherUi.field("Résultat attendu", result), TeacherUi.field("Explication du code", explanation));
        }

        /** @param initial ressource à modifier, ou null pour en créer une */
        static Optional<Ressource> resource(Node owner, Ressource initial) {
            ComboBox<TypeRessource> type = new ComboBox<>(FXCollections.observableArrayList(TypeRessource.values()));
            type.getStyleClass().add("t-input");
            type.setConverter(new StringConverter<>() {
                @Override public String toString(TypeRessource t) { return t == null ? "" : t.getLibelle(); }
                @Override public TypeRessource fromString(String text) { return null; }
            });
            type.setValue(initial == null ? TypeRessource.PDF : initial.getType());
            TextField title = Ui.input(new TextField(), "Ex. : Aide-mémoire sur les variables");
            TextField description = Ui.input(new TextField(), "Facultative");
            TextField location = Ui.input(new TextField(), "");
            HBox.setHgrow(location, Priority.ALWAYS);

            Button browse = Ui.secondaryButton("Parcourir", "fth-folder", () ->
                    Ui.chooseFile(location, "Choisir un fichier", patterns(type.getValue()))
                            .ifPresent(file -> location.setText(file.getAbsolutePath())));
            type.valueProperty().addListener((obs, oldType, newType) -> updateLocation(newType, location, browse));
            updateLocation(type.getValue(), location, browse);

            if (initial != null) {
                title.setText(initial.getTitre());
                description.setText(Ui.orDefault(initial.getDescription(), ""));
                location.setText(initial.getEmplacement());
            }
            HBox source = new HBox(8, location, browse);
            source.setAlignment(Pos.CENTER_LEFT);

            return ask(owner, initial == null ? "Ajouter une ressource" : "Modifier la ressource", () -> new Ressource(
                            initial == null ? Cours.nouvelId() : initial.getIdRessource(), title.getText(), type.getValue(),
                            location.getText(), Ui.blankToNull(description.getText())),
                    TeacherUi.field("Type *", type), TeacherUi.field("Titre *", title),
                    TeacherUi.field("Fichier ou lien *", source), TeacherUi.field("Description", description));
        }

        /* ---------- Outils ---------- */

        private static void updateLocation(TypeRessource type, TextField location, Button browse) {
            boolean link = type == TypeRessource.LIEN;
            browse.setDisable(link);
            location.setPromptText(link ? "https://..." : "Chemin du fichier");
        }

        private static String[] patterns(TypeRessource type) {
            return switch (type) {
                case PDF -> new String[]{"*.pdf"};
                case IMAGE -> new String[]{"*.png", "*.jpg", "*.jpeg", "*.gif"};
                case VIDEO -> new String[]{"*.mp4", "*.mkv", "*.avi", "*.mov"};
                default -> new String[0];
            };
        }

        /** Ouvre la fenêtre ; le résultat n'est renvoyé que si « Enregistrer » est validé sans erreur. */
        private static <T> Optional<T> ask(Node owner, String title, Supplier<T> builder, Node... fields) {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(title);
            dialog.initOwner(owner.getScene().getWindow());

            DialogPane pane = dialog.getDialogPane();
            pane.getStylesheets().addAll(owner.getScene().getStylesheets());
            pane.getStyleClass().add("t-dialog");

            Label error = Ui.label("", "t-error");
            VBox form = new VBox(10);
            form.setPrefWidth(560);
            form.getChildren().addAll(fields);
            form.getChildren().add(error);
            pane.setContent(form);

            ButtonType save = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            pane.getButtonTypes().addAll(save, ButtonType.CANCEL);

            AtomicReference<T> result = new AtomicReference<>();
            pane.lookupButton(save).addEventFilter(ActionEvent.ACTION, e -> {
                try {
                    result.set(builder.get());
                } catch (IllegalArgumentException ex) {
                    error.setText(ex.getMessage());
                    e.consume();
                }
            });
            dialog.showAndWait();
            return Optional.ofNullable(result.get());
        }
    }
}