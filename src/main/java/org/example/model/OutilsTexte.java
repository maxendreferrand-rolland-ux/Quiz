package org.example.model;

/**
 * Petites fonctions de nettoyage de texte utilisées pour comparer
 * les noms de catégories, les libellés et les propositions.
 */
public class OutilsTexte {

    private OutilsTexte() {
    }

    /**
     * Supprime les espaces en début et fin de texte et remplace
     * toute suite d'espaces (y compris insécables) par un seul espace.
     * @param texte texte à nettoyer (peut être null)
     * @return le texte nettoyé, jamais null
     */
    public static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        String resultat = texte.replace(' ', ' ').replace(' ', ' ');
        return resultat.trim().replaceAll("\\s+", " ");
    }

    /**
     * Comme normaliser, mais en ignorant aussi la casse.
     * @param texte texte à nettoyer (peut être null)
     * @return le texte nettoyé en minuscules
     */
    public static String normaliserSansCasse(String texte) {
        return normaliser(texte).toLowerCase();
    }

    /**
     * @param texte texte à tester
     * @return true si le texte est null ou ne contient que des espaces
     */
    public static boolean estVide(String texte) {
        return normaliser(texte).isEmpty();
    }
}
