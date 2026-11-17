package com.pylo.view.pages.teacher.exercice;

import com.pylo.domain.enums.Difficulte;
import com.pylo.domain.learning.Exercice;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.FormView;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Consumer;

/** Création et modification d'un exercice (attributs de la classe Exercice du domaine). */
class ExerciceFormView extends FormView {

    private static final double DEFAULT_POINTS = 10;

    private final CatalogueService service = CatalogueService.getInstance();
    private final Exercice exercise;      // null = création

    private final ComboBox<Niveau> level;
    private final TextField title = new TextField();
    private final TextArea statement = new TextArea();
    private final ComboBox<Difficulte> difficulty = new ComboBox<>();
    private final Spinner<Double> points;

    /** @param exercise exercice à modifier, ou null pour en créer un */
    ExerciceFormView(Exercice exercise, List<Niveau> levels, Runnable onClose, Consumer<String> onSaved) {
        super(exercise == null ? "Nouvel exercice" : "Modifier l'exercice « " + exercise.getTitre() + " »",
                "Un exercice appartient à un niveau ; son barème indique les points qu'il rapporte.", onClose, onSaved);
        this.exercise = exercise;

        level = TeacherUi.levelCombo(levels);
        points = TeacherUi.decimalSpinner(0, 100, exercise == null ? DEFAULT_POINTS : exercise.getBareme());

        difficulty.getStyleClass().add("t-input");
        difficulty.getItems().setAll(Difficulte.values());
        difficulty.setConverter(new StringConverter<>() {
            @Override
            public String toString(Difficulte value) {
                return value == null ? "" : value.getLibelle();
            }

            @Override
            public Difficulte fromString(String text) {
                return null;
            }
        });
        difficulty.getSelectionModel().select(Difficulte.FACILE);

        title.getStyleClass().add("t-input");
        title.setPromptText("Ex. : Afficher un message");
        statement.getStyleClass().add("t-input");
        statement.setPromptText("Consigne donnée à l'apprenant");
        statement.setWrapText(true);
        statement.setPrefRowCount(6);

        if (exercise != null) {
            level.getSelectionModel().select(exercise.getNiveau());
            title.setText(exercise.getTitre());
            statement.setText(exercise.getEnonce() == null ? "" : exercise.getEnonce());
            for (Difficulte value : Difficulte.values()) {
                if (value.getLibelle().equals(exercise.getDifficulte())) {
                    difficulty.getSelectionModel().select(value);
                }
            }
        }

        build(TeacherUi.row(TeacherUi.field("Niveau *", level),
                        TeacherUi.field("Difficulté", difficulty),
                        TeacherUi.field("Barème (points)", points)),
                TeacherUi.field("Titre *", title),
                TeacherUi.field("Énoncé", statement));
    }

    @Override
    protected String submit() {
        Niveau selected = level.getValue();
        if (selected == null) {
            throw new IllegalArgumentException("Choisissez un niveau.");
        }
        String name = title.getText().trim();
        String text = statement.getText().trim();
        double mark = TeacherUi.doubleValue(points);

        if (exercise == null) {
            service.creerExercice(selected, name, text, difficulty.getValue(), mark);
            return "Exercice « " + name + " » créé.";
        }
        service.modifierExercice(exercise, selected, name, text, difficulty.getValue(), mark);
        return "Exercice « " + name + " » modifié.";
    }
}
