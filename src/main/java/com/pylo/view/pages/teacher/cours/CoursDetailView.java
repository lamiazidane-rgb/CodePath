package com.pylo.view.pages.teacher.cours;

import com.pylo.domain.learning.Cours.Chapitre;
import com.pylo.domain.learning.Cours.Lecon;
import com.pylo.domain.learning.Cours.Ressource;
import com.pylo.domain.learning.Cours.StatutCours;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Exemple;
import com.pylo.view.pages.teacher.common.Motion;
import com.pylo.view.pages.teacher.common.TeacherUi;
import com.pylo.view.pages.teacher.cours.CoursView.CoursFlow;
import com.pylo.view.pages.teacher.cours.CoursView.Ui;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;

/** Consultation d'un cours + étape 4 : vérification avant publication, publication, archivage. */
class CoursDetailView extends VBox {

    private final CoursFlow flow;
    private final Cours course;
    private final Label message = new Label();

    CoursDetailView(CoursFlow flow, Cours course) {
        super(18);
        this.flow = flow;
        this.course = course;

        HBox navigation = new HBox(10,
                Ui.secondaryButton("Retour aux cours", "fth-arrow-left", flow::list),
                Ui.secondaryButton("Modifier les informations", "fth-edit-2", () -> flow.form(course)));
        getChildren().addAll(
                TeacherUi.header(course.getTitre(), "Consultation du cours",
                        TeacherUi.primaryButton("Gérer le contenu", "fth-layers", () -> flow.content(course, null))),
                navigation, message, information(), structure(), publication());
        Ui.show(message, null, false);
    }

    /* ---------- Informations ---------- */

    private Node information() {
        FlowPane facts = new FlowPane(32, 14,
                fact("Niveau", strong(TeacherUi.levelLabel(course.getNiveau()))),
                fact("Rang", strong(String.valueOf(course.getOrdre()))),
                fact("Statut", Ui.chip(course.getStatut().getLibelle(), tone(course.getStatut()))),
                fact("Durée", strong(Ui.duration(course.dureeTotaleMinutes()))),
                fact("Contenu", strong(course.nombreChapitres() + " chapitre(s) · " + course.nombreLecons() + " leçon(s)")),
                fact("Dernière modification", strong(Ui.date(course.getDateModification()))));

        VBox text = new VBox(10, facts,
                block("Description", course.getDescription()),
                block("Objectifs pédagogiques", course.getObjectifs()),
                block("Prérequis", course.getPrerequis()));
        return Ui.section("Informations générales", new HBox(16, Ui.cover(course.getImageCouverture(), 120, 90), text));
    }

    private static Node fact(String title, Node value) {
        return new VBox(2, Ui.label(title, "t-hint"), value);
    }

    private static Label strong(String text) {
        return Ui.label(text, "t-cell-strong");
    }

    private static Node block(String title, String text) {
        return new VBox(2, Ui.label(title, "t-hint"), Ui.label(Ui.orDefault(text, "Non renseigné"), "t-cell"));
    }

    private static String tone(StatutCours status) {
        return switch (status) {
            case PUBLIE -> "ok";
            case BROUILLON -> "warn";
            case ARCHIVE -> "neutral";
        };
    }

    /* ---------- Structure ---------- */

    private Node structure() {
        VBox tree = new VBox(8);
        if (course.getChapitres().isEmpty()) {
            tree.getChildren().add(Ui.label("Ce cours ne contient encore aucun chapitre.", "t-hint"));
        }
        for (Chapitre chapter : course.getChapitres()) {
            tree.getChildren().add(Ui.label("Chapitre " + chapter.getOrdre() + " : " + chapter.getTitre(), "t-cell-strong"));
            for (Lecon lesson : chapter.getLecons()) {
                HBox row = new HBox(8, Ui.label("Leçon " + lesson.getOrdre() + " : " + lesson.getTitre(), "t-cell"), Ui.spacer(),
                        Ui.label(lesson.getExemples().size() + " exemple(s) · " + lesson.getRessources().size() + " ressource(s)", "t-cell-sub"),
                        Ui.linkButton("Prévisualiser", () -> flow.preview(lesson, () -> flow.detail(course))));
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("t-list-row");
                tree.getChildren().add(row);
            }
        }
        return Ui.section("Structure du cours", tree);
    }

    /* ---------- Vérification et publication ---------- */

    private Node publication() {
        List<String> problems = course.verifierPublication();
        VBox check = new VBox(6);
        if (problems.isEmpty()) {
            check.getChildren().add(Ui.chip("✓ Le cours est complet et prêt à être publié.", "ok"));
        } else {
            check.getStyleClass().add("t-accent-yellow");
            check.setStyle("-fx-padding: 12;");
            check.getChildren().add(Ui.label("À corriger avant la publication :", "t-label"));
            problems.forEach(problem -> check.getChildren().add(Ui.label("• " + problem, "t-cell")));
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        switch (course.getStatut()) {
            case BROUILLON -> {
                Node publish = TeacherUi.primaryButton("Publier le cours", "fth-upload-cloud", () -> changeStatus(StatutCours.PUBLIE));
                publish.setDisable(!problems.isEmpty());
                actions.getChildren().add(publish);
            }
            case PUBLIE -> actions.getChildren().add(
                    Ui.secondaryButton("Repasser en brouillon", "fth-edit-3", () -> changeStatus(StatutCours.BROUILLON)));
            case ARCHIVE -> actions.getChildren().add(
                    Ui.secondaryButton("Restaurer en brouillon", "fth-rotate-ccw", () -> changeStatus(StatutCours.BROUILLON)));
        }
        if (course.getStatut() != StatutCours.ARCHIVE) {
            actions.getChildren().add(Ui.secondaryButton("Archiver", "fth-archive", () -> changeStatus(StatutCours.ARCHIVE)));
        }
        actions.getChildren().add(Ui.secondaryButton("Revenir à la modification", "fth-layers", () -> flow.content(course, null)));

        return Ui.section("Vérification et publication", check, actions);
    }

    private void changeStatus(StatutCours target) {
        String error = flow.changeStatus(course, target);
        if (error == null) {
            flow.list("Cours « " + course.getTitre() + " » : " + target.getLibelle().toLowerCase() + ".", false);
        } else {
            Ui.show(message, error, true);
        }
    }

    /** Prévisualisation d'une leçon exactement comme l'apprenant la verra. */
    static class LeconPreviewView extends VBox {

        private static final String STYLESHEET = "/css/pages/teacher-lesson-preview.css";

        LeconPreviewView(Lecon lesson, Runnable back) {
            super(18);
            getStyleClass().add("pv-root");
            URL css = LeconPreviewView.class.getResource(STYLESHEET);
            if (css != null) {
                getStylesheets().add(css.toExternalForm());
            }

            getChildren().addAll(
                    TeacherUi.header("Prévisualisation", "Aperçu fidèle de la leçon côté apprenant.",
                            Ui.secondaryButton("Retour", "fth-arrow-left", back)),
                    banner(), hero(lesson), theory(lesson));
            if (!lesson.getExemples().isEmpty()) {
                getChildren().add(examples(lesson));
            }
            if (!lesson.getRessources().isEmpty()) {
                getChildren().add(resources(lesson));
            }

            Motion.cascade(getChildren());
        }

        /* ---------- Bandeaux ---------- */

        private static Node banner() {
            FontIcon icon = new FontIcon("fth-eye");
            icon.getStyleClass().add("pv-banner-icon");
            VBox copy = new VBox(2, Ui.label("Vue apprenant", "pv-banner-title"),
                    Ui.label("Voici exactement ce que l'apprenant verra une fois le cours publié.", "pv-banner-text"));
            HBox box = new HBox(12, icon, copy);
            box.setAlignment(Pos.CENTER_LEFT);
            box.getStyleClass().add("pv-banner");
            return box;
        }

        private static Node hero(Lecon lesson) {
            VBox hero = new VBox(10, Ui.label("LEÇON", "pv-kicker"), Ui.label(lesson.getTitre(), "pv-title"));
            if (lesson.getDescription() != null && !lesson.getDescription().isBlank()) {
                hero.getChildren().add(Ui.label(lesson.getDescription(), "pv-lead"));
            }
            HBox meta = new HBox(10,
                    metaItem("fth-clock", Ui.duration(lesson.getDureeMinutes())),
                    metaItem("fth-code", lesson.getExemples().size() + " exemple(s)"),
                    metaItem("fth-paperclip", lesson.getRessources().size() + " ressource(s)"));
            meta.setAlignment(Pos.CENTER_LEFT);
            meta.getStyleClass().add("pv-meta");
            hero.getChildren().add(meta);
            hero.getStyleClass().add("pv-hero");
            return hero;
        }

        private static Node metaItem(String icon, String text) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.getStyleClass().add("pv-meta-icon");
            Label label = new Label(text);
            label.getStyleClass().add("pv-meta-text");
            HBox item = new HBox(7, fontIcon, label);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("pv-meta-item");
            return item;
        }

        /* ---------- Sections ---------- */

        private static Node sectionHeader(String icon, String title, String count) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.getStyleClass().add("pv-section-icon");
            StackPane box = new StackPane(fontIcon);
            box.getStyleClass().add("pv-section-icon-box");

            HBox header = new HBox(12, box, Ui.label(title, "pv-section-title"));
            if (count != null) {
                header.getChildren().addAll(Ui.spacer(), Ui.chip(count, "neutral"));
            }
            header.setAlignment(Pos.CENTER_LEFT);
            return header;
        }

        private static Node card(Node... content) {
            VBox card = new VBox(16, content);
            card.getStyleClass().add("pv-card");
            return card;
        }

        private static Node theory(Lecon lesson) {
            return card(sectionHeader("fth-book-open", "Contenu de la leçon", null),
                    LessonRenderer.render(lesson.getContenu()));
        }

        /* ---------- Exemples ---------- */

        private static Node examples(Lecon lesson) {
            VBox blocks = new VBox(16);
            lesson.getExemples().forEach(example -> blocks.getChildren().add(exampleBlock(example)));
            return card(sectionHeader("fth-code", "Exemples de code", lesson.getExemples().size() + " exemple(s)"), blocks);
        }

        private static Node exampleBlock(Exemple example) {
            HBox head = new HBox(10, Ui.label(example.getTitre(), "pv-example-title"), Ui.spacer(),
                    Ui.chip(Ui.orDefault(example.getLangage(), "Code"), "neutral"));
            head.setAlignment(Pos.CENTER_LEFT);

            VBox block = new VBox(10, head);
            if (example.getDescription() != null) {
                block.getChildren().add(Ui.label(example.getDescription(), "pv-example-desc"));
            }
            block.getChildren().add(readOnly(example.getCodeSource(), "pv-code-area"));
            if (example.getResultatAttendu() != null) {
                block.getChildren().addAll(Ui.label("RÉSULTAT ATTENDU", "pv-sub-label"),
                        readOnly(example.getResultatAttendu(), "pv-output-area"));
            }
            if (example.getExplication() != null) {
                VBox explain = new VBox(4, Ui.label("EXPLICATION", "pv-sub-label"),
                        Ui.label(example.getExplication(), "pv-explain-text"));
                explain.getStyleClass().add("pv-explain");
                block.getChildren().add(explain);
            }
            block.getStyleClass().add("pv-example");
            return block;
        }

        private static TextArea readOnly(String text, String styleClass) {
            TextArea area = new TextArea(text);
            area.setEditable(false);
            area.setPrefRowCount(Math.min(18, (int) text.lines().count() + 1));
            area.getStyleClass().add(styleClass);
            return area;
        }

        /* ---------- Ressources ---------- */

        private static Node resources(Lecon lesson) {
            VBox rows = new VBox(10);
            lesson.getRessources().forEach(resource -> rows.getChildren().add(resourceRow(resource)));
            return card(sectionHeader("fth-paperclip", "Ressources", lesson.getRessources().size() + " ressource(s)"), rows);
        }

        private static Node resourceRow(Ressource resource) {
            FontIcon icon = new FontIcon(switch (resource.getType()) {
                case PDF -> "fth-file-text";
                case IMAGE -> "fth-image";
                case VIDEO -> "fth-video";
                case LIEN -> "fth-link";
                case FICHIER -> "fth-file";
            });
            icon.getStyleClass().add("pv-res-icon");
            String tone = switch (resource.getType()) {
                case PDF -> "pv-tone-rose";
                case IMAGE -> "pv-tone-blue";
                case VIDEO -> "pv-tone-orange";
                case LIEN, FICHIER -> "pv-tone-violet";
            };
            StackPane iconBox = new StackPane(icon);
            iconBox.getStyleClass().addAll("pv-res-icon-box", tone);

            VBox text = new VBox(2, Ui.label(resource.getTitre(), "pv-res-title"),
                    Ui.label(Ui.orDefault(resource.getDescription(), resource.getEmplacement()), "pv-res-sub"));
            HBox row = new HBox(14, iconBox, text, Ui.spacer(), Ui.chip(resource.getType().getLibelle(), "neutral"));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("pv-resource");
            return row;
        }
    }

    /**
     * Affiche le contenu théorique d'une leçon tel que l'apprenant le verra.
     * Syntaxe prise en charge : titres (#), gras, italique, code, liens, listes, tableaux et images.
     * Le style vient de teacher-lesson-preview.css (classes pv-*), plus aucun style en dur ici.
     */
    static final class LessonRenderer {

        private static final Pattern INLINE = Pattern.compile("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*|`(.+?)`|\\[(.+?)]\\((.+?)\\)");
        private static final Pattern IMAGE = Pattern.compile("^!\\[(.*?)]\\((.+?)\\)$");
        private static final Pattern ORDERED = Pattern.compile("^(\\d+)\\.\\s+(.*)$");
        private static final Pattern BULLET = Pattern.compile("^[-*]\\s+(.*)$");

        private LessonRenderer() { }

        static VBox render(String markdown) {
            VBox box = new VBox(12);
            if (markdown == null || markdown.isBlank()) {
                box.getChildren().add(Ui.label("Aucun contenu théorique.", "t-hint"));
                return box;
            }
            List<String> lines = markdown.lines().map(String::strip).toList();
            int i = 0;
            while (i < lines.size()) {
                String line = lines.get(i);
                if (line.isEmpty()) {
                    i++;
                } else if (line.startsWith("|")) {
                    int end = runEnd(lines, i, l -> l.startsWith("|"));
                    box.getChildren().add(table(lines.subList(i, end)));
                    i = end;
                } else if (isItem(line)) {
                    int end = runEnd(lines, i, LessonRenderer::isItem);
                    box.getChildren().add(list(lines.subList(i, end)));
                    i = end;
                } else if (line.startsWith("#")) {
                    box.getChildren().add(heading(line));
                    i++;
                } else if (IMAGE.matcher(line).matches()) {
                    box.getChildren().add(image(line));
                    i++;
                } else {
                    int end = runEnd(lines, i, l -> !l.isEmpty() && !isBlock(l));
                    box.getChildren().add(inline(String.join(" ", lines.subList(i, end)), "pv-text"));
                    i = end;
                }
            }
            return box;
        }

        /* ---------- Blocs ---------- */

        private static Node heading(String line) {
            int level = 0;
            while (level < line.length() && line.charAt(level) == '#') {
                level++;
            }
            int size = Math.min(level, 3);
            VBox block = new VBox(inline(line.substring(level).strip(), "pv-h" + size));
            block.getStyleClass().addAll("pv-heading-block", "pv-heading-block-" + size);
            return block;
        }

        private static Node list(List<String> items) {
            VBox box = new VBox(8);
            for (String item : items) {
                Matcher ordered = ORDERED.matcher(item);
                Matcher bullet = BULLET.matcher(item);
                boolean isOrdered = ordered.matches();
                String text = isOrdered ? ordered.group(2) : bullet.matches() ? bullet.group(1) : item;

                Node marker;
                if (isOrdered) {
                    marker = Ui.label(ordered.group(1) + ".", "pv-list-number");
                } else {
                    Region dot = new Region();
                    dot.getStyleClass().add("pv-bullet-dot");
                    VBox wrap = new VBox(dot);
                    wrap.getStyleClass().add("pv-bullet-wrap");
                    marker = wrap;
                }
                TextFlow flow = inline(text, "pv-text");
                HBox.setHgrow(flow, Priority.ALWAYS);
                HBox row = new HBox(8, marker, flow);
                row.setAlignment(Pos.TOP_LEFT);
                box.getChildren().add(row);
            }
            return box;
        }

        private static Node table(List<String> rows) {
            GridPane grid = new GridPane();
            int rowIndex = 0;
            int columns = 0;
            for (String row : rows) {
                List<String> cells = cells(row);
                if (cells.stream().allMatch(c -> c.matches(":?-{2,}:?"))) {
                    continue;                                       // ligne de séparation
                }
                columns = Math.max(columns, cells.size());
                boolean header = rowIndex == 0;
                for (int c = 0; c < cells.size(); c++) {
                    VBox cell = new VBox(inline(cells.get(c), header ? "pv-th-text" : "pv-td-text"));
                    cell.getStyleClass().add(header ? "pv-th-cell" : "pv-td-cell");
                    if (!header && rowIndex % 2 == 0) {
                        cell.getStyleClass().add("pv-td-alt");
                    }
                    grid.add(cell, c, rowIndex);
                }
                rowIndex++;
            }
            for (int c = 0; c < columns; c++) {
                ColumnConstraints constraints = new ColumnConstraints();
                constraints.setPercentWidth(100.0 / columns);
                grid.getColumnConstraints().add(constraints);
            }

            // Cadre arrondi : le clip évite que les cellules dépassent des coins.
            StackPane frame = new StackPane(grid);
            frame.getStyleClass().add("pv-table-frame");
            Rectangle clip = new Rectangle();
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            clip.widthProperty().bind(frame.widthProperty());
            clip.heightProperty().bind(frame.heightProperty());
            frame.setClip(clip);
            return frame;
        }

        private static Node image(String line) {
            Matcher m = IMAGE.matcher(line);
            m.matches();
            String source = m.group(2);
            String url = source.matches("(?i)^(https?|file):.*") ? source : new File(source).toURI().toString();
            Image image = new Image(url, 640, 0, true, true);
            if (image.isError()) {
                return Ui.label("[Image introuvable : " + m.group(1) + "]", "t-hint");
            }
            StackPane frame = new StackPane(new ImageView(image));
            frame.getStyleClass().add("pv-image-frame");
            frame.setMaxWidth(Region.USE_PREF_SIZE);

            VBox figure = new VBox(6, frame);
            if (!m.group(1).isBlank()) {
                figure.getChildren().add(Ui.label(m.group(1), "pv-caption"));
            }
            return figure;
        }

        /* ---------- Texte en ligne ---------- */

        /** @param base classe du texte courant : pv-text, pv-h1..pv-h3, pv-th-text, pv-td-text */
        private static TextFlow inline(String text, String base) {
            TextFlow flow = new TextFlow();
            flow.getStyleClass().add("pv-flow");
            Matcher m = INLINE.matcher(text);
            int last = 0;
            while (m.find()) {
                if (m.start() > last) {
                    flow.getChildren().add(run(text.substring(last, m.start()), base, null));
                }
                if (m.group(1) != null) {
                    flow.getChildren().add(run(m.group(1), base, "pv-bold"));
                } else if (m.group(2) != null) {
                    flow.getChildren().add(run(m.group(2), base, "pv-italic"));
                } else if (m.group(3) != null) {
                    flow.getChildren().add(run(m.group(3), base, "pv-code-inline"));
                } else {
                    flow.getChildren().add(run(m.group(4), base, "pv-link-text"));
                }
                last = m.end();
            }
            if (last < text.length()) {
                flow.getChildren().add(run(text.substring(last), base, null));
            }
            return flow;
        }

        private static Text run(String content, String base, String modifier) {
            Text text = new Text(content);
            text.getStyleClass().add(base);
            if (modifier != null) {
                text.getStyleClass().add(modifier);
            }
            return text;
        }

        /* ---------- Analyse ---------- */

        private static int runEnd(List<String> lines, int from, Predicate<String> accept) {
            int end = from;
            while (end < lines.size() && accept.test(lines.get(end))) {
                end++;
            }
            return end;
        }

        private static boolean isItem(String line) {
            return ORDERED.matcher(line).matches() || BULLET.matcher(line).matches();
        }

        private static boolean isBlock(String line) {
            return line.startsWith("|") || line.startsWith("#") || isItem(line) || IMAGE.matcher(line).matches();
        }

        private static List<String> cells(String row) {
            String s = row.strip();
            if (s.startsWith("|")) {
                s = s.substring(1);
            }
            if (s.endsWith("|")) {
                s = s.substring(0, s.length() - 1);
            }
            return Arrays.stream(s.split("\\|", -1)).map(String::strip).toList();
        }
    }
}