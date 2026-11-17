package com.pylo.view.pages.teacher.test;

import com.pylo.domain.assessment.Test;
import com.pylo.domain.learning.Niveau;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.DataTable;
import com.pylo.view.pages.teacher.common.DataTable.Column;
import com.pylo.view.pages.teacher.common.TeacherPage;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Gestion des tests : un test par niveau (création, modification, détail avec questions et tentatives).
 * Un niveau possède exactement un test dans le domaine : il n'y a donc pas de suppression isolée.
 */
public class TestView extends TeacherPage {

    private final CatalogueService service = CatalogueService.getInstance();

    public TestView() {
        showList();
    }

    @Override
    protected Node buildList(Label message) {
        List<Test> tests = service.findTests(TeacherUi.currentTeacher());

        DataTable table = new DataTable(
                Column.grow("Test"),
                Column.flex("Paramètres", 190),
                Column.flex("Contenu", 140),
                Column.fixed("Actions", 70));
        table.setItems(tests, test -> new Node[]{
                DataTable.twoLines(test.getTitre(), TeacherUi.levelLabel(test.getNiveau())),
                DataTable.twoLines(test.getDuree() + " min · sur " + TeacherUi.number(test.getNoteMaximale()),
                        "Seuil " + TeacherUi.number(test.getSeuilReussite()) + " % · "
                                + test.getNombreTentativesAutorisees() + " tentative(s)"),
                DataTable.twoLines(test.getQuestions().size() + " question(s)", test.getTentatives().size() + " tentative(s)"),
                TeacherUi.rowActions(() -> showView(new TestDetailView(test, this::showList)), () -> openForm(test), null)
        }, "Aucun test pour le moment : cliquez sur « Nouveau test ».");

        return new VBox(18,
                TeacherUi.header("Gestion des tests",
                        "Chaque niveau se valide par un test : paramétrez-le puis ajoutez ses questions.",
                        TeacherUi.primaryButton("Nouveau test", "fth-plus", () -> openForm(null))),
                message,
                TeacherUi.card(table));
    }

    private void openForm(Test test) {
        List<Niveau> levels = test == null
                ? service.findNiveaux(TeacherUi.currentTeacher()).stream().filter(n -> n.getTest() == null).toList()
                : List.of(test.getNiveau());
        if (levels.isEmpty()) {
            showList(service.findNiveaux(TeacherUi.currentTeacher()).isEmpty()
                    ? "Créez d'abord un niveau dans « Gestion des niveaux »."
                    : "Tous vos niveaux ont déjà un test : un niveau n'en possède qu'un seul.", true);
            return;
        }
        showView(new TestFormView(test, levels, this::showList, text -> showList(text, false)));
    }
}
