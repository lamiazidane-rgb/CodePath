package com.pylo.view;

import com.pylo.navigation.NavigationService;
import com.pylo.navigation.Route;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/** Layout principal : Sidebar + Topbar + zone centrale + Footer. Construit UNE seule fois. */
public class MainLayout extends BorderPane {

    private static final double AUTO_COLLAPSE_WIDTH = 900;

    private final StackPane contentHost = new StackPane();   // SEULE zone remplacée par navigateTo()

    public MainLayout() {
        getStyleClass().add("app-root");

        Sidebar sidebar = new Sidebar();
        Topbar topbar = new Topbar(sidebar::toggle);          // bouton ☰
        Footer footer = new Footer();

        contentHost.getStyleClass().add("content-host");
        ScrollPane scroll = new ScrollPane(contentHost);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("content-scroll");

        BorderPane mainColumn = new BorderPane();
        mainColumn.getStyleClass().add("main-column");
        mainColumn.setTop(topbar);
        mainColumn.setCenter(scroll);
        mainColumn.setBottom(footer);

        setLeft(sidebar);
        setCenter(mainColumn);

        NavigationService navigation = NavigationService.getInstance();
        navigation.setContentHost(contentHost);

        // Responsive : le menu se réduit tout seul quand la fenêtre devient étroite
        widthProperty().addListener((obs, oldWidth, newWidth) -> {
            boolean wasNarrow = oldWidth.doubleValue() < AUTO_COLLAPSE_WIDTH;
            boolean isNarrow = newWidth.doubleValue() < AUTO_COLLAPSE_WIDTH;
            if (wasNarrow != isNarrow) {
                sidebar.setCollapsed(isNarrow);
            }
        });

        navigation.navigateTo(Route.DASHBOARD);
    }
}