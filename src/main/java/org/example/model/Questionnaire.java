package org.example.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Ensemble de questions répondant à des critères (niveau, catégorie, nombre souhaité).
 */
public class Questionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    /** valeur du niveau quand le joueur n'a pas choisi de difficulté */
    public static final int NIVEAU_INDIFFERENT = 0;

    private int niveau;
    private String nomCategorie;
    private int nombreSouhaite;
    private boolean perfectionnement;
    private ArrayList<Question> questions;

    /**
     * @param niveau 1, 2, 3 ou NIVEAU_INDIFFERENT
     * @param nomCategorie nom de la catégorie choisie, null si toutes les catégories
     * @param nombreSouhaite nombre de questions demandé par le joueur
     * @param perfectionnement true pour un questionnaire du mode perfectionnement avancé
     * @param questions questions du questionnaire
     */
    public Questionnaire(int niveau, String nomCategorie, int nombreSouhaite, boolean perfectionnement,
                         ArrayList<Question> questions) {
        this.niveau = niveau;
        this.nomCategorie = nomCategorie;
        this.nombreSouhaite = nombreSouhaite;
        this.perfectionnement = perfectionnement;
        this.questions = new ArrayList<>(questions);
    }

    public int getNiveau() {
        return this.niveau;
    }

    public String getNomCategorie() {
        return this.nomCategorie;
    }

    public int getNombreSouhaite() {
        return this.nombreSouhaite;
    }

    public boolean estPerfectionnement() {
        return this.perfectionnement;
    }

    public ArrayList<Question> getQuestions() {
        return new ArrayList<>(this.questions);
    }

    public Question getQuestion(int index) {
        return this.questions.get(index);
    }

    public int getNombreQuestions() {
        return this.questions.size();
    }

    /**
     * @return true si la banque n'a pas permis d'atteindre le nombre de questions souhaité
     */
    public boolean estIncomplet() {
        return this.questions.size() < this.nombreSouhaite;
    }

    /**
     * @return texte décrivant le niveau, pour l'affichage
     */
    public String getLibelleNiveau() {
        if (this.niveau == NIVEAU_INDIFFERENT) {
            return "Indifférent";
        }
        return "Niveau " + this.niveau;
    }

    /**
     * @return texte décrivant la catégorie, pour l'affichage
     */
    public String getLibelleCategorie() {
        if (this.perfectionnement) {
            return "Perfectionnement";
        }
        if (this.nomCategorie == null) {
            return "Toutes catégories";
        }
        return this.nomCategorie;
    }

    /**
     * @return copie indépendante du questionnaire et de toutes ses questions
     */
    public Questionnaire copier() {
        ArrayList<Question> copies = new ArrayList<>();
        for (Question question : this.questions) {
            copies.add(question.copier());
        }
        return new Questionnaire(this.niveau, this.nomCategorie, this.nombreSouhaite,
                this.perfectionnement, copies);
    }
}
