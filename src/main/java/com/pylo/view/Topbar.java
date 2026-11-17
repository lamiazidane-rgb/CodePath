package com.pylo.view;

import com.pylo.domain.user.Utilisateur;
import com.pylo.navigation.NavigationService;
import com.pylo.navigation.Role;
import com.pylo.navigation.Route;
import com.pylo.service.SessionService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Topbar : bouton ☰, recherche, date/heure en direct, notifications, utilisateur. */
public class Topbar extends HBox {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy HH:mm", Locale.FRENCH);
    private static final double HIDE_DATE_BELOW = 980;

    private final SessionService session = SessionService.getInstance();

    private final HBox dateChip = new HBox(8);
    private final Label titleLabel = new Label();
    private final Label dateLabel = new Label();
    private final Circle bellDot = new Circle(4);
    private final HBox userBox = new HBox(10);
    private final Label avatarLabel = new Label();
    private final Label nameLabel = new Label();
    private final Label roleLabel = new Label();
    private final ContextMenu roleMenu = new ContextMenu();

    /** @param onMenuToggle action du bouton ☰ (ouvrir / fermer le menu) */
    public Topbar(Runnable onMenuToggle) {
        super(16);
        getStyleClass().add("topbar");
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(64);
        setPrefHeight(64);

        getChildren().addAll(
                buildMenuButton(onMenuToggle),
                buildTitle(),
                buildSearchBox(),
                grow(),
                buildDateChip(),
                grow(),
                buildBell(),
                buildUserBox());

        startClock();
        showUser(session.getCurrentUser());
        session.currentUserProperty().addListener((obs, oldUser, user) -> showUser(user));
        buildRoleMenu();

        // Responsive : on masque la date quand la fenêtre est étroite
        widthProperty().addListener((obs, oldW, newW) -> {
            boolean show = newW.doubleValue() >= HIDE_DATE_BELOW;
            dateChip.setVisible(show);
            dateChip.setManaged(show);
        });
    }

    /** Construit une topbar sans bouton de menu, pour les écrans autonomes. */
    public Topbar() {
        this(() -> {
        });
    }

    /** Met à jour le titre affiché par les écrans qui pilotent leur propre navigation. */
    public void setTitle(String title) {
        boolean hasTitle = title != null && !title.isBlank();
        titleLabel.setText(hasTitle ? title : "");
        titleLabel.setVisible(hasTitle);
        titleLabel.setManaged(hasTitle);
    }

    /* ---------- Construction ---------- */

    private Label buildTitle() {
        titleLabel.getStyleClass().add("topbar-title");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        return titleLabel;
    }

    private Button buildMenuButton(Runnable onMenuToggle) {
        Button button = new Button();
        button.getStyleClass().add("icon-button");
        button.setGraphic(new FontIcon("fth-menu"));
        button.setOnAction(e -> onMenuToggle.run());
        return button;
    }

    private HBox buildSearchBox() {
        FontIcon icon = new FontIcon("fth-search");
        icon.getStyleClass().add("search-icon");

        TextField field = new TextField();
        field.setPromptText("Rechercher un cours, un exercice...");
        field.getStyleClass().add("search-field");
        HBox.setHgrow(field, Priority.ALWAYS);

        Label shortcut = new Label("⌘ K");
        shortcut.getStyleClass().add("search-shortcut");

        HBox box = new HBox(10, icon, field, shortcut);
        box.getStyleClass().add("search-box");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(380);
        box.setMinWidth(160);
        HBox.setHgrow(box, Priority.SOMETIMES);
        return box;
    }

    private HBox buildDateChip() {
        Circle dot = new Circle(3);
        dot.getStyleClass().add("date-dot");
        dateLabel.getStyleClass().add("date-text");

        dateChip.getStyleClass().add("date-chip");
        dateChip.setAlignment(Pos.CENTER);
        dateChip.getChildren().addAll(dot, dateLabel);
        return dateChip;
    }

    private StackPane buildBell() {
        Button bell = new Button();
        bell.getStyleClass().add("icon-button");
        bell.setGraphic(new FontIcon("fth-bell"));
        bell.setOnAction(e -> NavigationService.getInstance().navigateTo(Route.NOTIFICATIONS));

        // Point jaune : visible seulement s'il y a des notifications
        bellDot.getStyleClass().add("bell-dot");
        bellDot.setMouseTransparent(true);
        bellDot.visibleProperty().bind(session.unreadNotificationsProperty().greaterThan(0));
        StackPane.setAlignment(bellDot, Pos.TOP_RIGHT);
        StackPane.setMargin(bellDot, new Insets(8, 8, 0, 0));

        return new StackPane(bell, bellDot);
    }

    private HBox buildUserBox() {
        avatarLabel.getStyleClass().add("avatar");
        nameLabel.getStyleClass().add("user-name");
        roleLabel.getStyleClass().add("user-role");

        VBox texts = new VBox(nameLabel, roleLabel);
        texts.setAlignment(Pos.CENTER_LEFT);

        FontIcon chevron = new FontIcon("fth-chevron-down");
        chevron.getStyleClass().add("chevron");

        userBox.getStyleClass().add("user-box");
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.getChildren().addAll(avatarLabel, texts, chevron);
        return userBox;
    }

    private Region grow() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    /* ---------- Comportement ---------- */

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        updateClock();
    }

    private void updateClock() {
        String text = LocalDateTime.now().format(DATE_FORMAT);
        dateLabel.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1));
    }

    private void showUser(Utilisateur user) {
        if (user == null) {                       // aucun utilisateur connecté
            avatarLabel.setText("?");
            nameLabel.setText("Non connecté");
            roleLabel.setText("");
            return;
        }
        String prenom = user.getPrenom();
        String nom = user.getNom();
        avatarLabel.setText((prenom.substring(0, 1) + nom.substring(0, 1)).toUpperCase());
        nameLabel.setText(prenom + " " + nom);
        roleLabel.setText(roleLabelOf(user));
    }

    private String roleLabelOf(Utilisateur user) {
        for (Role role : Role.values()) {
            if (role.matches(user)) {
                return role.getLabel();
            }
        }
        return "";
    }

    /** Menu de test : permet de basculer entre les 3 rôles sans écran de connexion. */
    private void buildRoleMenu() {
        for (Role role : Role.values()) {
            MenuItem item = new MenuItem("Voir en tant que : " + role.getLabel());
            item.setOnAction(e -> {
                session.switchRole(role);
                NavigationService.getInstance().navigateTo(Route.DASHBOARD);
            });
            roleMenu.getItems().add(item);
        }
        userBox.setOnMouseClicked(e -> roleMenu.show(userBox, Side.BOTTOM, 0, 6));
    }
}