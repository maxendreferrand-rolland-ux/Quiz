package org.example.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Question à choix multiple : un niveau, un libellé, une proposition juste,
 * de 1 à 4 propositions fausses et un feedback facultatif.
 */
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NIVEAU_MIN = 1;
    public static final int NIVEAU_MAX = 3;
    public static final int NB_FAUSSES_MAX = 4;

    private Categorie categorie;
    private int niveau;
    private String libelle;
    private String reponseJuste;
    private ArrayList<String> reponsesFausses;
    private String feedback;

    /**
     * Crée une question après avoir vérifié toutes ses informations.
     * Les propositions fausses vides sont ignorées.
     * @param categorie catégorie de la question
     * @param niveau niveau de difficulté (1, 2 ou 3)
     * @param libelle texte de la question
     * @param reponseJuste l'unique proposition juste
     * @param reponsesFausses de 1 à 4 propositions fausses
     * @param feedback texte de correction (peut être vide ou null)
     * @throws IllegalArgumentException avec un message précis si une information est incorrecte
     */
    public Question(Categorie categorie, int niveau, String libelle, String reponseJuste,
                    ArrayList<String> reponsesFausses, String feedback) {
        ArrayList<String> faussesNettoyees = nettoyerPropositions(reponsesFausses);
        verifier(categorie, niveau, libelle, reponseJuste, faussesNettoyees);
        this.categorie = categorie;
        this.niveau = niveau;
        this.libelle = OutilsTexte.normaliser(libelle);
        this.reponseJuste = OutilsTexte.normaliser(reponseJuste);
        this.reponsesFausses = faussesNettoyees;
        if (feedback == null) {
            this.feedback = "";
        } else {
            this.feedback = feedback.trim();
        }
    }

    /* garde uniquement les propositions non vides, nettoyées */
    private static ArrayList<String> nettoyerPropositions(ArrayList<String> propositions) {
        ArrayList<String> resultat = new ArrayList<>();
        if (propositions == null) {
            return resultat;
        }
        for (String proposition : propositions) {
            if (!OutilsTexte.estVide(proposition)) {
                resultat.add(OutilsTexte.normaliser(proposition));
            }
        }
        return resultat;
    }

    private static void verifier(Categorie categorie, int niveau, String libelle, String reponseJuste,
                                 ArrayList<String> fausses) {
        if (categorie == null) {
            throw new IllegalArgumentException("La question doit appartenir à une catégorie.");
        }
        if (niveau < NIVEAU_MIN || niveau > NIVEAU_MAX) {
            throw new IllegalArgumentException("Le niveau de difficulté doit être 1, 2 ou 3 (valeur reçue : "
                    + niveau + ").");
        }
        if (OutilsTexte.estVide(libelle)) {
            throw new IllegalArgumentException("Le libellé de la question est vide.");
        }
        if (OutilsTexte.estVide(reponseJuste)) {
            throw new IllegalArgumentException("La proposition juste est vide.");
        }
        if (fausses.isEmpty()) {
            throw new IllegalArgumentException("Il faut au moins une proposition fausse.");
        }
        if (fausses.size() > NB_FAUSSES_MAX) {
            throw new IllegalArgumentException("Il y a " + fausses.size()
                    + " propositions fausses alors que 4 au maximum sont autorisées.");
        }
        ArrayList<String> dejaVues = new ArrayList<>();
        dejaVues.add(OutilsTexte.normaliserSansCasse(reponseJuste));
        for (String fausse : fausses) {
            String fausseComparable = OutilsTexte.normaliserSansCasse(fausse);
            if (dejaVues.contains(fausseComparable)) {
                throw new IllegalArgumentException("La proposition « " + fausse
                        + " » apparaît plusieurs fois dans la question.");
            }
            dejaVues.add(fausseComparable);
        }
    }

    public Categorie getCategorie() {
        return this.categorie;
    }

    /* réservé à BanqueDeDonnees (déplacement d'une question) */
    void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public int getNiveau() {
        return this.niveau;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public String getReponseJuste() {
        return this.reponseJuste;
    }

    /**
     * @return une copie de la liste des propositions fausses
     */
    public ArrayList<String> getReponsesFausses() {
        return new ArrayList<>(this.reponsesFausses);
    }

    public String getFeedback() {
        return this.feedback;
    }

    public boolean aUnFeedback() {
        return !this.feedback.isEmpty();
    }

    /**
     * @return la proposition juste suivie des propositions fausses
     */
    public ArrayList<String> getToutesLesPropositions() {
        ArrayList<String> propositions = new ArrayList<>();
        propositions.add(this.reponseJuste);
        propositions.addAll(this.reponsesFausses);
        return propositions;
    }

    /**
     * @param reponse réponse donnée par le joueur (null si pas de réponse)
     * @return true si la réponse est la proposition juste
     */
    public boolean estReponseJuste(String reponse) {
        if (reponse == null) {
            return false;
        }
        return OutilsTexte.normaliser(reponse).equals(this.reponseJuste);
    }

    /**
     * Deux questions sont identiques si elles ont la même catégorie (casse et espaces ignorés),
     * le même libellé et le même ensemble de propositions. Le niveau et le feedback n'interviennent pas.
     * @param autre question à comparer
     * @return true si les questions sont identiques
     */
    public boolean estIdentique(Question autre) {
        if (autre == null) {
            return false;
        }
        if (!this.categorie.aLeMemeNomQue(autre.categorie.getNom())) {
            return false;
        }
        if (!this.libelle.equals(autre.libelle)) {
            return false;
        }
        ArrayList<String> mesPropositions = this.getToutesLesPropositions();
        ArrayList<String> autresPropositions = autre.getToutesLesPropositions();
        if (mesPropositions.size() != autresPropositions.size()) {
            return false;
        }
        for (String proposition : mesPropositions) {
            if (!autresPropositions.contains(proposition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copie indépendante de la question (y compris sa catégorie),
     * utilisée pour figer les questions d'une tentative dans l'historique.
     * @return la copie
     */
    public Question copier() {
        return new Question(new Categorie(this.categorie.getNom()), this.niveau, this.libelle,
                this.reponseJuste, new ArrayList<>(this.reponsesFausses), this.feedback);
    }

    /* réservé à BanqueDeDonnees : recopie toutes les informations d'une autre question */
    void modifierDepuis(Question source, Categorie nouvelleCategorie) {
        this.categorie = nouvelleCategorie;
        this.niveau = source.niveau;
        this.libelle = source.libelle;
        this.reponseJuste = source.reponseJuste;
        this.reponsesFausses = new ArrayList<>(source.reponsesFausses);
        this.feedback = source.feedback;
    }

    @Override
    public String toString() {
        return this.libelle;
    }
}
