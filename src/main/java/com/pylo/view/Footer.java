package com.pylo.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.Year;

/** Pied de page fixe : l'année est calculée automatiquement. */
public class Footer extends HBox {

    public Footer() {
        getStyleClass().add("footer");
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(44);
        setPrefHeight(44);

        Label copyright = new Label("© " + Year.now() + " PYLO · Le Python, pas à pas.");
        copyright.getStyleClass().add("footer-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label secure = new Label("Espace personnel et sécurisé");
        secure.getStyleClass().add("footer-text");

        getChildren().addAll(copyright, spacer, secure);
    }
}