package com.pylo.app;

import com.pylo.view.MainLayout;
import com.pylo.view.pages.visitor.HomePage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    private static final String[] FONTS = {
            "/fonts/Poppins-Regular.ttf", "/fonts/Poppins-Medium.ttf",
            "/fonts/Poppins-SemiBold.ttf", "/fonts/Poppins-Bold.ttf"
    };
    private static final String[] STYLES = {
            "/css/theme.css", "/css/layout.css", "/css/components.css",
            "/css/pages/teacher.css"
    };

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        for (String font : FONTS) {
            URL url = MainApp.class.getResource(font);
            if (url != null) {
                Font.loadFont(url.toExternalForm(), 13);
            }
        }

        showHome();

        stage.setTitle("PYLO");
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        stage.show();
    }

    /** Affiche la page d'accueil marketing. */

    private void showHome() {
        HomePage home = new HomePage(
                () -> showApp(),
                () -> showApp()
        );

        Scene scene = new Scene(home, 1280, 800);
        applyStyles(scene);
        stage.setScene(scene);
        stage.show();
    }


    /** Entre dans l'espace connecté (sidebar + tableaux de bord). */
    private void showApp() {
        Scene scene = new Scene(new MainLayout(), 1280, 800);
        applyStyles(scene);
        stage.setScene(scene);
    }

    private void applyStyles(Scene scene) {
        for (String style : STYLES) {
            URL url = MainApp.class.getResource(style);
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
