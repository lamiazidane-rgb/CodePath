package com.pylo.view.pages.teacher.exercice;

import com.pylo.domain.enums.Difficulte;
import com.pylo.domain.learning.Exercice;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.DataTable;
import com.pylo.view.pages.teacher.common.DataTable.Column;
import com.pylo.view.pages.teacher.common.LevelFilter;
import com.pylo.view.pages.teacher.common.TeacherPage;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/** Gestion des exercices : liste (filtrable par niveau), création, modification et suppression. */
public class ExerciceView extends TeacherPage {

    private final CatalogueService service = CatalogueService.getInstance();

    public ExerciceView() {
        showList();
    }

    @Override
    protected Node buildList(Label message) {
        List<Niveau> levels = service.findNiveaux(TeacherUi.currentTeacher());
        List<Exercice> exercises = service.findExercices(TeacherUi.currentTeacher());

        DataTable table = new DataTable(
                Column.grow("Exercice"),
                Column.flex("Niveau", 170),
                Column.fixed("Difficulté", 90),
                Column.fixed("Barème", 70),
                Column.fixed("Actions", 70));
        LevelFilter filter = new LevelFilter(levels);
        Runnable refresh = () -> table.setItems(
                exercises.stream().filter(e -> filter.selected().map(e.getNiveau()::equals).orElse(true)).toList(),
                exercise -> new Node[]{
                        DataTable.twoLines(exercise.getTitre(), statement(exercise)),
                        DataTable.text(TeacherUi.levelLabel(exercise.getNiveau())),
                        TeacherUi.chip(String.valueOf(exercise.getDifficulte()), tone(exercise.getDifficulte())),
                        DataTable.text(TeacherUi.number(exercise.getBareme()) + " pts"),
                        TeacherUi.rowActions(null, () -> openForm(exercise), () -> delete(exercise))
                },
                levels.isEmpty() ? "Aucun niveau : créez d'abord un niveau dans « Gestion des niveaux »."
                        : "Aucun exercice pour le moment : cliquez sur « Nouvel exercice ».");
        filter.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        refresh.run();

        return new VBox(18,
                TeacherUi.header("Gestion des exercices", "Les exercices s'ajoutent à un niveau et rapportent des points.",
                        TeacherUi.primaryButton("Nouvel exercice", "fth-plus", () -> openForm(null))),
                message,
                TeacherUi.card(filter, table));
    }

    private String statement(Exercice exercise) {
        String statement = exercise.getEnonce();
        return statement == null || statement.isBlank() ? "Aucun énoncé" : statement.strip().split("\\R", 2)[0];
    }

    private String tone(String difficulty) {
        if (Difficulte.FACILE.getLibelle().equals(difficulty)) {
            return "ok";
        }
        if (Difficulte.MOYEN.getLibelle().equals(difficulty)) {
            return "warn";
        }
        return Difficulte.DIFFICILE.getLibelle().equals(difficulty) ? "danger" : "neutral";
    }

    private void openForm(Exercice exercise) {
        List<Niveau> levels = service.findNiveaux(TeacherUi.currentTeacher());
        if (levels.isEmpty()) {
            showList("Créez d'abord un niveau dans « Gestion des niveaux » : un exercice appartient toujours à un niveau.", true);
            return;
        }
        showView(new ExerciceFormView(exercise, levels, this::showList, text -> showList(text, false)));
    }

    private void delete(Exercice exercise) {
        if (TeacherUi.confirm(this, "Supprimer l'exercice", "Supprimer l'exercice « " + exercise.getTitre() + " » ?", "Supprimer")) {
            service.supprimerExercice(exercise);
            showList("Exercice « " + exercise.getTitre() + " » supprimé.", false);
        }
    }
}
