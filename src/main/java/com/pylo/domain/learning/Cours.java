package com.pylo.domain.learning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cours appartenant à un niveau (association «contient» : Niveau 1 -> 1..* Cours).
 * Un cours contient des chapitres, qui contiennent des leçons (exemples et ressources dans les leçons).
 */
public class Cours {

    private static final AtomicInteger NEXT_ID = new AtomicInteger((int) (System.currentTimeMillis() / 1000));

    private final int idCours;
    private String titre;
    private String description;
    private String contenu;
    private int ordre;
    private String objectifs;
    private int dureeMinutes;
    private String prerequis;
    private String imageCouverture;   // chemin local de l'image (facultatif)
    private StatutCours statut = StatutCours.BROUILLON;
    private LocalDate dateModification = LocalDate.now();

    private Niveau niveau;   // niveau auquel appartient ce cours
    private final List<Chapitre> chapitres = new ArrayList<>();

    public Cours(int idCours, String titre, String description, String contenu, int ordre) {
        this.idCours = idCours;
        setTitre(titre);
        this.description = description;
        this.contenu = contenu;
        setOrdre(ordre);
    }

    /* ---------- Comportements du diagramme ---------- */

    /** Renvoie le cours pour consultation (ici : une description résumée). */
    public String consulterCours() {
        return "Cours " + ordre + " : " + titre + (description != null ? " - " + description : "");
    }

    /** Renvoie le contenu du cours. */
    public String afficherContenu() {
        return contenu != null ? contenu : "";
    }

    /* ---------- Chapitres ---------- */

    public void ajouterChapitre(Chapitre c) {
        Objects.requireNonNull(c, "Le chapitre est obligatoire.");
        if (!chapitres.contains(c)) {
            chapitres.add(c);
            touch();
        }
    }

    public void retirerChapitre(Chapitre c) {
        if (chapitres.remove(c)) {
            Ordonne.renumeroter(chapitres);
            touch();
        }
    }

    public void deplacerChapitre(Chapitre c, int delta) {
        Ordonne.deplacer(chapitres, c, delta);
        touch();
    }

    public int nombreChapitres() { return chapitres.size(); }

    public int nombreLecons() { return chapitres.stream().mapToInt(c -> c.getLecons().size()).sum(); }

    /** Durée affichée : somme des chapitres si le cours en a, sinon durée saisie. */
    public int dureeTotaleMinutes() {
        int somme = chapitres.stream().mapToInt(Chapitre::getDureeMinutes).sum();
        return somme > 0 ? somme : dureeMinutes;
    }

    /* ---------- Publication ---------- */

    /** Liste des problèmes qui empêchent la publication (vide = publiable). */
    public List<String> verifierPublication() {
        List<String> problemes = new ArrayList<>();
        if (description == null || description.isBlank()) {
            problemes.add("La description du cours est obligatoire.");
        }
        if (objectifs == null || objectifs.isBlank()) {
            problemes.add("Les objectifs pédagogiques sont obligatoires.");
        }
        if (niveau == null) {
            problemes.add("Le cours doit appartenir à un niveau.");
        }
        if (chapitres.isEmpty()) {
            problemes.add("Le cours doit contenir au moins un chapitre.");
        }
        for (Chapitre chapitre : chapitres) {
            if (chapitre.getLecons().isEmpty()) {
                problemes.add("Le chapitre « " + chapitre.getTitre() + " » doit contenir au moins une leçon.");
            }
            for (Lecon lecon : chapitre.getLecons()) {
                if (!lecon.aUnContenu()) {
                    problemes.add("La leçon « " + lecon.getTitre() + " » n'a aucun contenu.");
                }
            }
        }
        return problemes;
    }

    public boolean estPubliable() { return verifierPublication().isEmpty(); }

    /** Change le statut ; la publication est refusée tant que le cours est incomplet. */
    public void changerStatut(StatutCours nouveau) {
        Objects.requireNonNull(nouveau, "Le statut est obligatoire.");
        if (nouveau == StatutCours.PUBLIE) {
            List<String> problemes = verifierPublication();
            if (!problemes.isEmpty()) {
                throw new IllegalStateException("Publication impossible : " + problemes.get(0)
                        + (problemes.size() > 1 ? " (+" + (problemes.size() - 1) + " autre(s))" : ""));
            }
        }
        this.statut = nouveau;
        touch();
    }

    /* ---------- Accesseurs ---------- */

    public int getIdCours() { return idCours; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getContenu() { return contenu; }
    public int getOrdre() { return ordre; }
    public String getObjectifs() { return objectifs; }
    public int getDureeMinutes() { return dureeMinutes; }
    public String getPrerequis() { return prerequis; }
    public String getImageCouverture() { return imageCouverture; }
    public StatutCours getStatut() { return statut; }
    public LocalDate getDateModification() { return dateModification; }
    public Niveau getNiveau() { return niveau; }
    public List<Chapitre> getChapitres() { return Collections.unmodifiableList(chapitres); }

    /* ---------- Mutateurs ---------- */

    public void setTitre(String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre du cours est obligatoire.");
        }
        this.titre = titre;
        touch();
    }

    public void setDescription(String description) { this.description = description; touch(); }
    public void setContenu(String contenu) { this.contenu = contenu; touch(); }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; touch(); }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; touch(); }
    public void setImageCouverture(String imageCouverture) { this.imageCouverture = imageCouverture; touch(); }

    public void setOrdre(int ordre) {
        if (ordre < 1) {
            throw new IllegalArgumentException("L'ordre doit être >= 1.");
        }
        this.ordre = ordre;
        touch();
    }

    public void setDureeMinutes(int dureeMinutes) {
        if (dureeMinutes < 0) {
            throw new IllegalArgumentException("La durée ne peut pas être négative.");
        }
        this.dureeMinutes = dureeMinutes;
        touch();
    }

    /** Appelé par Niveau (ajouterCours / retirerCours). Ne rappelle PAS Niveau pour éviter la récursion. */
    void setNiveau(Niveau niveau) { this.niveau = niveau; }

    private void touch() { dateModification = LocalDate.now(); }

    /** Identifiant pour les chapitres, leçons, exemples et ressources créés depuis l'interface. */
    public static int nouvelId() { return NEXT_ID.incrementAndGet(); }

    /* ---------- Identité ---------- */

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cours c && c.idCours == idCours);
    }

    @Override
    public int hashCode() { return Objects.hash(idCours); }

    @Override
    public String toString() { return "Cours[" + idCours + ", " + titre + "]"; }

    /** Cycle de vie d'un cours. */
    public enum StatutCours {
        BROUILLON("Brouillon"), PUBLIE("Publié"), ARCHIVE("Archivé");

        private final String libelle;

        StatutCours(String libelle) { this.libelle = libelle; }

        public String getLibelle() { return libelle; }
    }

    /** Nature d'une ressource jointe à une leçon. */
    public enum TypeRessource {
        PDF("PDF"), IMAGE("Image"), VIDEO("Vidéo"), LIEN("Lien externe"), FICHIER("Fichier");

        private final String libelle;

        TypeRessource(String libelle) { this.libelle = libelle; }

        public String getLibelle() { return libelle; }
    }

    /** Support complémentaire d'une leçon (PDF, image, vidéo, lien, fichier). */
    public static class Ressource {

        private final int idRessource;
        private final String titre;
        private final TypeRessource type;
        private final String emplacement;   // chemin du fichier ou URL
        private final String description;   // facultative

        public Ressource(int idRessource, String titre, TypeRessource type, String emplacement, String description) {
            if (titre == null || titre.isBlank()) {
                throw new IllegalArgumentException("Le titre de la ressource est obligatoire.");
            }
            if (emplacement == null || emplacement.isBlank()) {
                throw new IllegalArgumentException("Le fichier ou le lien de la ressource est obligatoire.");
            }
            this.idRessource = idRessource;
            this.titre = titre;
            this.type = Objects.requireNonNull(type, "Le type de ressource est obligatoire.");
            this.emplacement = emplacement;
            this.description = description;
        }

        public int getIdRessource() { return idRessource; }
        public String getTitre() { return titre; }
        public TypeRessource getType() { return type; }
        public String getEmplacement() { return emplacement; }
        public String getDescription() { return description; }

        @Override
        public boolean equals(Object o) { return this == o || (o instanceof Ressource r && r.idRessource == idRessource); }

        @Override
        public int hashCode() { return Integer.hashCode(idRessource); }

        @Override
        public String toString() { return "Ressource[" + idRessource + ", " + titre + "]"; }
    }

    /** Leçon d'un chapitre : contenu théorique + exemples de code + ressources. */
    public static class Lecon implements Ordonne {

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

        /** Remplace tous les exemples (utilisé par l'éditeur qui travaille sur une copie). */
        public void definirExemples(List<Exemple> nouveaux) {
            List<Exemple> copie = new ArrayList<>(nouveaux);
            exemples.clear();
            exemples.addAll(copie);
        }

        /** Remplace toutes les ressources (utilisé par l'éditeur qui travaille sur une copie). */
        public void definirRessources(List<Ressource> nouvelles) {
            List<Ressource> copie = new ArrayList<>(nouvelles);
            ressources.clear();
            ressources.addAll(copie);
        }

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

    /** Chapitre d'un cours : division thématique contenant des leçons ordonnées. */
    public static class Chapitre implements Ordonne {

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

    /** Élément d'une liste ordonnée (chapitres d'un cours, leçons d'un chapitre). */
    private interface Ordonne {

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
}