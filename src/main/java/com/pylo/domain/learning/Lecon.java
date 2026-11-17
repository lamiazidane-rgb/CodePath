package com.pylo.domain.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Leçon d'un chapitre : contenu théorique + exemples de code + ressources. */
public class Lecon implements Ordonne {

    private final int idLecon;
    private String titre;
    private String description;
    private String contenu;
    private int ordre;
    private int dureeMinutes;

    private final List<Exemple> exemples = new ArrayList<>();
    private final List<Ressource> ressources = new ArrayList<>();

    public Lecon(int idLecon, String titre, String description, String contenu, int ordre, int dureeMinutes) {
        this.idLecon = idLecon;
        setTitre(titre);
        this.description = description;
        this.contenu = contenu;
        setOrdre(ordre);
        setDureeMinutes(dureeMinutes);
    }

    public void ajouterExemple(Exemple e) { exemples.add(Objects.requireNonNull(e, "L'exemple est obligatoire.")); }
    public void retirerExemple(Exemple e) { exemples.remove(e); }
    public void ajouterRessource(Ressource r) { ressources.add(Objects.requireNonNull(r, "La ressource est obligatoire.")); }
    public void retirerRessource(Ressource r) { ressources.remove(r); }

    /** Une leçon est publiable si elle contient du texte ou au moins un exemple. */
    public boolean aUnContenu() {
        return (contenu != null && !contenu.isBlank()) || !exemples.isEmpty();
    }

    public int getIdLecon() { return idLecon; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getContenu() { return contenu; }
    public int getOrdre() { return ordre; }
    public int getDureeMinutes() { return dureeMinutes; }
    public List<Exemple> getExemples() { return Collections.unmodifiableList(exemples); }
    public List<Ressource> getRessources() { return Collections.unmodifiableList(ressources); }

    public void setTitre(String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre de la leçon est obligatoire.");
        }
        this.titre = titre;
    }

    public void setDescription(String description) { this.description = description; }
    public void setContenu(String contenu) { this.contenu = contenu; }

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
    public boolean equals(Object o) { return this == o || (o instanceof Lecon l && l.idLecon == idLecon); }

    @Override
    public int hashCode() { return Integer.hashCode(idLecon); }

    @Override
    public String toString() { return "Lecon[" + idLecon + ", " + titre + "]"; }
}