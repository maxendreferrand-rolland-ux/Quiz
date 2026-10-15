package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.Question;
import org.example.model.Tentative;

/**
 * Corrigé d'une tentative : pour chaque question, la réponse donnée,
 * la bonne réponse et le feedback s'il a été renseigné.
 */
public class VueCorrige extends BorderPane {

    /**
     * @param application application principale
     * @param tentative tentative à corriger
     * @param vueRetour vue à réafficher avec le bouton Retour
     */
    public VueCorrige(ApplicationQuiz application, Tentative tentative, Node vueRetour) {
        VBox contenu = Style.vue();

        Button retour = Style.boutonSecondaire("← Retour");
        retour.setOnAction(evenement -> application.afficher(vueRetour));
        HBox entete = new HBox(16, retour, Style.titre("Corrigé"));
        entete.setAlignment(Pos.CENTER_LEFT);
        contenu.getChildren().add(entete);
        contenu.getChildren().add(Style.sousTitre("Test du " + tentative.getDateFormatee() + " · "
                + tentative.getQuestionnaire().getLibelleCategorie() + " · "
                + tentative.getNombreReponsesJustes() + "/" + tentative.getNombreQuestions()
                + " (" + tentative.getPourcentage() + " %)"));

        for (int i = 0; i < tentative.getNombreQuestions(); i++) {
            contenu.getChildren().add(this.creerCarteQuestion(tentative, i));
        }
        this.setCenter(Style.defilable(contenu));
    }

    private VBox creerCarteQuestion(Tentative tentative, int index) {
        Question question = tentative.getQuestionnaire().getQuestion(index);
        boolean juste = tentative.estJuste(index);

        Label verdict;
        if (juste) {
            verdict = Style.etiquette("Bonne réponse", "etiquette-verte");
        } else if (tentative.estSansReponse(index)) {
            verdict = Style.etiquette("Sans réponse", "etiquette-rouge");
        } else {
            verdict = Style.etiquette("Mauvaise réponse", "etiquette-rouge");
        }
        HBox entete = new HBox(8, Style.label("Question " + (index + 1), "texte-gras"), verdict, Style.espace(),
                Style.texteDiscret(question.getCategorie().getNom() + " · niveau " + question.getNiveau()));
        entete.setAlignment(Pos.CENTER_LEFT);

        Label libelle = Style.label(question.getLibelle(), "titre-carte");
        VBox carte = Style.carte(entete, libelle);

        String reponseDonnee = tentative.getReponse(index);
        if (reponseDonnee == null) {
            reponseDonnee = "aucune réponse";
        }
        Label votreReponse = new Label("Votre réponse : " + reponseDonnee);
        votreReponse.setWrapText(true);
        if (juste) {
            votreReponse.getStyleClass().add("reponse-juste");
            carte.getStyleClass().add("carte-juste");
        } else {
            votreReponse.getStyleClass().add("reponse-fausse");
            carte.getStyleClass().add("carte-fausse");
        }
        carte.getChildren().add(votreReponse);
        if (!juste) {
            carte.getChildren().add(Style.label("Bonne réponse : " + question.getReponseJuste(), "reponse-juste"));
        }
        if (question.aUnFeedback()) {
            Label feedback = Style.label(question.getFeedback(), "feedback");
            feedback.setMaxWidth(Double.MAX_VALUE);
            carte.getChildren().add(feedback);
        }
        return carte;
    }
}
