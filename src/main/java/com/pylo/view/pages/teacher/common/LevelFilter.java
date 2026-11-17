package com.pylo.view.pages.teacher.common;

import com.pylo.domain.learning.Niveau;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.Optional;

/** Liste déroulante « Tous les niveaux / N1 – … » pour filtrer les cours et les exercices (écouter valueProperty()). */
public final class LevelFilter extends ComboBox<String> {

    private final List<Niveau> levels;

    public LevelFilter(List<Niveau> levels) {
        this.levels = levels;
        getStyleClass().add("t-input");
        getItems().add("Tous les niveaux");
        levels.forEach(level -> getItems().add(TeacherUi.levelLabel(level)));
        getSelectionModel().selectFirst();
    }

    /** Niveau choisi, ou vide pour « Tous les niveaux ». */
    public Optional<Niveau> selected() {
        int index = getSelectionModel().getSelectedIndex();
        return index <= 0 ? Optional.empty() : Optional.of(levels.get(index - 1));
    }
}
