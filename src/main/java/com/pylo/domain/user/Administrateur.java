package com.pylo.domain.user;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Administrateur extends Utilisateur {

    private final Map<Integer, String> roles = new HashMap<>();   // idUtilisateur -> rôle

    public Administrateur(int idUtilisateur, String nom, String prenom, String email, String mdp) {
        super(idUtilisateur, nom, prenom, email, mdp);
    }

    public void attribuerRole(Utilisateur utilisateur, String role) {
        Objects.requireNonNull(utilisateur, "L'utilisateur est obligatoire.");
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        roles.put(utilisateur.getIdUtilisateur(), role);
    }

    /** Renvoie les rôles attribués (idUtilisateur -> rôle). */
    public Map<Integer, String> gererComptes() {
        return Collections.unmodifiableMap(roles);
    }
}