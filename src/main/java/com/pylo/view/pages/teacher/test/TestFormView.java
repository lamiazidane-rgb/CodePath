package com.pylo.view.pages.teacher.test;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.FormView;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Consumer;

/** Création et modification du test d'un niveau (attributs de la classe Test du domaine). */
class TestFormView extends FormView {

    private static final int DEFAULT_DURATION_MINUTES = 30;
    private static final double DEFAULT_MAX_MARK = 20;
    private static final int DEFAULT_ATTEMPTS = 3;

    private final CatalogueService service = CatalogueService.getInstance();
    private final Test test;              // null = création

    private final ComboBox<Niveau> level;
    private final TextField title = new TextField();
    private final TextArea description = new TextArea();
    private final Spinner<Integer> duration;
    private final Spinner<Double> maxMark;
    private final Spinner<Integer> threshold;
    private final Spinner<Integer> attempts;

    /**
     * @param test       test à modifier, ou null pour en créer un
     * @param levels     niveaux proposés : ceux qui n'ont pas encore de test (création) ou le niveau du test (modification)
     */
    TestFormView(Test test, List<Niveau> levels, Runnable onClose, Consumer<String> onSaved) {
        super(test == null ? "Nouveau test" : "Modifier le test « " + test.getTitre() + " »",
                "Un niveau possède exactement un test : l'apprenant le réussit pour valider le niveau.", onClose, onSaved);
        this.test = test;

        level = TeacherUi.levelCombo(levels);
        duration = TeacherUi.spinner(1, 600, test == null ? DEFAULT_DURATION_MINUTES : test.getDuree());
        maxMark = TeacherUi.decimalSpinner(1, 1000, test == null ? DEFAULT_MAX_MARK : test.getNoteMaximale());
        threshold = TeacherUi.spinner(1, 100, test == null ? 70 : (int) Math.round(test.getSeuilReussite()));
        attempts = TeacherUi.spinner(1, 20, test == null ? DEFAULT_ATTEMPTS : test.getNombreTentativesAutorisees());

        title.getStyleClass().add("t-input");
        title.setPromptText("Ex. : Test final – Débutant");
        description.getStyleClass().add("t-input");
        description.setPromptText("Ce que le test vérifie");
        description.setWrapText(true);
        description.setPrefRowCount(3);

        if (test == null) {
            useLevelThreshold(level.getValue());
            level.valueProperty().addListener((obs, oldLevel, newLevel) -> useLevelThreshold(newLevel));
        } else {
            level.setDisable(true);        // le niveau d'un test ne change pas
            title.setText(test.getTitre());
            description.setText(test.getDescription() == null ? "" : test.getDescription());
        }

        build(TeacherUi.field("Niveau *", level),
                TeacherUi.field("Titre *", title),
                TeacherUi.field("Description", description),
                TeacherUi.row(TeacherUi.field("Durée (minutes)", duration),
                        TeacherUi.field("Note maximale", maxMark)),
                TeacherUi.row(TeacherUi.field("Seuil de réussite (%)", threshold),
                        TeacherUi.field("Tentatives autorisées", attempts)));
    }

    /** Propose par défaut le seuil du niveau choisi. */
    private void useLevelThreshold(Niveau selected) {
        if (selected != null) {
            threshold.getValueFactory().setValue((int) Math.round(selected.getSeuilReussite()));
        }
    }

    @Override
    protected String submit() {
        Niveau selected = level.getValue();
        if (selected == null) {
            throw new IllegalArgumentException("Choisissez un niveau.");
        }
        String name = title.getText().trim();
        String text = description.getText().trim();
        int minutes = TeacherUi.intValue(duration);
        double mark = TeacherUi.doubleValue(maxMark);
        int passMark = TeacherUi.intValue(threshold);
        int tries = TeacherUi.intValue(attempts);

        if (test == null) {
            service.definirTest(selected, name, text, minutes, mark, passMark, tries);
            return "Test « " + name + " » créé : ajoutez-y des questions.";
        }
        service.modifierTest(test, name, text, minutes, mark, passMark, tries);
        return "Test « " + name + " » modifié.";
    }
}
