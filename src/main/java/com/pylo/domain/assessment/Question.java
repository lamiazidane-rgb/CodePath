package com.pylo.domain.assessment;

import java.util.Objects;

/** Question d'un test (composition : Test 1 -> 1..* Question). */
public class Question {

    private final int idQuestion;
    private String enonce;
    private String typeQuestion;   // ex. : "QCM", "VRAI_FAUX", "TEXTE"
    private double bareme;

    private Test test;

    public Question(int idQuestion, String enonce, String typeQuestion, double bareme) {
        this.idQuestion = idQuestion;
        setEnonce(enonce);
        this.typeQuestion = typeQuestion;
        setBareme(bareme);
    }

    public int getIdQuestion() { return idQuestion; }
    public String getEnonce() { return enonce; }
    public String getTypeQuestion() { return typeQuestion; }
    public double getBareme() { return bareme; }
    public Test getTest() { return test; }

    public void setEnonce(String enonce) {
        if (enonce == null || enonce.isBlank()) {
            throw new IllegalArgumentException("L'énoncé est obligatoire.");
        }
        this.enonce = enonce;
    }

    public void setTypeQuestion(String typeQuestion) { this.typeQuestion = typeQuestion; }

    public void setBareme(double bareme) {
        if (bareme < 0) {
            throw new IllegalArgumentException("Le barème ne peut pas être négatif.");
        }
        this.bareme = bareme;
    }

    /** Appelé par Test (ajouterQuestion / retirerQuestion). */
    void setTest(Test test) { this.test = test; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Question q && q.idQuestion == idQuestion);
    }

    @Override
    public int hashCode() { return Objects.hash(idQuestion); }
}