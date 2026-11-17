package com.pylo.view;

import com.pylo.navigation.MenuEntry;
import com.pylo.navigation.MenuProvider;
import com.pylo.navigation.NavigationService;
import com.pylo.navigation.Route;
import com.pylo.navigation.Role;
import com.pylo.domain.user.Utilisateur;
import com.pylo.service.SessionService;
import com.pylo.ui.SidebarItem;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/** Menu latéral : construit depuis MenuProvider, réagit à la route, au rôle et au bouton ☰. */
public class Sidebar extends VBox {

    private static final double EXPANDED_WIDTH = 260;
    private static final double COLLAPSED_WIDTH = 76;
    private static final double LOGO_EXPANDED_WIDTH = 120;
    private static final double LOGO_COLLAPSED_WIDTH = 40;

    private final NavigationService navigation = NavigationService.getInstance();
    private final SessionService session = SessionService.getInstance();
    private final Map<Route, SidebarItem> menuItems = new EnumMap<>(Route.class);
    private final Map<Route, SidebarItem> bottomItems = new EnumMap<>(Route.class);

    // Composants
    private final ImageView logoImage = new ImageView();
    private final Label logoFallback = new Label("PYLO");
    private final VBox identityBox = new VBox(2);
    private final Label spaceLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final VBox menuBox = new VBox(4);
    private final VBox bottomBox = new VBox(2);
    private SidebarItem logoutItem;

    // États
    private Timeline animation;
    private boolean hasLogo;
    private boolean collapsed = false;        // état demandé
    private boolean contentCollapsed = false; // état d'affichage du contenu

    public Sidebar() {
        super(6);
        getStyleClass().add("sidebar");
        setMinWidth(Region.USE_PREF_SIZE);
        setMaxWidth(Region.USE_PREF_SIZE);
        setPrefWidth(EXPANDED_WIDTH);

        VBox.setMargin(menuBox, new Insets(12, 0, 0, 0));
        VBox.setMargin(bottomBox, new Insets(10, 0, 0, 0));
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(buildLogo(), buildIdentity(), menuBox, spacer,
                bottomBox);

        buildBottomItems();
        refresh(session.getCurrentUser());

        // Menu reconstruit si l'utilisateur / le rôle change
        session.currentUserProperty().addListener((obs, oldUser, user) -> refresh(user));
        // Item actif = route courante
        navigation.currentRouteProperty().addListener((obs, oldRoute, route) -> highlight(route));
    }

    /* ---------- API publique ---------- */

    public void toggle() {
        setCollapsed(!collapsed);
    }

    public void setCollapsed(boolean value) {
        if (value == collapsed) {
            return;
        }
        collapsed = value;
        if (animation != null) {
            animation.stop();
        }
        animation = new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(prefWidthProperty(),
                        value ? COLLAPSED_WIDTH : EXPANDED_WIDTH, Interpolator.EASE_BOTH)));
        if (value) {
            applyContentState(true);                                // on masque les textes tout de suite
        } else {
            animation.setOnFinished(e -> applyContentState(false)); // on les affiche une fois ouvert
        }
        animation.play();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    /* ---------- Construction des blocs fixes ---------- */

    private Node buildLogo() {
        URL url = getClass().getResource("/images/logo_PYLO.png");
        if (url != null) {
            logoImage.setImage(new Image(url.toExternalForm()));
            logoImage.setPreserveRatio(true);
            logoImage.setSmooth(true);
            logoImage.setFitWidth(LOGO_EXPANDED_WIDTH);
            VBox.setMargin(logoImage, new Insets(0, 0, 8, 8));
            hasLogo = true;
            return logoImage;
        }
        System.err.println("Logo introuvable : /images/logo_PYLO.png");
        logoFallback.getStyleClass().add("sidebar-logo");
        return logoFallback;
    }

    private Node buildIdentity() {
        identityBox.getStyleClass().add("sidebar-identity");
        spaceLabel.getStyleClass().add("sidebar-space");
        subtitleLabel.getStyleClass().add("sidebar-subtitle");
        identityBox.getChildren().addAll(spaceLabel, subtitleLabel);
        return identityBox;
    }

    private void buildBottomItems() {
        logoutItem = new SidebarItem("Se déconnecter", "fth-log-out", Platform::exit);

        bottomBox.getChildren().add(logoutItem);
        logoutItem.setCollapsed(contentCollapsed);
    }

    /* ---------- Contenu dynamique ---------- */

    private void refresh(Utilisateur user) {
        menuBox.getChildren().clear();
        menuItems.clear();
        if (user == null) {                       // aucun utilisateur connecté : menu vide
            spaceLabel.setText("");
            subtitleLabel.setText("");
            return;
        }

        Role role = roleOf(user);
        spaceLabel.setText(role.getSpaceTitle());
        subtitleLabel.setText(role.getSubtitle());

        for (MenuEntry entry : MenuProvider.getMenu(role)) {
            SidebarItem item = new SidebarItem(entry.title(), entry.icon(),
                    () -> navigation.navigateTo(entry.route()));
            if (entry.badge() != null) {
                item.bindBadge(entry.badge());
            }
            item.setCollapsed(contentCollapsed);
            menuItems.put(entry.route(), item);
            menuBox.getChildren().add(item);
        }

        highlight(navigation.getCurrentRoute());
    }

    private Role roleOf(Utilisateur user) {
        for (Role role : Role.values()) {
            if (role.matches(user)) {
                return role;
            }
        }
        throw new IllegalStateException("Utilisateur sans rôle reconnu.");
    }

    /* ---------- États ---------- */

    private void highlight(Route route) {
        menuItems.forEach((r, item) -> item.setActive(r == route));
        bottomItems.forEach((r, item) -> item.setActive(r == route));
    }

    private void applyContentState(boolean isCollapsed) {
        contentCollapsed = isCollapsed;
        menuItems.values().forEach(item -> item.setCollapsed(isCollapsed));
        bottomItems.values().forEach(item -> item.setCollapsed(isCollapsed));
        logoutItem.setCollapsed(isCollapsed);

        identityBox.setVisible(!isCollapsed);
        identityBox.setManaged(!isCollapsed);

        if (hasLogo) {
            logoImage.setFitWidth(isCollapsed ? LOGO_COLLAPSED_WIDTH : LOGO_EXPANDED_WIDTH);
        } else {
            logoFallback.setText(isCollapsed ? "P" : "PYLO");
        }
    }
}