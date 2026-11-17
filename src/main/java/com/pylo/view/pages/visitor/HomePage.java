package com.pylo.view.pages.visitor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static com.pylo.view.pages.visitor.VisitorUi.button;
import static com.pylo.view.pages.visitor.VisitorUi.grid;
import static com.pylo.view.pages.visitor.VisitorUi.ico;
import static com.pylo.view.pages.visitor.VisitorUi.lbl;
import static com.pylo.view.pages.visitor.VisitorUi.spacer;

/**
 * Page d'accueil visiteur : barre de navigation, héros, atouts, parcours, thèmes, étapes, appel à l'action, pied de page.
 * Les contenus (niveaux, thèmes) sont des exemples : à remplacer par ceux du CourseService si on veut les afficher en vrai.
 */
public final class HomePage extends BorderPane {

    private final ScrollPane scroll = new ScrollPane();
    private final VBox page = new VBox();
    private final Runnable onLogin;
    private final Runnable onRegister;

    public HomePage(Runnable onLogin, Runnable onRegister) {
        this.onLogin = onLogin;
        this.onRegister = onRegister;
        VisitorUi.style(this);

        Node features = section(features());
        Node levels = section(levels());
        Node themes = section(themes());
        Node steps = section(steps());

        page.getStyleClass().add("vs-home");
        page.setAlignment(Pos.TOP_CENTER);
        page.getChildren().addAll(section(hero()), features, levels, themes, steps, section(cta()), footer());

        scroll.setContent(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("vs-scroll");
        setTop(nav(features, levels, themes, steps));
        setCenter(scroll);
    }

    private void goTo(Node target) {
        double extra = page.getHeight() - scroll.getViewportBounds().getHeight();
        if (extra > 0) {
            scroll.setVvalue(Math.max(0, Math.min(1, target.getBoundsInParent().getMinY() / extra)));
        }
    }

    private static VBox section(Node content) {
        VBox box = new VBox(content);
        box.setMaxWidth(1180);
        box.getStyleClass().add("vs-section");
        return box;
    }

    private static HBox head(String title, String linkText, Runnable action) {
        HBox row = new HBox(lbl(title, "vs-h2"), spacer());
        row.setAlignment(Pos.CENTER_LEFT);
        if (linkText != null) {
            Button link = new Button(linkText);
            link.getStyleClass().add("vs-link-strong");
            link.setOnAction(e -> action.run());
            row.getChildren().add(link);
        }
        return row;
    }

    // ---------------------------------------------------------------- navigation

    private Node nav(Node features, Node levels, Node themes, Node steps) {
        HBox bar = new HBox(26, VisitorUi.logo(false));
        bar.getStyleClass().add("vs-nav");
        bar.setAlignment(Pos.CENTER_LEFT);
        String[] names = {"Fonctionnalités", "Parcours", "Thèmes", "Comment ça marche"};
        Node[] targets = {features, levels, themes, steps};
        HBox links = new HBox(22);
        links.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < names.length; i++) {
            Button b = new Button(names[i]);
            b.getStyleClass().add("vs-nav-link");
            Node t = targets[i];
            b.setOnAction(e -> goTo(t));
            links.getChildren().add(b);
        }
        bar.getChildren().addAll(links, spacer(),
                button("Se connecter", null, "vs-secondary", onLogin),
                button("S'inscrire", null, "vs-primary", onRegister));
        return bar;
    }

    // ---------------------------------------------------------------- héros

    private Node hero() {
        Label pill = lbl("Nouveau · Parcours Python guidé", "vs-pill");
        pill.setGraphic(ico("fth-zap", "vs-pill-icon"));

        Label lead = lbl("Suis des niveaux structurés : des cours clairs, des exercices pour pratiquer et un test "
                + "pour valider chaque étape.", "vs-lead");
        lead.setWrapText(true);
        lead.setMaxWidth(440);

        HBox actions = new HBox(12,
                button("Commencer gratuitement", "fth-arrow-right", "vs-primary", onRegister),
                button("J'ai déjà un compte", null, "vs-secondary", onLogin));
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox perks = new HBox(20, perk("fth-book-open", "Cours"), perk("fth-terminal", "Exercices"),
                perk("fth-award", "Tests de validation"));
        perks.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(14, pill, lbl("Apprends.", "vs-h1"), lbl("Pratique.", "vs-h1"),
                lbl("Réussis.", "vs-h1-accent"), lead, actions, perks);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPadding(new Insets(6, 0, 6, 0));

        GridPane g = new GridPane();
        g.setHgap(20);
        ColumnConstraints a = new ColumnConstraints();
        a.setPercentWidth(52);
        ColumnConstraints b = new ColumnConstraints();
        b.setPercentWidth(48);
        g.getColumnConstraints().addAll(a, b);
        g.add(left, 0, 0);
        g.add(heroArt(), 1, 0);
        GridPane.setHalignment(g.getChildren().get(1), javafx.geometry.HPos.CENTER);
        return g;
    }

    private static Node perk(String icon, String text) {
        HBox box = new HBox(6, ico(icon, "vs-perk-icon"), lbl(text, "vs-perk"));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node heroArt() {
        Pane art = new Pane();
        art.setPrefSize(440, 350);
        art.setMinSize(440, 350);
        art.setMaxSize(440, 350);

        Region panel = new Region();
        panel.getStyleClass().add("vs-hero-panel");
        panel.setPrefSize(340, 330);
        panel.relocate(50, 10);

        Node desk = VisitorUi.desk();
        desk.relocate(60, 62);

        ProgressBar bar = new ProgressBar(0.6);
        bar.getStyleClass().add("vs-progress");
        bar.setPrefWidth(120);
        VBox levelText = new VBox(4, lbl("Niveau 1 · Les bases", "vs-float-title"), bar);
        HBox levelCard = floatCard("fth-layers", levelText);
        levelCard.relocate(236, 0);

        VBox doneText = new VBox(2, lbl("Exercice : boucle for", "vs-float-title"), lbl("Validé", "vs-float-ok"));
        HBox doneCard = floatCard("fth-check-circle", doneText);
        doneCard.relocate(0, 262);

        art.getChildren().addAll(panel, desk, VisitorUi.bob(levelCard, 7, 3.0), VisitorUi.bob(doneCard, 7, 2.6));
        return art;
    }

    private static HBox floatCard(String icon, Node text) {
        StackPane tile = new StackPane(ico(icon, "vs-float-icon"));
        tile.getStyleClass().add("vs-float-tile");
        HBox card = new HBox(10, tile, text);
        card.getStyleClass().add("vs-float");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    // ---------------------------------------------------------------- atouts

    private Node features() {
        String[][] f = {
                {"fth-book-open", "Cours structurés", "Des leçons claires, dans l'ordre"},
                {"fth-terminal", "Exercices pratiques", "Pratique le code pas à pas"},
                {"fth-award", "Tests de validation", "Valide chaque niveau"},
                {"fth-trending-up", "Suivi de progression", "Vois ton avancée en direct"}
        };
        Node[] cells = new Node[f.length];
        for (int i = 0; i < f.length; i++) {
            StackPane tile = new StackPane(ico(f[i][0], "vs-feat-icon"));
            tile.getStyleClass().add("vs-feat-tile");
            HBox cell = new HBox(12, tile, new VBox(2, lbl(f[i][1], "vs-feat-title"), lbl(f[i][2], "vs-muted")));
            cell.setAlignment(Pos.CENTER_LEFT);
            cells[i] = cell;
        }
        GridPane g = grid(4, 16, cells);
        g.getStyleClass().add("vs-strip");
        return g;
    }

    // ---------------------------------------------------------------- parcours

    private Node levels() {
        String[] titles = {"Les bases de Python", "Structures de contrôle", "Fonctions et modules", "Programmation objet"};
        String[] descs = {"Variables, types, entrées et sorties.", "Conditions, boucles for et while.",
                "Réutilise ton code avec des fonctions.", "Classes, objets et héritage."};
        String[] snippets = {"print(\"Salut PYLO\")", "for i in range(5):", "def saluer(nom):", "class Etudiant:"};
        String[] icons = {"fth-terminal", "fth-repeat", "fth-code", "fth-box"};
        Node[] cards = new Node[4];
        for (int i = 0; i < 4; i++) {
            StackPane banner = new StackPane(ico(icons[i], "vs-banner-icon"));
            banner.getStyleClass().addAll("vs-banner", "vs-b" + i);
            Label tag = lbl("Niveau " + (i + 1), "vs-tag");
            Label code = lbl(snippets[i], "vs-snippet");
            StackPane.setAlignment(tag, Pos.TOP_LEFT);
            StackPane.setAlignment(code, Pos.BOTTOM_LEFT);
            banner.getChildren().addAll(tag, code);

            Label desc = lbl(descs[i], "vs-muted");
            desc.setWrapText(true);
            HBox meta = new HBox(6, ico("fth-layers", "vs-perk-icon"), lbl("Cours · Exercices · Test", "vs-perk"));
            meta.setAlignment(Pos.CENTER_LEFT);
            Button start = new Button("Commencer");
            start.setGraphic(ico("fth-arrow-right", "vs-link-icon"));
            start.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
            start.getStyleClass().add("vs-link-strong");
            start.setOnAction(e -> onRegister.run());

            VBox body = new VBox(8, lbl(titles[i], "vs-card-title"), desc, meta, start);
            body.getStyleClass().add("vs-card-body");
            VBox card = new VBox(banner, body);
            card.getStyleClass().add("vs-course");
            cards[i] = card;
        }
        VBox box = new VBox(18, head("Parcours d'apprentissage", "Créer un compte →", onRegister), grid(4, 18, cards));
        return box;
    }

    // ---------------------------------------------------------------- thèmes

    private Node themes() {
        String[][] t = {
                {"fth-hash", "Variables", "Types et affectations"},
                {"fth-repeat", "Boucles", "for et while"},
                {"fth-code", "Fonctions", "def et retour"},
                {"fth-list", "Listes", "Collections de données"},
                {"fth-folder", "Fichiers", "Lecture et écriture"},
                {"fth-box", "Objets", "Classes et héritage"}
        };
        Node[] cells = new Node[t.length];
        for (int i = 0; i < t.length; i++) {
            StackPane tile = new StackPane(ico(t[i][0], "vs-theme-icon"));
            tile.getStyleClass().addAll("vs-theme-tile", "vs-th" + (i % 3));
            VBox cell = new VBox(8, tile, lbl(t[i][1], "vs-feat-title"), lbl(t[i][2], "vs-muted"));
            cell.getStyleClass().add("vs-theme");
            cell.setAlignment(Pos.CENTER);
            cells[i] = cell;
        }
        return new VBox(18, head("Explorer par thème", null, null), grid(6, 14, cells));
    }

    // ---------------------------------------------------------------- étapes

    private Node steps() {
        String[][] s = {
                {"fth-user-plus", "Crée ton compte", "Choisis ton espace : apprenant ou enseignant."},
                {"fth-layers", "Suis les niveaux", "Cours puis exercices, dans l'ordre du parcours."},
                {"fth-check-circle", "Valide par un test", "Réussis le test du niveau pour valider ta progression."}
        };
        Node[] cells = new Node[s.length];
        for (int i = 0; i < s.length; i++) {
            StackPane tile = new StackPane(ico(s[i][0], "vs-step-icon"));
            tile.getStyleClass().add("vs-step-tile");
            Label desc = lbl(s[i][2], "vs-muted");
            desc.setWrapText(true);
            VBox cell = new VBox(10, new HBox(12, tile, lbl("0" + (i + 1), "vs-step-num")),
                    lbl(s[i][1], "vs-card-title"), desc);
            cell.getStyleClass().add("vs-step");
            cells[i] = cell;
        }
        return new VBox(18, head("Comment ça marche", null, null), grid(3, 18, cells));
    }

    // ---------------------------------------------------------------- appel à l'action + pied de page

    private Node cta() {
        Label text = lbl("Prêt à écrire ton premier programme ?", "vs-cta-title");
        text.setWrapText(true);
        Label sub = lbl("Rejoins PYLO et avance niveau après niveau.", "vs-cta-sub");
        Button go = button("Créer mon compte", "fth-arrow-right", "vs-white", onRegister);
        VBox left = new VBox(10, text, sub, go);
        left.setAlignment(Pos.CENTER_LEFT);

        HBox right = new HBox(28, ctaStat("fth-book-open", "Cours"), ctaStat("fth-terminal", "Exercices"),
                ctaStat("fth-award", "Tests"));
        right.setAlignment(Pos.CENTER);

        HBox box = new HBox(left, spacer(), right);
        box.getStyleClass().add("vs-cta");
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.NEVER);
        return box;
    }

    private static Node ctaStat(String icon, String text) {
        VBox v = new VBox(6, ico(icon, "vs-cta-icon"), lbl(text, "vs-cta-stat"));
        v.setAlignment(Pos.CENTER);
        return v;
    }

    private Node footer() {
        HBox bar = new HBox(VisitorUi.logo(false), spacer(),
                lbl("© 2026 PYLO · Plateforme d'apprentissage de Python", "vs-muted"));
        bar.getStyleClass().add("vs-footer");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}