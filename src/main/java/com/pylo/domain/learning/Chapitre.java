package com.pylo.domain.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Chapitre d'un cours : division thématique contenant des leçons ordonnées. */
public class Chapitre implements Ordonne {

    private final int idChapitre;
    private String titre;
    private String description;
    private String objectifs;
    private int ordre;
    private int dureeMinutes;

    private final List<Lecon> lecons = new ArrayList<>();

    public Chapitre(int idChapitre, String titre, String description, String objectifs, int ordre, int dureeMinutes) {
        this.idChapitre = idChapitre;
        setTitre(titre);
        this.description = description;
        this.objectifs = objectifs;
        setOrdre(ordre);
        setDureeMinutes(dureeMinutes);
    }

    public void ajouterLecon(Lecon l) {
        Objects.requireNonNull(l, "La leçon est obligatoire.");
        if (!lecons.contains(l)) {
            lecons.add(l);
        }
    }

    public void retirerLecon(Lecon l) {
        if (lecons.remove(l)) {
            Ordonne.renumeroter(lecons);
        }
    }

    public void deplacerLecon(Lecon l, int delta) { Ordonne.deplacer(lecons, l, delta); }

    public int getIdChapitre() { return idChapitre; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getObjectifs() { return objectifs; }
    public int getOrdre() { return ordre; }
    public int getDureeMinutes() { return dureeMinutes; }
    public List<Lecon> getLecons() { return Collections.unmodifiableList(lecons); }

    public void setTitre(String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre du chapitre est obligatoire.");
        }
        this.titre = titre;
    }

    public void setDescription(String description) { this.description = description; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }

    @Override
    public void setOrdre(int ordre) {
        if (ordre < 1) {
            throw new IllegalArgumentException("L'ordre doit être >= 1.");
        }
        this.ordre = ordre;
    }

    public void setDureeMinutes(int dureeMinutes) {
        if (dureeMinutes < 0) {
            throw new IllegalArgumentException("La durée ne peut pas être négative.");
        }
        this.dureeMinutes = dureeMinutes;
    }

    @Override
    public boolean equals(Object o) { return this == o || (o instanceof Chapitre c && c.idChapitre == idChapitre); }

    @Override
    public int hashCode() { return Integer.hashCode(idChapitre); }

    @Override
    public String toString() { return "Chapitre[" + idChapitre + ", " + titre + "]"; }
}