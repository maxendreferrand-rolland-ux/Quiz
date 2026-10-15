package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.Questionnaire;
import org.example.model.Tentative;

/**
 * Résultat d'une tentative : score, pourcentage, phrase adaptée
 * et, pour un test refait, comparaison avec le score précédent.
 */
public class VueResultat extends BorderPane {

    private ApplicationQuiz application;
    private Tentative tentative;

    public VueResultat(ApplicationQuiz application, Tentative tentative, Tentative tentativePrecedente) {
        this.application = application;
        this.tentative = tentative;
        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Résultat"));

        Questionnaire questionnaire = tentative.getQuestionnaire();
        HBox criteres = new HBox(8,
                Style.etiquette(questionnaire.getLibelleCategorie(), ""),
                Style.etiquette(questionnaire.getLibelleNiveau(), "etiquette-grise"),
                Style.etiquette(tentative.getDateFormatee(), "etiquette-grise"));
        criteres.setAlignment(Pos.CENTER);

        Label score = Style.label(tentative.getNombreReponsesJustes() + " / " + tentative.getNombreQuestions(), "score");
        Label pourcentage = Style.sousTitre(tentative.getPourcentage() + " % de bonnes réponses");
        ProgressBar barre = new ProgressBar(tentative.getPourcentage() / 100.0);
        barre.setPrefWidth(360);
        Label phrase = Style.label(tentative.getPhraseResultat(
                application.getBanque().getParametres().getNomJoueur()), "phrase-resultat");

        VBox carteScore = Style.carte(criteres, score, pourcentage, barre, phrase);
        carteScore.setAlignment(Pos.CENTER);
        contenu.getChildren().add(carteScore);

        if (tentativePrecedente != null) {
            Label comparaison = new Label(tentative.getMessageProgression(tentativePrecedente));
            comparaison.setWrapText(true);
            comparaison.setMaxWidth(Double.MAX_VALUE);
            if (tentative.getPourcentage() >= tentativePrecedente.getPourcentage()) {
                comparaison.getStyleClass().add("message-succes");
            } else {
                comparaison.getStyleClass().add("message-alerte");
            }
            contenu.getChildren().add(comparaison);
        }
        if (questionnaire.estIncomplet()) {
            Label incomplet = Style.label("Ce questionnaire comportait " + questionnaire.getNombreQuestions()
                    + " question(s) au lieu des " + questionnaire.getNombreSouhaite() + " demandées.", "texte-discret");
            contenu.getChildren().add(incomplet);
        }

        Button corrige = Style.boutonPrincipal("Voir les réponses correctes");
        corrige.setOnAction(evenement -> application.afficher(new VueCorrige(application, tentative, this)));
        Button refaire = Style.boutonSecondaire("Refaire ce test");
        refaire.setOnAction(evenement -> application.lancerQuestionnaire(tentative.creerQuestionnaireRejeu(), tentative));
        Button nouveau = Style.boutonSecondaire("Nouveau questionnaire");
        nouveau.setOnAction(evenement -> this.nouveauQuestionnaire());
        HBox actions = new HBox(12, corrige, refaire, nouveau);
        actions.setAlignment(Pos.CENTER);
        contenu.getChildren().add(actions);

        this.setCenter(Style.defilable(contenu));
    }

    private void nouveauQuestionnaire() {
        if (this.tentative.getQuestionnaire().estPerfectionnement()) {
            this.application.naviguerVers(ApplicationQuiz.MENU_PERFECTIONNEMENT);
        } else {
            this.application.afficherAccueil();
        }
    }
}
