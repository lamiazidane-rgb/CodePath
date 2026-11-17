package com.pylo.view.pages.teacher.common;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Animations de l'espace enseignant. Le CSS JavaFX ne sait pas animer (pas de transition),
 * ces effets sont donc faits en Java et s'ajoutent au style des fichiers .css.
 *
 * Usage :
 *   Motion.cascade(getChildren());      // apparition en cascade des blocs d'une page
 *   Motion.fadeIn(node, 0);             // apparition d'un seul bloc
 *   Motion.hoverLift(card);             // ombre qui s'intensifie au survol d'une carte
 */
public final class Motion {

    private static final Duration APPEAR = Duration.millis(380);
    private static final Duration HOVER = Duration.millis(180);
    private static final double STEP_MS = 70;
    private static final int MAX_STAGGERED = 8;

    private Motion() { }

    /** Fait apparaître les noeuds l'un après l'autre (fondu + léger glissement vers le haut). */
    public static void cascade(List<? extends Node> nodes) {
        List<Node> snapshot = new ArrayList<>(nodes);
        for (int i = 0; i < snapshot.size(); i++) {
            fadeIn(snapshot.get(i), Math.min(i, MAX_STAGGERED) * STEP_MS);
        }
    }

    /** Apparition en fondu avec glissement de 14 px, après un délai en millisecondes. */
    public static void fadeIn(Node node, double delayMs) {
        node.setOpacity(0);
        node.setTranslateY(14);

        FadeTransition fade = new FadeTransition(APPEAR, node);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(APPEAR, node);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition appear = new ParallelTransition(fade, slide);
        appear.setDelay(Duration.millis(delayMs));
        appear.setOnFinished(e -> {
            node.setOpacity(1);
            node.setTranslateY(0);
        });
        appear.play();
    }

    /**
     * Ombre violette qui grandit en douceur au survol. On anime l'ombre et non la position :
     * un déplacement ferait « clignoter » la carte quand la souris est sur son bord.
     */
    public static void hoverLift(Node node) {
        DropShadow shadow = new DropShadow(12, 0, 3, Color.rgb(36, 16, 79, 0.06));
        node.setEffect(shadow);

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> animate(shadow, 22, 7, Color.rgb(108, 60, 233, 0.20)));
        node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> animate(shadow, 12, 3, Color.rgb(36, 16, 79, 0.06)));
    }

    private static void animate(DropShadow shadow, double radius, double offsetY, Color color) {
        new Timeline(new KeyFrame(HOVER,
                new KeyValue(shadow.radiusProperty(), radius, Interpolator.EASE_BOTH),
                new KeyValue(shadow.offsetYProperty(), offsetY, Interpolator.EASE_BOTH),
                new KeyValue(shadow.colorProperty(), color, Interpolator.EASE_BOTH))).play();
    }
}