package com.pylo.navigation;

import javafx.beans.property.IntegerProperty;

/** Un élément de menu : titre, icône Feather, route cible et badge optionnel (observable). */
public record MenuEntry(String title, String icon, Route route, IntegerProperty badge) {

    public MenuEntry(String title, String icon, Route route) {
        this(title, icon, route, null);
    }
}