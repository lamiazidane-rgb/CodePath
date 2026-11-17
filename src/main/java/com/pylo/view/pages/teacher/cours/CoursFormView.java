package com.pylo.view.pages.teacher.cours;

import com.pylo.domain.learning.Cours.Chapitre;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.FormView;
import com.pylo.view.pages.teacher.common.TeacherUi;
import com.pylo.view.pages.teacher.cours.CoursView.CoursFlow;
import com.pylo.view.pages.teacher.cours.CoursView.Ui;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;

/** Étape 1 : informations générales d'un cours (création ou modification). */
class CoursFormView extends FormView {

    private final CatalogueService service = CatalogueService.getInstance();
    private final Cours course;                       // null = création
    private final AtomicReference<Cours> saved;       // cours enregistré, lu par CoursFlow.courseSaved

    private final ComboBox<Niveau> level;
    private final Spinner<Integer> order = TeacherUi.spinner(1, 99, 1);
    private final Spinner<Integer> hours = TeacherUi.spinner(0, 500, 2);
    private final TextField title = Ui.input(new TextField(), "Ex. : Les conditions en Java");
    private final TextArea description = Ui.input(new TextArea(), "Description du cours");
    private final TextArea objectives = Ui.input(new TextArea(), "Ce que l'apprenant saura faire à la fin du cours");
    private final TextField prerequisites = Ui.input(new TextField(), "Ex. : avoir terminé « Les variables »");
    private final Label coverName = Ui.label("Aucune image", "t-hint");
    private String coverPath;

    static CoursFormView open(CoursFlow flow, Cours course, List<Niveau> levels) {
        return new CoursFormView(flow, course, levels, new AtomicReference<>());
    }

    private CoursFormView(CoursFlow flow, Cours course, List<Niveau> levels, AtomicReference<Cours> saved) {
        super(course == null ? "Nouveau cours — étape 1 : informations générales" : "Modifier le cours « " + course.getTitre() + " »",
                "Le niveau est choisi parmi ceux déjà créés. Les chapitres et leçons s'ajoutent à l'étape suivante.",
                flow::list, text -> flow.courseSaved(saved.get(), course == null, text));
        this.course = course;
        this.saved = saved;

        level = TeacherUi.levelCombo(levels);
        for (TextArea area : List.of(description, objectives)) {
            area.setWrapText(true);
            area.setPrefRowCount(3);
        }

        if (course == null) {
            updateNextOrder();
            level.valueProperty().addListener((obs, oldLevel, newLevel) -> updateNextOrder());
        } else {
            level.getSelectionModel().select(course.getNiveau());
            order.getValueFactory().setValue(course.getOrdre());
            hours.getValueFactory().setValue((course.getDureeMinutes() + 30) / 60);
            title.setText(course.getTitre());
            description.setText(Ui.orDefault(course.getDescription(), ""));
            objectives.setText(Ui.orDefault(course.getObjectifs(), ""));
            prerequisites.setText(Ui.orDefault(course.getPrerequis(), ""));
            coverPath = course.getImageCouverture();
            coverName.setText(Ui.orDefault(coverPath, "Aucune image"));
        }

        HBox cover = new HBox(10, Ui.secondaryButton("Ajouter une image", "fth-image", this::chooseCover), coverName);
        cover.setAlignment(Pos.CENTER_LEFT);
        HBox duration = new HBox(8, hours, Ui.label("heures", "t-hint"));
        duration.setAlignment(Pos.CENTER_LEFT);

        build(TeacherUi.row(TeacherUi.field("Niveau *", level), TeacherUi.field("Rang dans le niveau", order)),
                TeacherUi.field("Nom du cours *", title),
                TeacherUi.field("Description *", description),
                TeacherUi.field("Objectifs pédagogiques *", objectives),
                TeacherUi.row(TeacherUi.field("Durée estimée", duration), TeacherUi.field("Prérequis", prerequisites)),
                TeacherUi.field("Image de couverture (facultative)", cover));
    }

    @Override
    protected String submit() {
        Niveau selected = level.getValue();
        if (selected == null) {
            throw new IllegalArgumentException("Choisissez un niveau.");
        }
        String name = title.getText().trim();
        String summary = required(description, "La description est obligatoire.");
        String goals = required(objectives, "Les objectifs pédagogiques sont obligatoires.");
        int rank = TeacherUi.intValue(order);

        Cours target;
        if (course == null) {
            target = service.creerCours(selected, name, summary, "", rank);
        } else {
            service.modifierCours(course, selected, name, summary, course.afficherContenu(), rank);
            target = course;
        }
        target.setObjectifs(goals);
        target.setDureeMinutes(TeacherUi.intValue(hours) * 60);
        target.setPrerequis(Ui.blankToNull(prerequisites.getText()));
        target.setImageCouverture(coverPath);

        saved.set(target);

        return course == null ? "Cours « " + name + " » créé en brouillon." : "Cours « " + name + " » modifié.";
    }

    private void chooseCover() {
        Ui.chooseFile(this, "Image de couverture", "*.png", "*.jpg", "*.jpeg", "*.gif").ifPresent(file -> {
            coverPath = file.getAbsolutePath();
            coverName.setText(file.getName());
        });
    }

    /** Propose le prochain rang libre du niveau choisi (création uniquement). */
    private void updateNextOrder() {
        if (level.getValue() != null) {
            order.getValueFactory().setValue(service.prochainOrdreCours(level.getValue()));
        }
    }

    private static String required(TextInputControl field, String error) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
        return value;
    }

    /** Ajout et modification d'un chapitre (l'ordre se règle ensuite avec les flèches de la liste). */
    static class ChapitreFormView extends FormView {

        private final CoursFlow flow;
        private final Cours course;
        private final Chapitre chapter;     // null = création

        private final TextField title = Ui.input(new TextField(), "Ex. : Introduction aux variables");
        private final TextArea description = Ui.input(new TextArea(), "Présentation du chapitre");
        private final TextArea goals = Ui.input(new TextArea(), "Ex. : comprendre l'utilité des variables");
        private final Spinner<Integer> minutes = TeacherUi.spinner(0, 600, 30);

        ChapitreFormView(CoursFlow flow, Cours course, Chapitre chapter) {
            super(chapter == null ? "Nouveau chapitre" : "Modifier le chapitre « " + chapter.getTitre() + " »",
                    "Cours : " + course.getTitre(),
                    () -> flow.content(course, null), text -> flow.content(course, text));
            this.flow = flow;
            this.course = course;
            this.chapter = chapter;

            description.setWrapText(true);
            description.setPrefRowCount(3);
            goals.setWrapText(true);
            goals.setPrefRowCount(3);
            if (chapter != null) {
                title.setText(chapter.getTitre());
                description.setText(Ui.orDefault(chapter.getDescription(), ""));
                goals.setText(Ui.orDefault(chapter.getObjectifs(), ""));
                minutes.getValueFactory().setValue(chapter.getDureeMinutes());
            }

            build(TeacherUi.field("Titre *", title),
                    TeacherUi.field("Description", description),
                    TeacherUi.field("Objectifs", goals),
                    TeacherUi.field("Durée estimée (minutes)", minutes));
        }

        @Override
        protected String submit() {
            String name = title.getText() == null ? "" : title.getText().trim();
            String summary = Ui.blankToNull(description.getText());
            String objectives = Ui.blankToNull(goals.getText());
            int duration = TeacherUi.intValue(minutes);

            if (chapter == null) {
                course.ajouterChapitre(new Chapitre(Cours.nouvelId(), name, summary, objectives, course.nombreChapitres() + 1, duration));
            } else {
                chapter.setTitre(name);
                chapter.setDescription(summary);
                chapter.setObjectifs(objectives);
                chapter.setDureeMinutes(duration);
            }
            flow.save(course);
            return "Chapitre « " + name + " » " + (chapter == null ? "ajouté." : "modifié.");
        }
    }
}