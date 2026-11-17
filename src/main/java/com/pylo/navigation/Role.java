package com.pylo.navigation;

import com.pylo.domain.user.Administrateur;
import com.pylo.domain.user.Apprenant;
import com.pylo.domain.user.Enseignant;
import com.pylo.domain.user.Utilisateur;

import java.util.function.Predicate;

/** Rôles d'accès correspondant aux trois spécialisations du domaine utilisateur. */
public enum Role {
    ADMIN("Administrateur", "Administration", "Espace administrateur",
            user -> user instanceof Administrateur),
    TEACHER("Enseignant", "Enseignement", "Espace enseignant",
            user -> user instanceof Enseignant),
    LEARNER("Apprenant", "Apprentissage", "Espace apprenant",
            user -> user instanceof Apprenant);

    private final String label;
    private final String spaceTitle;
    private final String subtitle;
    private final Predicate<Utilisateur> matcher;

    Role(String label, String spaceTitle, String subtitle, Predicate<Utilisateur> matcher) {
        this.label = label;
        this.spaceTitle = spaceTitle;
        this.subtitle = subtitle;
        this.matcher = matcher;
    }

    public String getLabel() { return label; }
    public String getSpaceTitle() { return spaceTitle; }
    public String getSubtitle() { return subtitle; }
    public boolean matches(Utilisateur user) { return matcher.test(user); }
}
