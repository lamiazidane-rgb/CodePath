package com.pylo.view.pages.teacher.niveau;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.DataTable;
import com.pylo.view.pages.teacher.common.DataTable.Column;
import com.pylo.view.pages.teacher.common.NiveauStatus;
import com.pylo.view.pages.teacher.common.TeacherPage;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/** Gestion des niveaux : liste du parcours, création, modification et suppression. */
public class NiveauView extends TeacherPage {

    private final CatalogueService service = CatalogueService.getInstance();

    public NiveauView() {
        showList();
    }

    @Override
    protected Node buildList(Label message) {
        List<Niveau> levels = service.findNiveaux(TeacherUi.currentTeacher());

        DataTable table = new DataTable(
                Column.grow("Niveau"),
                Column.flex("Période", 140),
                Column.flex("Validation", 140),
                Column.flex("Contenu", 180),
                Column.fixed("Statut", 84),
                Column.fixed("Actions", 70));
        table.setItems(levels, level -> new Node[]{
                DataTable.twoLines("N" + level.getOrdre() + " · " + level.getNom(), summary(level)),
                DataTable.twoLines(level.getDateDebut().format(TeacherUi.DATE), "→ " + level.getDateFin().format(TeacherUi.DATE)),
                DataTable.twoLines(Math.round(level.getSeuilReussite()) + " % · " + level.getDuree() + " j", unlocks(level, levels)),
                DataTable.twoLines(level.getCours().size() + " cours · " + level.getExercices().size() + " exercice(s)", testSummary(level)),
                NiveauStatus.chip(level),
                TeacherUi.rowActions(null, () -> openForm(level), () -> delete(level))
        }, "Aucun niveau pour le moment : cliquez sur « Nouveau niveau ».");

        return new VBox(18,
                TeacherUi.header("Gestion des niveaux",
                        "Organisez le parcours : un niveau regroupe des cours, des exercices et un test.",
                        TeacherUi.primaryButton("Nouveau niveau", "fth-plus", () -> openForm(null))),
                message,
                TeacherUi.card(table));
    }

    private String summary(Niveau level) {
        String description = level.getDescription();
        return description == null || description.isBlank() ? "Aucune description" : description.strip().split("\\R", 2)[0];
    }

    /** Niveau débloqué par la validation de celui-ci (règle du domaine : debloquerNiveauSuivant). */
    private String unlocks(Niveau level, List<Niveau> all) {
        return level.debloquerNiveauSuivant(all).map(n -> "Ouvre : " + n.getNom()).orElse("Aucun niveau suivant");
    }

    private String testSummary(Niveau level) {
        Test test = level.getTest();
        return test == null ? "Aucun test" : "Test · " + test.getQuestions().size() + " question(s)";
    }

    private void openForm(Niveau level) {
        int nextOrder = service.findNiveaux(TeacherUi.currentTeacher()).size() + 1;
        showView(new NiveauFormView(level, nextOrder, this::showList, text -> showList(text, false)));
    }

    private void delete(Niveau level) {
        Test test = level.getTest();
        if (test != null && !test.getTentatives().isEmpty()) {
            showList("« " + level.getNom() + " » a déjà été tenté par des apprenants : il ne peut pas être supprimé.", true);
            return;
        }
        String content = level.getCours().size() + " cours, " + level.getExercices().size()
                + " exercice(s) et le test de ce niveau seront aussi supprimés.";
        if (TeacherUi.confirm(this, "Supprimer le niveau", "Supprimer « " + level.getNom() + " » ? " + content, "Supprimer")) {
            service.supprimerNiveau(level);
            showList("Niveau « " + level.getNom() + " » supprimé.", false);
        }
    }
}
