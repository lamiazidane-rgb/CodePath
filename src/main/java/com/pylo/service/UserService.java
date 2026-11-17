package com.pylo.service;

import com.pylo.domain.user.Utilisateur;
import com.pylo.navigation.Role;

/** Accès temporaire aux utilisateurs du domaine. */
public interface UserService {
    Utilisateur findByRole(Role role);

    static UserService inMemory() {
        return role -> switch (role) {
            case ADMIN -> new com.pylo.domain.user.Administrateur(
                    3, "Durand", "Alex", "alex.durand@pylo.fr", "pylo123");
            case TEACHER -> new com.pylo.domain.user.Enseignant(
                    2, "Martin", "Sophie", "sophie.martin@pylo.fr", "pylo123");
            case LEARNER -> new com.pylo.domain.user.Apprenant(
                    1, "Dupont", "Lina", "lina.dupont@pylo.fr", "pylo123");
        };
    }
}