package com.pylo.domain.learning;

/** Exemple de code illustrant une leçon. */
public class Exemple {

    private final int idExemple;
    private final String titre;
    private final String codeSource;
    private final String description;
    private final String langage;
    private final String resultatAttendu;
    private final String explication;

    /** Constructeur d'origine conservé : compatible avec le code existant. */
    public Exemple(int idExemple, String titre, String codeSource, String description) {
        this(idExemple, titre, codeSource, description, "Java", null, null);
    }

    public Exemple(int idExemple, String titre, String codeSource, String description,
                   String langage, String resultatAttendu, String explication) {
        this.idExemple = idExemple;
        this.titre = titre;
        this.codeSource = codeSource;
        this.description = description;
        this.langage = langage;
        this.resultatAttendu = resultatAttendu;
        this.explication = explication;
    }

    public int getIdExemple() { return idExemple; }
    public String getTitre() { return titre; }
    public String getCodeSource() { return codeSource; }
    public String getDescription() { return description; }
    public String getLangage() { return langage; }
    public String getResultatAttendu() { return resultatAttendu; }
    public String getExplication() { return explication; }

    @Override
    public String toString() { return "Exemple[" + idExemple + ", " + titre + "]"; }
}