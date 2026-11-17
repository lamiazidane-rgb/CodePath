package com.pylo.domain.learning;

import java.util.Collections;
import java.util.List;

/** Élément d'une liste ordonnée (chapitres d'un cours, leçons d'un chapitre). */
interface Ordonne {

    void setOrdre(int ordre);

    /** Déplace un élément de {@code delta} positions (-1 = monter, +1 = descendre) puis renumérote 1..n. */
    static <T extends Ordonne> void deplacer(List<T> liste, T element, int delta) {
        int from = liste.indexOf(element);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= liste.size()) {
            return;
        }
        Collections.swap(liste, from, to);
        renumeroter(liste);
    }

    static <T extends Ordonne> void renumeroter(List<T> liste) {
        for (int i = 0; i < liste.size(); i++) {
            liste.get(i).setOrdre(i + 1);
        }
    }
}