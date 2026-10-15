package org.example.service;

import java.util.ArrayList;

/**
 * Compte rendu d'une importation de questions (fichier CSV local ou réception réseau).
 */
public class RapportImport {

    private int lignesLues;
    private int questionsImportees;
    private ArrayList<String> categoriesCreees;
    private ArrayList<String> doublons;
    private ArrayList<String> erreurs;
    private String erreurFatale;
    private boolean enTeteIgnoree;

    public RapportImport() {
        this.lignesLues = 0;
        this.questionsImportees = 0;
        this.categoriesCreees = new ArrayList<>();
        this.doublons = new ArrayList<>();
        this.erreurs = new ArrayList<>();
        this.erreurFatale = null;
        this.enTeteIgnoree = false;
    }

    public void incrementerLignesLues() {
        this.lignesLues++;
    }

    public void incrementerQuestionsImportees() {
        this.questionsImportees++;
    }

    public void ajouterCategorieCreee(String nom) {
        this.categoriesCreees.add(nom);
    }

    public void ajouterDoublon(int numeroLigne, String description) {
        this.doublons.add("Ligne " + numeroLigne + " : " + description);
    }

    public void ajouterErreur(int numeroLigne, String cause) {
        this.erreurs.add("Ligne " + numeroLigne + " : " + cause);
    }

    public void setErreurFatale(String erreurFatale) {
        this.erreurFatale = erreurFatale;
    }

    public void setEnTeteIgnoree(boolean enTeteIgnoree) {
        this.enTeteIgnoree = enTeteIgnoree;
    }

    public int getLignesLues() {
        return this.lignesLues;
    }

    public int getQuestionsImportees() {
        return this.questionsImportees;
    }

    public ArrayList<String> getCategoriesCreees() {
        return new ArrayList<>(this.categoriesCreees);
    }

    public ArrayList<String> getDoublons() {
        return new ArrayList<>(this.doublons);
    }

    public ArrayList<String> getErreurs() {
        return new ArrayList<>(this.erreurs);
    }

    public String getErreurFatale() {
        return this.erreurFatale;
    }

    public boolean estEnTeteIgnoree() {
        return this.enTeteIgnoree;
    }

    /**
     * @return true si l'importation n'a rencontré aucune erreur (les doublons ne sont pas des erreurs)
     */
    public boolean estSansErreur() {
        return this.erreurFatale == null && this.erreurs.isEmpty();
    }

    /**
     * @return résumé en une phrase
     */
    public String getResume() {
        if (this.erreurFatale != null) {
            return "Échec de l'importation : " + this.erreurFatale;
        }
        return this.questionsImportees + " question(s) importée(s), "
                + this.categoriesCreees.size() + " catégorie(s) créée(s), "
                + this.doublons.size() + " doublon(s) ignoré(s), "
                + this.erreurs.size() + " ligne(s) en erreur.";
    }

    /**
     * @return compte rendu détaillé sur plusieurs lignes
     */
    public String getDetail() {
        String texte = this.getResume() + "\n";
        if (this.erreurFatale != null) {
            return texte;
        }
        texte = texte + "Lignes de questions lues : " + this.lignesLues + "\n";
        if (this.enTeteIgnoree) {
            texte = texte + "La première ligne a été reconnue comme une ligne d'en-tête et ignorée.\n";
        }
        if (!this.categoriesCreees.isEmpty()) {
            texte = texte + "\nCatégories créées automatiquement :\n";
            for (String nom : this.categoriesCreees) {
                texte = texte + "  • " + nom + "\n";
            }
        }
        if (!this.doublons.isEmpty()) {
            texte = texte + "\nQuestions déjà présentes (non importées) :\n";
            for (String doublon : this.doublons) {
                texte = texte + "  • " + doublon + "\n";
            }
        }
        if (!this.erreurs.isEmpty()) {
            texte = texte + "\nLignes en erreur (non importées) :\n";
            for (String erreur : this.erreurs) {
                texte = texte + "  • " + erreur + "\n";
            }
        }
        return texte;
    }
}
