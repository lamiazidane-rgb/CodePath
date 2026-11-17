package com.pylo.view.pages;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Page temporaire affichée tant qu'une rubrique n'est pas développée. */
public class PlaceholderPage extends VBox {

    public PlaceholderPage(String title) {
        super(20);
        getStyleClass().add("page");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");

        Label cardTitle = new Label("Page temporaire");
        cardTitle.getStyleClass().add("card-title");
        Label cardText = new Label("Le contenu de cette rubrique sera développé à une prochaine étape.");
        cardText.setWrapText(true);
        cardText.getStyleClass().add("card-text");

        VBox card = new VBox(8, cardTitle, cardText);
        card.getStyleClass().add("card");

        getChildren().addAll(titleLabel, card);
    }
}