package com.pylo.domain.learning;
import java.util.Objects;
import com.pylo.domain.enums.TypeRessource;
/** Support complémentaire d'une leçon (PDF, image, vidéo, lien, fichier). */
public class Ressource {

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