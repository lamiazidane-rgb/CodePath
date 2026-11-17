package com.pylo.view.pages.teacher.common;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;

/** Tableau à lignes de nœuds (texte, pastilles, boutons) : une colonne extensible, les autres de largeur fixe. */
public final class DataTable extends VBox {

    /**
     * Colonne : {@code grow} occupe l'espace restant, {@code flex} a une largeur souhaitée mais peut
     * rétrécir (le texte est alors tronqué), {@code fixed} garde toujours sa largeur (boutons, pastilles).
     */
    public record Column(String title, double width, double minWidth) {
        private static final double GROW_MIN_WIDTH = 100;
        private static final double FLEX_SHRINK_RATIO = 0.6;

        public static Column grow(String title) { return new Column(title, -1, GROW_MIN_WIDTH); }
        public static Column flex(String title, double width) { return new Column(title, width, width * FLEX_SHRINK_RATIO); }
        public static Column fixed(String title, double width) { return new Column(title, width, width); }
    }

    private final List<Column> columns;
    private final VBox rows = new VBox();

    public DataTable(Column... columns) {
        this.columns = List.of(columns);
        Node[] heads = new Node[columns.length];
        for (int i = 0; i < columns.length; i++) {
            heads[i] = TeacherUi.label(columns[i].title(), "t-th");
        }
        getChildren().addAll(line(heads, "t-table-head"), rows);
    }

    /** Remplace le contenu ; {@code cells} produit une cellule par colonne pour chaque élément. */
    public <T> void setItems(List<T> items, Function<T, Node[]> cells, String emptyText) {
        rows.getChildren().clear();
        if (items.isEmpty()) {
            rows.getChildren().add(TeacherUi.emptyState("fth-inbox", emptyText));
            return;
        }
        for (T item : items) {
            rows.getChildren().add(line(cells.apply(item), "t-row"));
        }
    }

    public static Label text(String text) {
        return TeacherUi.label(text, "t-cell");
    }

    /** Cellule à deux lignes : valeur principale en gras et précision en dessous. */
    public static VBox twoLines(String main, String sub) {
        Label mainLabel = TeacherUi.label(main, "t-cell-strong");
        Label subLabel = TeacherUi.label(sub, "t-cell-sub");
        return new VBox(2, mainLabel, subLabel);
    }

    private GridPane line(Node[] cells, String styleClass) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.getStyleClass().add(styleClass);
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setMinWidth(column.minWidth());
            if (column.width() < 0) {
                constraints.setHgrow(Priority.ALWAYS);
            } else {
                constraints.setHgrow(Priority.SOMETIMES);
                constraints.setPrefWidth(column.width());
                constraints.setMaxWidth(column.width());
            }
            grid.getColumnConstraints().add(constraints);
            grid.add(cells[i], i, 0);
            GridPane.setValignment(cells[i], VPos.CENTER);
        }
        return grid;
    }
}
