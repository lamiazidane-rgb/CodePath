package com.pylo.view.pages.visitor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.pylo.view.pages.visitor.VisitorUi.ico;
import static com.pylo.view.pages.visitor.VisitorUi.lbl;

/**
 * Page de connexion ou d'inscription (même mise en page : carte à deux volets).
 * Le traitement est délégué au {@link Handler} : il renvoie un message d'erreur, ou null si tout s'est bien passé
 * (c'est alors à l'appelant de naviguer vers l'espace apprenant / enseignant).
 */
public final class AuthPage extends ScrollPane {

    @FunctionalInterface
    public interface Handler {
        /** name = null en connexion ; role = "APPRENANT" ou "ENSEIGNANT" en inscription (null en connexion). */
        String submit(String name, String email, String password, String role);
    }

    private final boolean register;
    private final Handler handler;
    private final Runnable switchMode;
    private final Runnable goHome;

    private final TextField name = new TextField();
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final TextField passwordText = new TextField();
    private final ToggleGroup roles = new ToggleGroup();
    private final CheckBox terms = new CheckBox("J'accepte les conditions d'utilisation et la politique de confidentialité");
    private final Label message = new Label();

    public static AuthPage login(Handler handler, Runnable toRegister, Runnable toHome) {
        return new AuthPage(false, handler, toRegister, toHome);
    }

    public static AuthPage register(Handler handler, Runnable toLogin, Runnable toHome) {
        return new AuthPage(true, handler, toLogin, toHome);
    }

    private AuthPage(boolean register, Handler handler, Runnable switchMode, Runnable goHome) {
        this.register = register;
        this.handler = handler;
        this.switchMode = switchMode;
        this.goHome = goHome;

        VisitorUi.style(this);
        getStyleClass().add("vs-scroll");
        setFitToWidth(true);
        setFitToHeight(true);

        GridPane card = new GridPane();
        card.getStyleClass().add("vs-auth-card");
        card.setMaxWidth(1000);
        card.setMinHeight(590);
        ColumnConstraints a = new ColumnConstraints();
        a.setPercentWidth(44);
        ColumnConstraints b = new ColumnConstraints();
        b.setPercentWidth(56);
        card.getColumnConstraints().addAll(a, b);
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        card.getRowConstraints().add(row);
        card.add(leftPanel(), 0, 0);
        card.add(rightPanel(), 1, 0);

        StackPane stage = new StackPane(card);
        stage.getStyleClass().add("vs-auth-stage");
        stage.setPadding(new Insets(32));
        setContent(stage);
    }
    private String handleLogin(
            String name,
            String email,
            String password,
            String role) {

        System.out.println("Connexion : " + email);

        return "Identifiants incorrects.";
    }

    private String handleRegister(
            String name,
            String email,
            String password,
            String role) {

        System.out.println("Inscription : " + name);
        System.out.println("Rôle : " + role);

        return null;
    }
    private static Node vSpacer() {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    // ---------------------------------------------------------------- volet gauche (illustration)

    private Node leftPanel() {
        Pane art = VisitorUi.desk();
        art.getChildren().addAll(
                VisitorUi.badge("fth-code", 36, 12, 2.6),
                VisitorUi.badge("fth-zap", 140, 0, 3.1),
                VisitorUi.badge("fth-book-open", 246, 24, 2.3));
        HBox artRow = new HBox(art);
        artRow.setAlignment(Pos.CENTER);

        Label title = lbl(register ? "Apprends Python avec des parcours clairs et guidés."
                : "Content de te revoir sur PYLO !", "vs-auth-title");
        title.setWrapText(true);
        Label sub = lbl(register ? "Cours, exercices et tests de validation, à ton rythme."
                : "Reprends ton parcours là où tu t'étais arrêté.", "vs-auth-sub");
        sub.setWrapText(true);

        VBox left = new VBox(16, VisitorUi.logo(true), title, sub, vSpacer(), artRow);
        left.getStyleClass().add("vs-auth-left");
        return left;
    }

    // ---------------------------------------------------------------- volet droit (formulaire)

    private Node rightPanel() {
        Button home = new Button("Accueil", ico("fth-arrow-left", "vs-link-icon"));
        home.getStyleClass().add("vs-link");
        home.setOnAction(e -> goHome.run());

        VBox form = new VBox(16);
        form.getStyleClass().add("vs-auth-right");
        form.getChildren().add(home);
        form.getChildren().add(lbl(register ? "Créer un compte" : "Connexion", "vs-form-title"));

        email.setPromptText("Adresse e-mail");
        email.getStyleClass().add("vs-input");
        password.setPromptText("Mot de passe");
        password.getStyleClass().add("vs-input");
        passwordText.setPromptText("Mot de passe");
        passwordText.getStyleClass().add("vs-input");
        passwordText.textProperty().bindBidirectional(password.textProperty());
        passwordText.visibleProperty().bind(password.visibleProperty().not());
        passwordText.managedProperty().bind(passwordText.visibleProperty());
        password.managedProperty().bind(password.visibleProperty());

        if (register) {
            name.setPromptText("Nom complet");
            name.getStyleClass().add("vs-input");
            form.getChildren().add(line("fth-user", name));
        }
        form.getChildren().add(line("fth-mail", email));
        form.getChildren().add(line("fth-lock", new HBox(password, passwordText), eyeButton()));

        if (register) {
            form.getChildren().add(roleToggle());
            terms.getStyleClass().add("vs-check");
            terms.setWrapText(true);
            form.getChildren().add(terms);
        }

        message.getStyleClass().add("vs-msg");
        message.setWrapText(true);
        message.setVisible(false);
        message.setManaged(false);
        form.getChildren().add(message);

        Button submit = VisitorUi.button(register ? "S'inscrire" : "Se connecter", null, "vs-primary", this::submit);
        submit.setMaxWidth(Double.MAX_VALUE);
        form.getChildren().add(submit);
        email.setOnAction(e -> submit());
        password.setOnAction(e -> submit());

        form.getChildren().add(lbl(register ? "Ou s'inscrire avec" : "Ou se connecter avec", "vs-or"));
        form.getChildren().add(socials());

        Button other = new Button(register ? "Se connecter" : "Créer un compte");
        other.getStyleClass().add("vs-link-strong");
        other.setOnAction(e -> switchMode.run());
        HBox bottom = new HBox(6, lbl(register ? "Déjà un compte ?" : "Pas encore de compte ?", "vs-muted"), other);
        bottom.setAlignment(Pos.CENTER);
        form.getChildren().add(bottom);
        return form;
    }

    /** Champ « à la ligne » : icône + saisie (+ boutons) soulignés. */
    private HBox line(String icon, Node input, Node... extra) {
        HBox.setHgrow(input, Priority.ALWAYS);
        if (input instanceof HBox h) {
            for (Node n : h.getChildren()) {
                if (n instanceof TextField tf) {
                    tf.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(tf, Priority.ALWAYS);
                }
            }
        }
        HBox box = new HBox(8, ico(icon, "vs-field-icon"), input);
        box.getChildren().addAll(extra);
        box.getStyleClass().add("vs-line-box");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button eyeButton() {
        FontIcon eye = ico("fth-eye", "vs-field-icon");
        Button b = new Button();
        b.setGraphic(eye);
        b.getStyleClass().add("vs-eye");
        b.setTooltip(new Tooltip("Afficher / masquer le mot de passe"));
        b.setOnAction(e -> {
            password.setVisible(!password.isVisible());
            eye.setIconLiteral(password.isVisible() ? "fth-eye" : "fth-eye-off");
        });
        return b;
    }

    private Node roleToggle() {
        ToggleButton learner = new ToggleButton("Apprenant");
        learner.setGraphic(ico("fth-book-open", "vs-seg-icon"));
        learner.setUserData("APPRENANT");
        ToggleButton teacher = new ToggleButton("Enseignant");
        teacher.setGraphic(ico("fth-users", "vs-seg-icon"));
        teacher.setUserData("ENSEIGNANT");
        for (ToggleButton t : new ToggleButton[]{learner, teacher}) {
            t.setToggleGroup(roles);
            t.getStyleClass().add("vs-seg");
            t.setContentDisplay(ContentDisplay.LEFT);
            t.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(t, Priority.ALWAYS);
        }
        learner.setSelected(true);
        // un des deux reste toujours sélectionné
        roles.selectedToggleProperty().addListener((o, was, now) -> {
            if (now == null && was != null) {
                was.setSelected(true);
            }
        });
        HBox box = new HBox(8, lbl("Je suis", "vs-muted"), learner, teacher);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node socials() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER);
        String[] names = {"Google", "Facebook", "GitHub", "LinkedIn"};
        for (String n : names) {
            Button b = new Button();
            b.getStyleClass().add("vs-social");
            if (n.equals("Google")) {
                b.setGraphic(lbl("G", "vs-social-g"));
            } else {
                b.setGraphic(ico("fth-" + n.toLowerCase(), "vs-social-icon"));
            }
            b.setTooltip(new Tooltip(n));
            b.setOnAction(e -> show(n + " : connexion bientôt disponible."));
            row.getChildren().add(b);
        }
        return row;
    }

    // ---------------------------------------------------------------- validation

    private void submit() {
        String n = name.getText().trim();
        String mail = email.getText().trim();
        String pass = password.getText();
        String error = null;
        if (register && n.isEmpty()) {
            error = "Saisis ton nom complet.";
        } else if (!mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            error = "Adresse e-mail invalide.";
        } else if (register && pass.length() < 6) {
            error = "Le mot de passe doit contenir au moins 6 caractères.";
        } else if (!register && pass.isEmpty()) {
            error = "Saisis ton mot de passe.";
        } else if (register && !terms.isSelected()) {
            error = "Accepte les conditions pour continuer.";
        }
        if (error == null) {
            Toggle t = roles.getSelectedToggle();
            String role = register && t != null ? (String) t.getUserData() : null;
            error = handler.submit(register ? n : null, mail, pass, role);
        }
        show(error);
    }

    private void show(String text) {
        boolean visible = text != null && !text.isEmpty();
        message.setText(visible ? text : "");
        message.setVisible(visible);
        message.setManaged(visible);
    }
}