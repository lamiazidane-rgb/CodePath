package com.pylo.view.pages.teacher.common;

import com.pylo.domain.assessment.Tentative;
import com.pylo.domain.learning.Niveau;
import com.pylo.domain.user.Enseignant;
import com.pylo.domain.user.Utilisateur;
import com.pylo.service.SessionService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** Briques d'interface partagées par toutes les pages de l'espace enseignant (styles : teacher.css). */
public final class TeacherUi {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] DIALOG_STYLESHEETS = {
            "/css/theme.css", "/css/components.css", "/css/pages/teacher.css"
    };

    private TeacherUi() { }

    /* ---------- Utilisateur courant ---------- */

    public static Enseignant currentTeacher() {
        Utilisateur user = SessionService.getInstance().getCurrentUser();
        if (user instanceof Enseignant teacher) {
            return teacher;
        }
        throw new IllegalStateException("L'espace enseignant nécessite un utilisateur Enseignant.");
    }

    /* ---------- Textes ---------- */

    public static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    public static String levelLabel(Niveau level) {
        return "N" + level.getOrdre() + " – " + level.getNom();
    }

    /** Nombre sans décimale inutile : 20.0 devient « 20 », 12.5 reste « 12.5 ». */
    public static String number(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    public static Label attemptChip(Tentative.Statut status) {
        return switch (status) {
            case REUSSIE -> chip("Réussie", "ok");
            case ECHOUEE -> chip("Échouée", "danger");
            case EN_COURS -> chip("En cours", "neutral");
        };
    }

    public static Label chip(String text, String tone) {
        Label chip = new Label(text);
        chip.getStyleClass().addAll("t-chip", "t-chip-" + tone);
        return chip;
    }

    /* ---------- Structure de page ---------- */

    /** En-tête : titre, sous-titre et action principale facultative (bouton à droite). */
    public static HBox header(String title, String subtitle, Node action) {
        Label subtitleLabel = label(subtitle, "card-text");
        subtitleLabel.setWrapText(true);
        VBox copy = new VBox(4, label(title, "page-title"), subtitleLabel);
        HBox.setHgrow(copy, Priority.ALWAYS);
        HBox header = new HBox(16, copy);
        if (action != null) {
            header.getChildren().add(action);
        }
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    public static VBox card(Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("card");
        return card;
    }

    public static VBox emptyState(String icon, String text) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("t-empty-icon");
        Label label = label(text, "t-hint");
        label.setWrapText(true);
        VBox box = new VBox(12, fontIcon, label);
        box.getStyleClass().add("t-empty");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /** Carte d'indicateur : icône colorée (tone : violet, blue, orange, rose), valeur, libellé et note. */
    public static VBox statCard(String icon, String tone, String label, String value, String note) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("t-stat-icon");
        HBox iconBox = new HBox(fontIcon);
        iconBox.getStyleClass().addAll("t-stat-box", "t-tone-" + tone);
        iconBox.setAlignment(Pos.CENTER);
        HBox top = new HBox(14, iconBox, new VBox(2, label(label, "t-stat-label"), label(value, "t-stat-value")));
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, top, label(note, "t-hint"));
        card.getStyleClass().addAll("card", "t-stat");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(1);
        return card;
    }

    /* ---------- Boutons ---------- */

    public static Button primaryButton(String text, String icon, Runnable action) {
        return button(text, icon, "t-btn-primary", action);
    }

    public static Button secondaryButton(String text, String icon, Runnable action) {
        return button(text, icon, "t-btn-secondary", action);
    }

    private static Button button(String text, String icon, String styleClass, Runnable action) {
        Button button = new Button(text, new FontIcon(icon));
        button.getStyleClass().add(styleClass);
        button.setOnAction(e -> action.run());
        return button;
    }

    public static Button iconButton(String icon, String tooltip, boolean danger, Runnable action) {
        Button button = new Button();
        button.setGraphic(new FontIcon(icon));
        button.getStyleClass().add(danger ? "t-icon-danger" : "t-icon-btn");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(e -> action.run());
        return button;
    }

    /** Boutons d'action d'une ligne de tableau (une action nulle est ignorée). */
    public static HBox rowActions(Runnable view, Runnable edit, Runnable delete) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        if (view != null) {
            box.getChildren().add(iconButton("fth-eye", "Consulter", false, view));
        }
        if (edit != null) {
            box.getChildren().add(iconButton("fth-edit-2", "Modifier", false, edit));
        }
        if (delete != null) {
            box.getChildren().add(iconButton("fth-trash-2", "Supprimer", true, delete));
        }
        return box;
    }

    /* ---------- Messages ---------- */

    public static Label messageLabel() {
        Label label = new Label();
        label.setVisible(false);
        label.setManaged(false);
        label.setWrapText(true);
        return label;
    }

    public static void show(Label label, String text, boolean error) {
        label.getStyleClass().removeAll("t-error", "t-success");
        label.getStyleClass().add(error ? "t-error" : "t-success");
        label.setText(text);
        boolean visible = text != null && !text.isEmpty();
        label.setVisible(visible);
        label.setManaged(visible);
    }

    /* ---------- Formulaires ---------- */

    public static VBox field(String label, Node input) {
        if (input instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        VBox box = new VBox(5, label(label, "t-label"), input);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /** Place plusieurs champs côte à côte, à largeur égale. */
    public static HBox row(Node... fields) {
        HBox row = new HBox(16, fields);
        for (Node field : fields) {
            HBox.setHgrow(field, Priority.ALWAYS);
        }
        return row;
    }

    public static Spinner<Integer> spinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        spinner.setEditable(true);
        spinner.getStyleClass().add("t-spinner");
        return spinner;
    }

    /** Valeur d'un Spinner éditable : le texte saisi est pris en compte, sinon on garde la dernière valeur. */
    public static int intValue(Spinner<Integer> spinner) {
        try {
            spinner.getValueFactory().setValue(Integer.parseInt(spinner.getEditor().getText().trim()));
        } catch (NumberFormatException ignored) {
            // valeur saisie invalide : on garde la dernière valeur valide
        }
        return spinner.getValue();
    }

    public static Spinner<Double> decimalSpinner(double min, double max, double initial) {
        Spinner<Double> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, 0.5));
        spinner.setEditable(true);
        spinner.getStyleClass().add("t-spinner");
        return spinner;
    }

    public static double doubleValue(Spinner<Double> spinner) {
        try {
            spinner.getValueFactory().setValue(Double.parseDouble(spinner.getEditor().getText().trim().replace(',', '.')));
        } catch (NumberFormatException ignored) {
            // valeur saisie invalide : on garde la dernière valeur valide
        }
        return spinner.getValue();
    }

    public static ComboBox<Niveau> levelCombo(List<Niveau> levels) {
        ComboBox<Niveau> combo = new ComboBox<>();
        combo.getStyleClass().add("t-input");
        combo.setPromptText("Niveau");
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Niveau level) {
                return level == null ? "" : levelLabel(level);
            }

            @Override
            public Niveau fromString(String text) {
                return null;
            }
        });
        combo.getItems().setAll(levels);
        if (!levels.isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
        return combo;
    }

    /* ---------- Boîtes de dialogue ---------- */

    private static void style(DialogPane pane) {
        for (String path : DIALOG_STYLESHEETS) {
            URL url = TeacherUi.class.getResource(path);
            if (url != null) {
                pane.getStylesheets().add(url.toExternalForm());
            }
        }
        pane.getStyleClass().add("t-dialog");
    }

    public static boolean confirm(Node owner, String title, String message, String confirmText) {
        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, confirm, cancel);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner.getScene() != null) {
            alert.initOwner(owner.getScene().getWindow());
        }
        style(alert.getDialogPane());
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirm;
    }

    /**
     * Dialogue de saisie. {@code submit} enregistre la saisie et renvoie un message d'erreur,
     * ou null si tout est correct (le dialogue se ferme alors).
     */
    public static boolean showForm(Node owner, String title, String confirmText,
                                   Node content, Label error, Supplier<String> submit) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        if (owner.getScene() != null) {
            dialog.initOwner(owner.getScene().getWindow());
        }
        dialog.getDialogPane().setContent(content);
        style(dialog.getDialogPane());

        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirm, new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE));
        dialog.getDialogPane().lookupButton(confirm).addEventFilter(ActionEvent.ACTION, event -> {
            String problem = submit.get();
            if (problem != null && !problem.isBlank()) {
                show(error, problem, true);
                event.consume();
            }
        });
        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == confirm;
    }
}
