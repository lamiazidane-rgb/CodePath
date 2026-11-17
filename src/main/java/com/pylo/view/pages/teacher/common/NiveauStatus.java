package com.pylo.view.pages.teacher.common;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Niveau;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;

/**
 * Complétude d'un niveau selon les multiplicités du domaine :
 * 1..* cours, 1..* exercices, 1 test contenant 1..* questions.
 */
public final class NiveauStatus {

    private NiveauStatus() { }

    public static List<String> missing(Niveau level) {
        List<String> missing = new ArrayList<>();
        if (level.getCours().isEmpty()) {
            missing.add("au moins un cours");
        }
        if (level.getExercices().isEmpty()) {
            missing.add("au moins un exercice");
        }
        Test test = level.getTest();
        if (test == null) {
            missing.add("un test");
        } else if (test.getQuestions().isEmpty()) {
            missing.add("au moins une question dans le test");
        }
        return missing;
    }

    public static boolean isComplete(Niveau level) {
        return level.estValide() && missing(level).isEmpty();
    }

    /** Pastille « Complet » / « Incomplet » (l'info-bulle liste ce qu'il manque). */
    public static Label chip(Niveau level) {
        if (isComplete(level)) {
            return TeacherUi.chip("Complet", "ok");
        }
        Label chip = TeacherUi.chip("Incomplet", "warn");
        chip.setTooltip(new Tooltip("Il manque : " + String.join(", ", missing(level))));
        return chip;
    }
}
