package com.pylo.service;

import com.pylo.domain.assessment.Question;
import com.pylo.domain.assessment.Test;
import com.pylo.domain.enums.Difficulte;
import com.pylo.domain.enums.StatutCours;
import com.pylo.domain.learning.Cours;
import com.pylo.domain.learning.Exercice;
import com.pylo.domain.learning.Niveau;
import com.pylo.domain.user.Enseignant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Stockage en mémoire du contenu pédagogique : niveaux, cours, exercices, tests et questions.
 * Toutes les règles métier (validation, relations) restent dans le package {@code domain} :
 * ce service ne fait que créer, retrouver, modifier et supprimer les objets du domaine.
 * Chaque modification est d'abord validée sur un objet temporaire, pour ne jamais laisser
 * une entité à moitié modifiée quand une valeur est refusée (IllegalArgumentException).
 */
public final class CatalogueService {

    private static final CatalogueService INSTANCE = new CatalogueService();

    private final List<Niveau> niveaux = new ArrayList<>();
    private int prochainIdNiveau = 1;
    private int prochainIdCours = 1;
    private int prochainIdExercice = 1;
    private int prochainIdTest = 1;
    private int prochainIdQuestion = 1;

    private CatalogueService() { }

    public static CatalogueService getInstance() { return INSTANCE; }

    /* ---------- Lecture (limitée aux niveaux organisés par l'enseignant) ---------- */

    public List<Niveau> findNiveaux(Enseignant enseignant) {
        return niveaux.stream()
                .filter(n -> enseignant.equals(n.getEnseignant()))
                .sorted(Comparator.comparingInt(Niveau::getOrdre))
                .toList();
    }

    /** Cours de tous les niveaux de l'enseignant, dans l'ordre du parcours. */
    public List<Cours> findCours(Enseignant enseignant) {
        return findNiveaux(enseignant).stream()
                .flatMap(n -> n.getCours().stream().sorted(Comparator.comparingInt(Cours::getOrdre)))
                .toList();
    }

    public List<Exercice> findExercices(Enseignant enseignant) {
        return findNiveaux(enseignant).stream().flatMap(n -> n.getExercices().stream()).toList();
    }

    public List<Test> findTests(Enseignant enseignant) {
        return findNiveaux(enseignant).stream().map(Niveau::getTest).filter(Objects::nonNull).toList();
    }

    /* ---------- Niveaux ---------- */

    public Niveau creerNiveau(Enseignant enseignant, String nom, String description, int ordre,
                              LocalDate dateDebut, LocalDate dateFin, double seuilReussite, int duree) {
        Niveau niveau = new Niveau(prochainIdNiveau, nom, description, ordre, dateDebut, dateFin, seuilReussite, duree);
        prochainIdNiveau++;
        niveaux.add(niveau);
        enseignant.organiserNiveau(niveau);
        return niveau;
    }

    public void modifierNiveau(Niveau niveau, String nom, String description, int ordre,
                               LocalDate dateDebut, LocalDate dateFin, double seuilReussite, int duree) {
        new Niveau(niveau.getIdNiveau(), nom, description, ordre, dateDebut, dateFin, seuilReussite, duree);
        niveau.setNom(nom);
        niveau.setDescription(description);
        niveau.setOrdre(ordre);
        niveau.setPeriode(dateDebut, dateFin);
        niveau.setSeuilReussite(seuilReussite);
        niveau.setDuree(duree);
    }

    /** Supprime le niveau et, par composition, ses cours, ses exercices et son test. */
    public void supprimerNiveau(Niveau niveau) {
        if (niveau.getEnseignant() != null) {
            niveau.getEnseignant().retirerNiveau(niveau);
        }
        niveaux.remove(niveau);
    }

    /* ---------- Cours ---------- */

    public Cours creerCours(Niveau niveau, String titre, String description, String contenu, int ordre) {
        Cours cours = new Cours(prochainIdCours, titre, description, contenu, ordre);
        prochainIdCours++;
        niveau.ajouterCours(cours);
        return cours;
    }

    public void modifierCours(Cours cours, Niveau niveau, String titre, String description, String contenu, int ordre) {
        new Cours(cours.getIdCours(), titre, description, contenu, ordre);
        cours.setTitre(titre);
        cours.setDescription(description);
        cours.setContenu(contenu);
        cours.setOrdre(ordre);
        if (!niveau.equals(cours.getNiveau())) {
            cours.getNiveau().retirerCours(cours);
            niveau.ajouterCours(cours);
        }
        niveau.trierCoursParOrdre();
    }

    public void supprimerCours(Cours cours) {
        cours.getNiveau().retirerCours(cours);
    }

    /** Prochain numéro d'ordre libre dans le niveau. */
    public int prochainOrdreCours(Niveau niveau) {
        return niveau.getCours().stream().mapToInt(Cours::getOrdre).max().orElse(0) + 1;
    }

    /* ---------- Exercices ---------- */

    public Exercice creerExercice(Niveau niveau, String titre, String enonce, Difficulte difficulte, double bareme) {
        Exercice exercice = new Exercice(prochainIdExercice, titre, enonce, difficulte.getLibelle(), bareme);
        prochainIdExercice++;
        niveau.ajouterExercice(exercice);
        return exercice;
    }

    public void modifierExercice(Exercice exercice, Niveau niveau, String titre, String enonce,
                                 Difficulte difficulte, double bareme) {
        new Exercice(exercice.getIdExercice(), titre, enonce, difficulte.getLibelle(), bareme);
        exercice.setTitre(titre);
        exercice.setEnonce(enonce);
        exercice.setDifficulte(difficulte.getLibelle());
        exercice.setBareme(bareme);
        if (!niveau.equals(exercice.getNiveau())) {
            exercice.getNiveau().retirerExercice(exercice);
            niveau.ajouterExercice(exercice);
        }
    }

    public void supprimerExercice(Exercice exercice) {
        exercice.getNiveau().retirerExercice(exercice);
    }

    /* ---------- Tests et questions ---------- */

    /** Un niveau possède exactement un test : cette méthode définit (ou remplace) celui du niveau. */
    public Test definirTest(Niveau niveau, String titre, String description, int duree,
                            double noteMaximale, double seuilReussite, int tentatives) {
        Test test = new Test(prochainIdTest, titre, description, duree, noteMaximale, seuilReussite, tentatives);
        prochainIdTest++;
        niveau.definirTest(test);
        return test;
    }

    public void modifierTest(Test test, String titre, String description, int duree,
                             double noteMaximale, double seuilReussite, int tentatives) {
        new Test(test.getIdTest(), titre, description, duree, noteMaximale, seuilReussite, tentatives);
        test.setTitre(titre);
        test.setDescription(description);
        test.setDuree(duree);
        test.setNoteMaximale(noteMaximale);
        test.setSeuilReussite(seuilReussite);
        test.setNombreTentativesAutorisees(tentatives);
    }

    public Question ajouterQuestion(Test test, String enonce, String type, double bareme) {
        Question question = new Question(prochainIdQuestion, enonce, type, bareme);
        prochainIdQuestion++;
        test.ajouterQuestion(question);
        return question;
    }

    public void modifierQuestion(Question question, String enonce, String type, double bareme) {
        new Question(question.getIdQuestion(), enonce, type, bareme);
        question.setEnonce(enonce);
        question.setTypeQuestion(type);
        question.setBareme(bareme);
    }

    public void supprimerQuestion(Question question) {
        question.getTest().retirerQuestion(question);
    }

    public void changerStatutCours(Cours course, StatutCours target) {
    }

    public void enregistrerCours(Cours course) {
        
    }
}
