package org.example.model;

import java.io.Serializable;

/**
 * Regroupement (éventuellement vide) de questions.
 * La catégorie « Général » existe toujours : elle ne peut être ni renommée ni supprimée.
 */
public class Categorie implements Serializable {

    private static final long serialVersionUID = 1L;

    /** nom de la catégorie présente par défaut */
    public static final String NOM_GENERALE = "Général";

    /** longueur maximale d'un nom de catégorie */
    public static final int LONGUEUR_MAX_NOM = 60;

    private String nom;

    /**
     * @param nom nom de la catégorie
     * @throws IllegalArgumentException si le nom est vide ou trop long
     */
    public Categorie(String nom) {
        this.nom = verifierNom(nom);
    }

    /**
     * Vérifie et nettoie un nom de catégorie.
     * @param nom nom proposé
     * @return le nom nettoyé
     * @throws IllegalArgumentException si le nom est vide ou trop long
     */
    public static String verifierNom(String nom) {
        String nomNettoye = OutilsTexte.normaliser(nom);
        if (nomNettoye.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la catégorie est vide.");
        }
        if (nomNettoye.length() > LONGUEUR_MAX_NOM) {
            throw new IllegalArgumentException("Le nom de la catégorie ne doit pas dépasser "
                    + LONGUEUR_MAX_NOM + " caractères.");
        }
        return nomNettoye;
    }

    public String getNom() {
        return this.nom;
    }

    /* réservé à BanqueDeDonnees qui vérifie l'absence d'homonyme */
    void renommer(String nouveauNom) {
        this.nom = verifierNom(nouveauNom);
    }

    /**
     * @return true s'il s'agit de la catégorie « Général »
     */
    public boolean estGenerale() {
        return aLeMemeNomQue(NOM_GENERALE);
    }

    /**
     * Compare les noms sans tenir compte de la casse ni des espaces superflus.
     * @param autreNom nom à comparer
     * @return true si les deux noms sont considérés comme identiques
     */
    public boolean aLeMemeNomQue(String autreNom) {
        return OutilsTexte.normaliserSansCasse(this.nom).equals(OutilsTexte.normaliserSansCasse(autreNom));
    }

    @Override
    public String toString() {
        return this.nom;
    }
}
