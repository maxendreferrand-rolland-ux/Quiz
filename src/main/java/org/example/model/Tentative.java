package org.example.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Questionnaire auquel le joueur a répondu.
 * Les questions sont copiées au moment de l'enregistrement : la tentative reste
 * consultable et rejouable même si les questions sont ensuite modifiées ou supprimées.
 */
public class Tentative implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private Questionnaire questionnaire;
    private ArrayList<String> reponses;
    private LocalDateTime date;

    /**
     * @param questionnaire questionnaire joué (il est copié)
     * @param reponses réponse donnée à chaque question, null si pas de réponse
     * @throws IllegalArgumentException si le nombre de réponses ne correspond pas
     */
    public Tentative(Questionnaire questionnaire, ArrayList<String> reponses) {
        this(questionnaire, reponses, LocalDateTime.now());
    }

    /**
     * Constructeur permettant de fixer la date (utile pour les tests).
     */
    public Tentative(Questionnaire questionnaire, ArrayList<String> reponses, LocalDateTime date) {
        if (questionnaire == null || reponses == null
                || reponses.size() != questionnaire.getNombreQuestions()) {
            throw new IllegalArgumentException("Il faut exactement une réponse (éventuellement vide) par question.");
        }
        this.questionnaire = questionnaire.copier();
        this.reponses = new ArrayList<>(reponses);
        this.date = date;
    }

    public Questionnaire getQuestionnaire() {
        return this.questionnaire;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public String getDateFormatee() {
        return this.date.format(FORMAT_DATE);
    }

    public int getNombreQuestions() {
        return this.questionnaire.getNombreQuestions();
    }

    /**
     * @param index numéro de la question (à partir de 0)
     * @return la réponse donnée, ou null si le joueur n'a pas répondu
     */
    public String getReponse(int index) {
        return this.reponses.get(index);
    }

    public boolean estSansReponse(int index) {
        return this.reponses.get(index) == null;
    }

    public boolean estJuste(int index) {
        return this.questionnaire.getQuestion(index).estReponseJuste(this.reponses.get(index));
    }

    public int getNombreReponsesJustes() {
        int nombre = 0;
        for (int i = 0; i < this.getNombreQuestions(); i++) {
            if (this.estJuste(i)) {
                nombre++;
            }
        }
        return nombre;
    }

    /**
     * @return pourcentage de bonnes réponses arrondi à l'entier
     */
    public int getPourcentage() {
        if (this.getNombreQuestions() == 0) {
            return 0;
        }
        return (int) Math.round(this.getNombreReponsesJustes() * 100.0 / this.getNombreQuestions());
    }

    /**
     * @return questionnaire identique (copie) pour refaire la tentative
     */
    public Questionnaire creerQuestionnaireRejeu() {
        return this.questionnaire.copier();
    }

    /**
     * @param nomJoueur nom du joueur (peut être vide)
     * @return phrase adaptée au score obtenu
     */
    public String getPhraseResultat(String nomJoueur) {
        String nom = "";
        if (!OutilsTexte.estVide(nomJoueur)) {
            nom = " " + OutilsTexte.normaliser(nomJoueur);
        }
        int justes = this.getNombreReponsesJustes();
        int total = this.getNombreQuestions();
        if (total == 0) {
            return "Ce questionnaire ne contenait aucune question.";
        }
        if (justes == total) {
            return "Félicitations" + nom + " ! Toutes vos réponses sont justes !";
        }
        if (justes * 2 > total) {
            return "Bravo" + nom + " ! Plus de la moitié de vos réponses sont justes !";
        }
        if (justes * 2 == total) {
            return "Pas mal" + nom + " ! La moitié de vos réponses sont justes, encore un petit effort !";
        }
        if (justes > 0) {
            return "Courage" + nom + " ! Moins de la moitié de vos réponses sont justes. "
                    + "Le corrigé vous aidera à progresser.";
        }
        return "Dommage" + nom + ", aucune réponse juste cette fois. Consultez le corrigé et retentez votre chance !";
    }

    /**
     * Message comparant cette tentative à une tentative antérieure du même test.
     * @param precedente tentative refaite
     * @return message de progression, de régression ou de stabilité
     */
    public String getMessageProgression(Tentative precedente) {
        String message = "Score précédent (test du " + precedente.getDateFormatee() + ") : "
                + precedente.getNombreReponsesJustes() + "/" + precedente.getNombreQuestions()
                + " (" + precedente.getPourcentage() + " %). ";
        int ecart = this.getPourcentage() - precedente.getPourcentage();
        if (ecart > 0) {
            return message + "Progression de " + ecart + " point(s), bravo !";
        }
        if (ecart < 0) {
            return message + "Régression de " + (-ecart) + " point(s) : relisez le corrigé et réessayez.";
        }
        return message + "Score identique à la tentative précédente.";
    }
}
