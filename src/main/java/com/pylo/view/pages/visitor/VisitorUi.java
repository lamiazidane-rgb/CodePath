package com.pylo.view.pages.visitor;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;

/** Petites briques communes aux pages visiteur (accueil, connexion, inscription). Aucune image : tout est dessiné en code. */
final class VisitorUi {

    private VisitorUi() { }

    static void style(Parent root) {
        root.getStyleClass().add("vs-root");
        URL url = VisitorUi.class.getResource("/css/pages/visitor.css");
        if (url != null) {
            root.getStylesheets().add(url.toExternalForm());
        }
    }

    static Label lbl(String text, String css) {
        Label label = new Label(text);
        label.getStyleClass().add(css);
        return label;
    }

    static FontIcon ico(String name, String css) {
        FontIcon icon = new FontIcon(name);
        icon.getStyleClass().add(css);
        return icon;
    }

    static Button button(String text, String icon, String css, Runnable action) {
        Button b = new Button(text);
        if (icon != null) {
            b.setGraphic(ico(icon, "vs-btn-icon"));
            b.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        }
        b.getStyleClass().add(css);
        b.setOnAction(e -> action.run());
        return b;
    }

    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    static HBox logo(boolean light) {
        StackPane mark = new StackPane(ico("fth-code", "vs-logo-icon"));
        mark.getStyleClass().add("vs-logo-mark");
        HBox box = new HBox(10, mark, lbl("PYLO", "vs-logo-text"));
        box.setAlignment(Pos.CENTER_LEFT);
        if (light) {
            box.getStyleClass().add("vs-light");
        }
        return box;
    }

    /** Grille de {@code cols} colonnes égales. */
    static GridPane grid(int cols, double gap, Node... nodes) {
        GridPane g = new GridPane();
        g.setHgap(gap);
        g.setVgap(gap);
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            cc.setHalignment(HPos.LEFT);
            g.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < nodes.length; i++) {
            GridPane.setFillWidth(nodes[i], true);
            g.add(nodes[i], i % cols, i / cols);
        }
        return g;
    }

    /** Léger va-et-vient vertical (mis en pause quand le nœud quitte la scène). */
    static <T extends Node> T bob(T node, double amplitude, double seconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), node);
        tt.setByY(-amplitude);
        tt.setCycleCount(TranslateTransition.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        node.sceneProperty().addListener((o, was, scene) -> {
            if (scene == null) {
                tt.pause();
            } else {
                tt.play();
            }
        });
        return node;
    }

    /** Pastille ronde flottante (icône) à la position donnée d'un Pane. */
    static Node badge(String icon, double x, double y, double seconds) {
        StackPane b = new StackPane(ico(icon, "vs-badge-icon"));
        b.getStyleClass().add("vs-badge");
        b.relocate(x, y);
        return bob(b, 8, seconds);
    }

    /** Illustration « étudiant devant son bureau » (320 x 250), dessinée avec des formes. */
    static Pane desk() {
        Pane p = new Pane();
        p.setPrefSize(320, 250);
        p.setMinSize(320, 250);
        p.setMaxSize(320, 250);

        Ellipse shadow = new Ellipse(160, 240, 135, 7);
        shadow.setFill(Color.web("#000000", 0.14));

        Ellipse leaf1 = new Ellipse(22, 194, 7, 16);
        leaf1.setFill(Color.web("#2EAD6B"));
        leaf1.setRotate(-20);
        Ellipse leaf2 = new Ellipse(31, 197, 6, 13);
        leaf2.setFill(Color.web("#3CC57F"));
        leaf2.setRotate(25);
        Rectangle pot = rect(13, 208, 20, 27, 6, "#4B2E93");

        Rectangle legL = rect(52, 168, 8, 68, 3, "#4B2E93");
        Rectangle legR = rect(262, 168, 8, 68, 3, "#4B2E93");

        Line stem = new Line(187, 176, 187, 228);
        stem.setStroke(Color.web("#4B2E93"));
        stem.setStrokeWidth(5);
        Line base = new Line(160, 232, 214, 232);
        base.setStroke(Color.web("#4B2E93"));
        base.setStrokeWidth(5);
        base.setStrokeLineCap(StrokeLineCap.ROUND);
        Rectangle seat = rect(158, 166, 58, 9, 5, "#F2B544");

        Rectangle pants = rect(166, 140, 42, 30, 12, "#5B3FD0");
        Rectangle body = rect(168, 98, 38, 54, 16, "#FFFFFF");
        Line armRest = arm(174, 114, 150, 146);
        Line armUp = arm(202, 108, 228, 72);
        Circle hand = new Circle(229, 67, 6);
        hand.setFill(Color.web("#F6CBA8"));
        Circle head = new Circle(187, 80, 19);
        head.setFill(Color.web("#F6CBA8"));
        Arc hair = new Arc(187, 79, 20, 20, 0, 180);
        hair.setType(ArcType.ROUND);
        hair.setFill(Color.web("#2B1B5E"));

        Rectangle deskTop = rect(34, 160, 252, 10, 4, "#3A2A8C");

        Rectangle monitor = rect(58, 82, 102, 66, 7, "#2B1B5E");
        Rectangle stand = rect(103, 148, 12, 12, 2, "#2B1B5E");
        StackPane screen = new StackPane(ico("fth-terminal", "vs-screen-icon"));
        screen.getStyleClass().add("vs-screen");
        screen.setPrefSize(90, 54);
        screen.setMinSize(90, 54);
        screen.setMaxSize(90, 54);
        screen.relocate(64, 88);

        Rectangle laptopScreen = rect(228, 124, 46, 30, 4, "#8B6FE0");
        Rectangle laptopBase = rect(222, 154, 58, 6, 3, "#6E57D6");

        p.getChildren().addAll(shadow, leaf1, leaf2, pot, legL, legR, stem, base, seat, pants, body, armRest,
                armUp, hand, head, hair, deskTop, monitor, stand, screen, laptopScreen, laptopBase);
        return p;
    }

    private static Rectangle rect(double x, double y, double w, double h, double arc, String color) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(arc * 2);
        r.setArcHeight(arc * 2);
        r.setFill(Color.web(color));
        return r;
    }

    private static Line arm(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(Color.WHITE);
        l.setStrokeWidth(9);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }
}