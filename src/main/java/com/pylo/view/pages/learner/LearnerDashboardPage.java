package com.pylo.view.pages.learner;

import com.pylo.navigation.NavigationService;
import com.pylo.navigation.Route;
import com.pylo.service.ActivityItem;
import com.pylo.service.ActivityType;
import com.pylo.service.Badge;
import com.pylo.service.DashboardState;
import com.pylo.service.LevelProgress;
import com.pylo.service.LevelStatus;
import com.pylo.service.Period;
import com.pylo.service.PeriodStats;
import com.pylo.service.SessionService;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;

/** Tableau de bord apprenant : la vue se met à jour automatiquement depuis DashboardState. */
public class LearnerDashboardPage extends VBox {

    private static final int MAX_ACTIVITIES = 3;
    private static final int MAX_BADGES = 4;

    private final DashboardState state = DashboardState.getInstance();
    private final NavigationService navigation = NavigationService.getInstance();

    // ----- Composants modifiés dynamiquement (déclarés AVANT les écouteurs) -----
    private final Text heroName = new Text();
    private final Label heroMessage = new Label();
    private final ImageView owlImage = new ImageView();

    private final Button periodButton = new Button();
    private final Arc progressArc = new Arc(60, 60, 50, 50, 90, 0);
    private final Label percentLabel = new Label();
    private final Label progressLevelName = new Label();
    private final Label coursesLabel = new Label();
    private final Label exercisesLabel = new Label();

    private final HBox levelsBox = new HBox(16);
    private final VBox activitiesBox = new VBox(14);
    private final HBox badgesBox = new HBox(18);
    private final Label badgesSubtitle = new Label();

    // Valeur affichée par l'anneau et le pourcentage (animée)
    private final DoubleProperty shownPercent = new SimpleDoubleProperty(0);
    private Timeline ringAnimation;
    private boolean firstRender = true;

    // Écouteurs conservés pour pouvoir les retirer quand on quitte la page
    private final ListChangeListener<LevelProgress> levelsListener = c -> onLevelsChanged();
    private final ListChangeListener<ActivityItem> activitiesListener = c -> renderActivities();
    private final ListChangeListener<Badge> badgesListener = c -> renderBadges();
    private final ChangeListener<PeriodStats> statsListener = (obs, oldStats, newStats) -> showStats(newStats);
    private final ChangeListener<Period> periodListener =
            (obs, oldPeriod, newPeriod) -> periodButton.setText(newPeriod.getLabel());

    public LearnerDashboardPage() {
        super(24);
        getStyleClass().add("page");
        URL stylesheet = getClass().getResource("/css/pages/learner-dashboard.css");
        if (stylesheet == null) {
            throw new IllegalStateException("Feuille de style introuvable : /css/pages/learner-dashboard.css");
        }
        getStylesheets().add(stylesheet.toExternalForm());

        getChildren().addAll(buildTopRow(), buildLevelsSection(), buildBottomRow());

        initData();
    }

    /* =====================================================================
     *  CONSTRUCTION DE L'INTERFACE
     * ===================================================================== */

    private HBox buildTopRow() {
        HBox row = new HBox(16, buildHero(), buildProgressCard());
        return row;
    }

    /** Bannière de bienvenue. */
    private StackPane buildHero() {
        Label kicker = new Label("UNE LIGNE DE PLUS, UN PAS DE PLUS");
        kicker.getStyleClass().add("hero-kicker");

        Text welcome = new Text("Bienvenue sur PYLO, ");
        welcome.getStyleClass().add("hero-title");
        heroName.getStyleClass().add("hero-title-accent");
        Text bang = new Text(" !");
        bang.getStyleClass().add("hero-title");
        TextFlow title = new TextFlow(welcome, heroName, bang);

        heroMessage.setWrapText(true);
        heroMessage.getStyleClass().add("hero-text");

        Button resume = new Button("Reprendre mon cours");
        resume.getStyleClass().add("accent-button");
        resume.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        resume.setGraphicTextGap(10);
        resume.setGraphic(new FontIcon("fth-arrow-right"));
        resume.setOnAction(e -> navigation.navigateTo(Route.MY_COURSES));

        VBox texts = new VBox(10, kicker, title, heroMessage, resume);
        texts.setAlignment(Pos.CENTER_LEFT);
        texts.setMaxWidth(360);
        texts.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(texts, Pos.CENTER_LEFT);

        // Illustration optionnelle : resources/images/hibou.png
        owlImage.setFitHeight(200);
        owlImage.setPreserveRatio(true);
        owlImage.setSmooth(true);
        owlImage.setMouseTransparent(true);
        StackPane.setAlignment(owlImage, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(owlImage, new Insets(0, 28, 0, 0));

        StackPane hero = new StackPane(texts, owlImage);
        hero.getStyleClass().add("hero-card");
        hero.setMinHeight(180);
        hero.setPrefHeight(190);
        HBox.setHgrow(hero, Priority.ALWAYS);
        return hero;
    }

    /** Carte « Votre progression » : période, anneau, compteurs. */
    private VBox buildProgressCard() {
        Label heading = new Label("Votre progression");
        heading.getStyleClass().add("card-heading");

        periodButton.getStyleClass().add("accent-button");
        periodButton.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        periodButton.setGraphicTextGap(6);
        periodButton.setGraphic(new FontIcon("fth-chevron-down"));

        HBox header = new HBox(heading, spacer(), periodButton);
        header.setAlignment(Pos.CENTER_LEFT);

        // Anneau : cercle de fond + arc
        Circle track = new Circle(60, 60, 50);
        track.getStyleClass().add("ring-track");
        progressArc.setType(ArcType.OPEN);
        progressArc.getStyleClass().add("ring-arc");

        Pane ring = new Pane(track, progressArc);
        fixSize(ring, 120);

        percentLabel.getStyleClass().add("ring-percent");
        Label caption = new Label("du niveau actuel");
        caption.getStyleClass().add("ring-caption");
        VBox center = new VBox(percentLabel, caption);
        center.setAlignment(Pos.CENTER);
        center.setMouseTransparent(true);

        StackPane ringStack = new StackPane(ring, center);
        fixSize(ringStack, 120);

        Label status = new Label("En cours");
        status.getStyleClass().addAll("status-badge", "status-in-progress");
        progressLevelName.getStyleClass().add("progress-level");
        coursesLabel.getStyleClass().add("stat-courses");
        exercisesLabel.getStyleClass().add("stat-exercises");
        VBox info = new VBox(5, status, progressLevelName, coursesLabel, exercisesLabel);
        info.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(16, ringStack, info);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, header, row);
        card.getStyleClass().add("card");
        card.setMinWidth(270);
        card.setPrefWidth(300);
        card.setMaxWidth(330);
        return card;
    }

    /** Section « Mes cours par niveau » (les cartes sont générées dynamiquement). */
    private VBox buildLevelsSection() {
        Label title = new Label("Mes cours par niveau");
        title.getStyleClass().add("section-title");
        HBox header = new HBox(title, spacer(),
                link("Tout mon parcours →", () -> navigation.navigateTo(Route.MY_PROGRESSION)));
        header.setAlignment(Pos.CENTER_LEFT);
        return new VBox(14, header, levelsBox);
    }

    /** Activité récente + réussites. */
    private HBox buildBottomRow() {
        // Activité récente
        Label activityTitle = new Label("Votre activité récente");
        activityTitle.getStyleClass().add("card-heading");
        Label activitySubtitle = new Label("Chaque étape compte.");
        activitySubtitle.getStyleClass().add("card-text");
        VBox.setMargin(activitiesBox, new Insets(10, 0, 0, 0));

        VBox activityCard = new VBox(6, activityTitle, activitySubtitle, activitiesBox);
        activityCard.getStyleClass().add("card");
        activityCard.setPrefWidth(520);
        HBox.setHgrow(activityCard, Priority.ALWAYS);

        // Réussites
        Label badgesTitle = new Label("Vos réussites");
        badgesTitle.getStyleClass().add("card-heading");
        badgesSubtitle.getStyleClass().add("card-text");
        badgesSubtitle.setWrapText(true);
        badgesBox.setAlignment(Pos.CENTER);
        VBox.setMargin(badgesBox, new Insets(12, 0, 12, 0));

        VBox badgesCard = new VBox(6, badgesTitle, badgesSubtitle, badgesBox,
                link("Découvrir mes objectifs →", () -> navigation.navigateTo(Route.MY_BADGES)));
        badgesCard.getStyleClass().add("card");
        badgesCard.setMinWidth(300);
        badgesCard.setPrefWidth(340);
        badgesCard.setMaxWidth(400);

        return new HBox(16, activityCard, badgesCard);
    }

    /* =====================================================================
     *  DONNÉES ET DYNAMISME
     * ===================================================================== */

    private void initData() {
        if (SessionService.getInstance().getCurrentUser() == null) {
            throw new IllegalStateException("Aucun utilisateur connecté pour le dashboard apprenant.");
        }
        heroName.setText(SessionService.getInstance().getCurrentUser().getPrenom());
        heroMessage.textProperty().bind(Bindings.concat(
                "Vos idées prennent forme en Python. Reprenez ",
                state.currentCourseProperty(),
                " et continuez à progresser."));
        loadOwl();

        // Anneau : pourcentage et arc liés à la même valeur animée
        percentLabel.textProperty().bind(Bindings.format("%.0f %%", shownPercent));
        progressArc.lengthProperty().bind(shownPercent.multiply(3.6));

        setupPeriodMenu();
        periodButton.setText(state.periodProperty().get().getLabel());
        showStats(state.statsProperty().get());

        onLevelsChanged();
        renderActivities();
        renderBadges();

        state.getLevels().addListener(levelsListener);
        state.getActivities().addListener(activitiesListener);
        state.getBadges().addListener(badgesListener);
        state.statsProperty().addListener(statsListener);
        state.periodProperty().addListener(periodListener);

        // Quand la page est retirée de la scène : on nettoie les écouteurs
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                dispose();
            }
        });
    }

    private void dispose() {
        state.getLevels().removeListener(levelsListener);
        state.getActivities().removeListener(activitiesListener);
        state.getBadges().removeListener(badgesListener);
        state.statsProperty().removeListener(statsListener);
        state.periodProperty().removeListener(periodListener);
        heroMessage.textProperty().unbind();
        percentLabel.textProperty().unbind();
        progressArc.lengthProperty().unbind();
        if (ringAnimation != null) {
            ringAnimation.stop();
        }
    }

    private void setupPeriodMenu() {
        ContextMenu menu = new ContextMenu();
        ToggleGroup group = new ToggleGroup();
        for (Period p : Period.values()) {
            RadioMenuItem item = new RadioMenuItem(p.getLabel());
            item.setToggleGroup(group);
            item.setSelected(p == state.periodProperty().get());
            item.setOnAction(e -> state.setPeriod(p));
            menu.getItems().add(item);
        }
        periodButton.setOnAction(e -> menu.show(periodButton, Side.BOTTOM, 0, 4));
    }

    private void onLevelsChanged() {
        progressLevelName.setText(state.currentLevelName());
        animateRing(state.currentLevelPercent());
        renderLevels();
    }

    private void showStats(PeriodStats s) {
        coursesLabel.setText(s.coursesDone() + "/" + s.coursesTotal() + " cours terminés");
        exercisesLabel.setText(s.exercisesDone() + "/" + s.exercisesTotal() + " exercices réussis");
    }

    private void animateRing(double target) {
        if (ringAnimation != null) {
            ringAnimation.stop();
        }
        ringAnimation = new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(shownPercent, target, Interpolator.EASE_OUT)));
        ringAnimation.play();
    }

    private void animateBar(ProgressBar bar, double target) {
        new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(bar.progressProperty(), target, Interpolator.EASE_OUT))).play();
    }

    private void renderLevels() {
        boolean animate = firstRender;   // les barres ne s'animent qu'à l'ouverture de la page
        firstRender = false;
        levelsBox.getChildren().clear();
        state.getLevels().forEach(level -> levelsBox.getChildren().add(buildLevelCard(level, animate)));
    }

    private void renderActivities() {
        activitiesBox.getChildren().clear();
        List<ActivityItem> items = state.getActivities();
        if (items.isEmpty()) {
            Label empty = new Label("Aucune activité pour le moment.");
            empty.getStyleClass().add("card-text");
            activitiesBox.getChildren().add(empty);
            return;
        }
        items.stream().limit(MAX_ACTIVITIES)
                .forEach(item -> activitiesBox.getChildren().add(buildActivityRow(item)));
    }

    private void renderBadges() {
        List<Badge> all = state.getBadges();
        int count = all.size();
        badgesSubtitle.setText(count == 0
                ? "Aucun badge pour le moment · la suite vous attend."
                : count + (count == 1 ? " badge obtenu" : " badges obtenus") + " · la suite vous attend.");

        badgesBox.getChildren().clear();
        all.stream().skip(Math.max(0, count - MAX_BADGES))
                .forEach(badge -> badgesBox.getChildren().add(buildBadge(badge)));
    }

    private void loadOwl() {
        URL url = getClass().getResource("/images/hibou.png");   // optionnel
        if (url != null) {
            owlImage.setImage(new Image(url.toExternalForm()));
        }
    }

    /* =====================================================================
     *  COMPOSANTS RÉUTILISABLES
     * ===================================================================== */

    private VBox buildLevelCard(LevelProgress level, boolean animate) {
        boolean locked = level.status() == LevelStatus.LOCKED;

        VBox card = new VBox(8);
        card.getStyleClass().add("level-card");
        if (level.status() == LevelStatus.IN_PROGRESS) {
            card.getStyleClass().add("level-card-current");
        }
        if (locked) {
            card.getStyleClass().add("level-card-locked");
        }
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(150);
        card.setPrefWidth(200);
        card.setMaxWidth(Double.MAX_VALUE);

        FontIcon icon = new FontIcon(level.icon());
        icon.getStyleClass().add("level-icon");
        Label status = new Label(level.status().getLabel());
        status.getStyleClass().addAll("status-badge",
                "status-" + level.status().name().toLowerCase().replace('_', '-'));
        HBox top = new HBox(icon, spacer(), status);
        top.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(level.name());
        name.getStyleClass().add("level-name");
        Label subtitle = new Label(level.subtitle());
        subtitle.getStyleClass().add("level-subtitle");
        subtitle.setWrapText(true);

        card.getChildren().addAll(top, name, subtitle);

        if (!locked) {
            Label meta = new Label("Progression du niveau");
            meta.getStyleClass().add("level-meta");
            Label percent = new Label(level.percent() + " %");
            percent.getStyleClass().add("level-percent");
            HBox progressRow = new HBox(meta, spacer(), percent);
            progressRow.setAlignment(Pos.CENTER_LEFT);

            double value = level.percent() / 100.0;
            ProgressBar bar = new ProgressBar(animate ? 0 : value);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.getStyleClass().add("level-bar");
            if (animate) {
                animateBar(bar, value);
            }

            Label link = new Label(level.status() == LevelStatus.VALIDATED
                    ? "Revoir les cours →" : "Continuer →");
            link.getStyleClass().add("link");

            card.getChildren().addAll(progressRow, bar, verticalFill(), link);

            // Carte cliquable
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> navigation.navigateTo(Route.MY_COURSES));
        } else {
            Label requirement = new Label("Validation du niveau précédent requise");
            requirement.getStyleClass().add("level-meta");
            requirement.setWrapText(true);
            requirement.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(requirement, Priority.ALWAYS);
            Label dash = new Label("–");
            dash.getStyleClass().add("level-percent");
            HBox requirementRow = new HBox(8, requirement, dash);
            requirementRow.setAlignment(Pos.CENTER_LEFT);

            Label mandatory = new Label("Cours, exercices et test obligatoires");
            mandatory.getStyleClass().add("level-meta");
            mandatory.setWrapText(true);

            Label lockedText = new Label("Niveau verrouillé");
            lockedText.getStyleClass().add("level-locked-text");

            card.getChildren().addAll(requirementRow, mandatory, verticalFill(), lockedText);
            Tooltip.install(card, new Tooltip("Validez d'abord le niveau précédent."));
        }
        return card;
    }

    private HBox buildActivityRow(ActivityItem item) {
        Label text = new Label(item.text());
        text.getStyleClass().add("activity-text");
        Label date = new Label(item.date());
        date.getStyleClass().add("activity-date");

        FontIcon check = new FontIcon("fth-check");
        check.getStyleClass().add("check-icon");

        HBox row = new HBox(12, iconBox(item.type().getIcon(), "icon-box"),
                new VBox(2, text, date), spacer(), check);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox buildBadge(Badge badge) {
        Label name = new Label(badge.name());
        name.getStyleClass().add("badge-name");
        VBox box = new VBox(8, iconBox(badge.icon(), "badge-box"), name);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private StackPane iconBox(String literal, String styleClass) {
        FontIcon icon = new FontIcon(literal);
        icon.getStyleClass().add("dash-icon");
        StackPane box = new StackPane(icon);
        box.getStyleClass().add(styleClass);
        return box;
    }

    private Label link(String text, Runnable action) {
        Label label = new Label(text);
        label.getStyleClass().add("link");
        label.setOnMouseClicked(e -> action.run());
        return label;
    }

    private void fixSize(Region region, double size) {
        region.setMinSize(size, size);
        region.setPrefSize(size, size);
        region.setMaxSize(size, size);
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Region verticalFill() {
        Region fill = new Region();
        VBox.setVgrow(fill, Priority.ALWAYS);
        return fill;
    }
}