package com.pylo.view.pages.teacher.test;

import com.pylo.domain.assessment.Question;
import com.pylo.domain.assessment.Tentative;
import com.pylo.domain.assessment.Test;
import com.pylo.service.CatalogueService;
import com.pylo.view.pages.teacher.common.DataTable;
import com.pylo.view.pages.teacher.common.DataTable.Column;
import com.pylo.view.pages.teacher.common.TeacherUi;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Détail d'un test : ses paramètres, ses questions (ajout, modification, suppression)
 * et les tentatives des apprenants (consultation seule, elles sont créées côté apprenant).
 */
class TestDetailView extends VBox {

    /** Types de question proposés (valeur stockée -> libellé affiché). */
    private static final Map<String, String> QUESTION_TYPES = new LinkedHashMap<>();

    static {
        QUESTION_TYPES.put("QCM", "QCM");
        QUESTION_TYPES.put("VRAI_FAUX", "Vrai / faux");
        QUESTION_TYPES.put("TEXTE", "Texte libre");
    }

    private final CatalogueService service = CatalogueService.getInstance();
    private final Test test;
    private final Label message = TeacherUi.messageLabel();
    private final Label questionsTitle = TeacherUi.label("", "card-title");
    private final DataTable questionTable = new DataTable(
            Column.fixed("N°", 50),
            Column.grow("Question"),
            Column.flex("Type", 110),
            Column.fixed("Barème", 80),
            Column.fixed("Actions", 70));

    TestDetailView(Test test, Runnable onClose) {
        super(18);
        this.test = test;

        Label parameters = TeacherUi.label(TeacherUi.number(test.getNoteMaximale()) + " pts · "
                + test.getDuree() + " min · seuil " + TeacherUi.number(test.getSeuilReussite()) + " % · "
                + test.getNombreTentativesAutorisees() + " tentative(s) autorisée(s)", "t-hint");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox questionsHead = new HBox(12, questionsTitle, spacer,
                TeacherUi.primaryButton("Ajouter une question", "fth-plus", () -> openQuestionDialog(null)));
        questionsHead.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                new HBox(TeacherUi.secondaryButton("Retour aux tests", "fth-arrow-left", onClose)),
                TeacherUi.header(test.getTitre(), levelName(), null),
                parameters,
                message,
                TeacherUi.card(questionsHead, questionTable),
                TeacherUi.card(TeacherUi.label("Tentatives des apprenants", "card-title"), attemptsTable()));
        refreshQuestions();
    }

    private String levelName() {
        return test.getNiveau() == null ? "Aucun niveau" : "Test du niveau " + TeacherUi.levelLabel(test.getNiveau());
    }

    /* ---------- Questions ---------- */

    private void refreshQuestions() {
        List<Question> questions = test.getQuestions();
        questionsTitle.setText("Questions (" + questions.size() + ")");
        int[] rank = {0};
        questionTable.setItems(questions, question -> new Node[]{
                DataTable.text(String.valueOf(++rank[0])),
                DataTable.text(question.getEnonce()),
                DataTable.text(QUESTION_TYPES.getOrDefault(question.getTypeQuestion(), String.valueOf(question.getTypeQuestion()))),
                DataTable.text(TeacherUi.number(question.getBareme()) + " pts"),
                TeacherUi.rowActions(null, () -> openQuestionDialog(question), () -> deleteQuestion(question))
        }, "Aucune question : un test doit en contenir au moins une.");
    }

    private void openQuestionDialog(Question question) {
        TextArea statement = new TextArea();
        statement.getStyleClass().add("t-input");
        statement.setPromptText("Énoncé de la question");
        statement.setWrapText(true);
        statement.setPrefRowCount(3);

        ComboBox<String> type = new ComboBox<>();
        type.getStyleClass().add("t-input");
        type.getItems().setAll(QUESTION_TYPES.keySet());
        type.setConverter(new StringConverter<>() {
            @Override
            public String toString(String key) {
                return key == null ? "" : QUESTION_TYPES.getOrDefault(key, key);
            }

            @Override
            public String fromString(String text) {
                return null;
            }
        });
        type.getSelectionModel().selectFirst();

        Spinner<Double> mark = TeacherUi.decimalSpinner(0, 100, 1);
        if (question != null) {
            statement.setText(question.getEnonce());
            type.getSelectionModel().select(question.getTypeQuestion());
            mark.getValueFactory().setValue(question.getBareme());
        }

        Label error = TeacherUi.messageLabel();
        VBox form = new VBox(14, TeacherUi.field("Énoncé *", statement),
                TeacherUi.row(TeacherUi.field("Type", type), TeacherUi.field("Barème (points)", mark)), error);
        form.setPrefWidth(460);

        boolean saved = TeacherUi.showForm(this, question == null ? "Nouvelle question" : "Modifier la question",
                "Enregistrer", form, error, () -> {
                    try {
                        String text = statement.getText().trim();
                        double points = TeacherUi.doubleValue(mark);
                        if (question == null) {
                            service.ajouterQuestion(test, text, type.getValue(), points);
                        } else {
                            service.modifierQuestion(question, text, type.getValue(), points);
                        }
                        return null;
                    } catch (IllegalArgumentException e) {
                        return e.getMessage();
                    }
                });
        if (saved) {
            TeacherUi.show(message, question == null ? "Question ajoutée." : "Question modifiée.", false);
            refreshQuestions();
        }
    }

    private void deleteQuestion(Question question) {
        if (TeacherUi.confirm(this, "Supprimer la question", "Supprimer cette question du test ?", "Supprimer")) {
            service.supprimerQuestion(question);
            TeacherUi.show(message, "Question supprimée.", false);
            refreshQuestions();
        }
    }

    /* ---------- Tentatives ---------- */

    private DataTable attemptsTable() {
        DataTable table = new DataTable(
                Column.grow("Apprenant"),
                Column.flex("Début", 140),
                Column.flex("Note", 90),
                Column.fixed("Résultat", 90));
        List<Tentative> attempts = test.getTentatives().stream()
                .sorted(Comparator.comparing(Tentative::getDateDebut).reversed())
                .toList();
        table.setItems(attempts, attempt -> new Node[]{
                DataTable.text(attempt.getApprenant().getPrenom() + " " + attempt.getApprenant().getNom()),
                DataTable.text(attempt.getDateDebut().format(TeacherUi.DATE_TIME)),
                DataTable.text(attempt.getStatut() == Tentative.Statut.EN_COURS ? "—"
                        : TeacherUi.number(attempt.getNoteObtenue()) + " / " + TeacherUi.number(test.getNoteMaximale())),
                TeacherUi.attemptChip(attempt.getStatut())
        }, "Aucun apprenant n'a encore passé ce test.");
        return table;
    }
}
