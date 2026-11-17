package com.pylo.view.pages.teacher.common;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Page de formulaire (création / modification) : retour, titre, champs dans une carte,
 * message d'erreur et boutons Annuler / Enregistrer. Les valeurs refusées par le domaine
 * (IllegalArgumentException) s'affichent dans le formulaire, qui reste ouvert.
 * Les sous-classes appellent {@link #build(Node...)} à la fin de leur constructeur.
 */
public abstract class FormView extends VBox {

    private final String title;
    private final String subtitle;
    private final Runnable onClose;
    private final Consumer<String> onSaved;
    private final Label error = TeacherUi.messageLabel();

    /**
     * @param onClose revient à la liste sans enregistrer
     * @param onSaved reçoit le message de confirmation une fois l'enregistrement réussi
     */
    protected FormView(String title, String subtitle, Runnable onClose, Consumer<String> onSaved) {
        super(18);
        this.title = title;
        this.subtitle = subtitle;
        this.onClose = onClose;
        this.onSaved = onSaved;
    }

    /** Enregistre les valeurs saisies et renvoie le message de confirmation. */
    protected abstract String submit();

    protected final void build(Node... fields) {
        VBox body = new VBox(16, fields);
        body.getChildren().add(error);
        HBox actions = new HBox(12,
                TeacherUi.secondaryButton("Annuler", "fth-x", onClose),
                TeacherUi.primaryButton("Enregistrer", "fth-check", this::save));
        actions.setAlignment(Pos.CENTER_RIGHT);
        getChildren().setAll(
                new HBox(TeacherUi.secondaryButton("Retour", "fth-arrow-left", onClose)),
                TeacherUi.header(title, subtitle, null),
                TeacherUi.card(body),
                actions);
    }

    private void save() {
        try {
            onSaved.accept(submit());
        } catch (IllegalArgumentException e) {
            TeacherUi.show(error, e.getMessage(), true);
        }
    }
}
