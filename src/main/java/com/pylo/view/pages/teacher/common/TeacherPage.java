package com.pylo.view.pages.teacher.common;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Base des pages de gestion de l'espace enseignant : affiche tour à tour la liste et un formulaire
 * (création / modification) à la place de la liste, sans changer de route.
 * Les sous-classes appellent {@link #showList()} à la fin de leur constructeur.
 */
public abstract class TeacherPage extends VBox {

    protected TeacherPage() {
        super(18);
        getStyleClass().addAll("page", "t-page");
    }

    /** Construit la liste ; {@code message} est la zone de retour (succès / erreur) à placer dans la page. */
    protected abstract Node buildList(Label message);

    protected final void showList() {
        showList(null, false);
    }

    /** Reconstruit la liste (données à jour) puis affiche un message de retour. */
    protected final void showList(String text, boolean error) {
        Label message = TeacherUi.messageLabel();
        getChildren().setAll(buildList(message));
        if (text != null) {
            TeacherUi.show(message, text, error);
        }
    }

    /** Remplace la liste par une autre vue (formulaire, détail). */
    protected final void showView(Node view) {
        getChildren().setAll(view);
    }
}
