package com.pylo.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

/** Un item du menu : icône + texte + badge. Gère l'état actif et l'état réduit. */
public class SidebarItem extends HBox {

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);
    private final IntegerProperty count = new SimpleIntegerProperty(0);
    private final Tooltip tooltip;

    public SidebarItem(String title, String iconLiteral, Runnable action) {
        getStyleClass().add("sidebar-item");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setFocusTraversable(true);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("sidebar-item-icon");

        Label text = new Label(title);
        text.getStyleClass().add("sidebar-item-text");
        text.visibleProperty().bind(collapsed.not());
        text.managedProperty().bind(text.visibleProperty());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.managedProperty().bind(collapsed.not());

        Label badge = new Label();
        badge.getStyleClass().add("sidebar-badge");
        badge.textProperty().bind(count.asString());
        badge.visibleProperty().bind(count.greaterThan(0).and(collapsed.not()));
        badge.managedProperty().bind(badge.visibleProperty());

        getChildren().addAll(icon, text, spacer, badge);

        tooltip = new Tooltip(title);
        collapsed.addListener((obs, wasCollapsed, isCollapsed) -> {
            setAlignment(isCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            if (isCollapsed) {
                Tooltip.install(this, tooltip);
            } else {
                Tooltip.uninstall(this, tooltip);
            }
        });

        setOnMouseClicked(e -> action.run());
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                action.run();
            }
        });
    }

    public void setActive(boolean active) {
        pseudoClassStateChanged(ACTIVE, active);
    }

    public void setCollapsed(boolean value) {
        collapsed.set(value);
    }

    /** Lie la pastille à une propriété observable : elle se met à jour toute seule. */
    public void bindBadge(IntegerProperty property) {
        count.bind(property);
    }
}