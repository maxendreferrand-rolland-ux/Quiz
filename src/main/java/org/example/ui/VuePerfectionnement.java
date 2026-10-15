package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Questionnaire;

/**
 * Mode « Perfectionnement avancé » : questions tirées parmi celles de l'historique
 * auxquelles le joueur a mal répondu (ou pas répondu) lors de sa dernière rencontre avec elles.
 */
public class VuePerfectionnement extends BorderPane {

    private static final int[] NOMBRES = {5, 10, 20, 30};

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private SelecteurSegmente choixNiveau;
    private SelecteurSegmente choixNombre;
    private Label disponibles;

    public VuePerfectionnement(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();
        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Perfectionnement avancé"));
        contenu.getChildren().add(Style.sousTitre("Retravaillez les questions sur lesquelles vous vous êtes trompé. "
                + "Une question sort de cette liste dès que vous y répondez correctement."));

        int total = this.banque.getQuestionsARetravailler(Questionnaire.NIVEAU_INDIFFERENT).size();
        HBox tuiles = new HBox(14);
        tuiles.getChildren().add(Style.tuile(String.valueOf(total), "questions à retravailler"));
        for (int niveau = 1; niveau <= 3; niveau++) {
            tuiles.getChildren().add(Style.tuile(String.valueOf(this.banque.getQuestionsARetravailler(niveau).size()),
                    "de niveau " + niveau));
        }
        contenu.getChildren().add(tuiles);

        if (total == 0) {
            Label vide = Style.label("Aucune erreur à retravailler dans l'historique pour le moment. "
                    + "Faites quelques questionnaires depuis l'accueil, puis revenez ici.", "message-info");
            vide.setMaxWidth(Double.MAX_VALUE);
            contenu.getChildren().add(vide);
        } else {
            contenu.getChildren().add(this.creerCartePreparation());
        }
        this.setCenter(Style.defilable(contenu));
    }

    private VBox creerCartePreparation() {
        this.choixNiveau = new SelecteurSegmente(new String[]{"Indifférent", "1 · Facile", "2 · Moyen", "3 · Difficile"}, 0);
        this.choixNombre = new SelecteurSegmente(new String[]{"5", "10", "20", "30"}, 0);
        GridPane grille = new GridPane();
        grille.setHgap(18);
        grille.setVgap(14);
        grille.add(Style.label("Difficulté", "texte-gras"), 0, 0);
        grille.add(this.choixNiveau, 1, 0);
        grille.add(Style.label("Nombre de questions", "texte-gras"), 0, 1);
        grille.add(this.choixNombre, 1, 1);

        this.disponibles = Style.texteDiscret("");
        this.choixNiveau.setActionChangement(this::mettreAJourDisponibles);
        this.mettreAJourDisponibles();

        Button lancer = Style.boutonPrincipal("Lancer le perfectionnement");
        lancer.setDefaultButton(true);
        lancer.setOnAction(evenement -> this.lancer());
        HBox actions = new HBox(16, lancer, this.disponibles);
        actions.setAlignment(Pos.CENTER_LEFT);
        return Style.carte(Style.titreCarte("Nouvelle séance"), grille, actions);
    }

    private void mettreAJourDisponibles() {
        int nombre = this.banque.getQuestionsARetravailler(this.choixNiveau.getIndexSelectionne()).size();
        this.disponibles.setText(nombre + " question(s) à retravailler pour ce niveau.");
    }

    private void lancer() {
        int niveau = this.choixNiveau.getIndexSelectionne();
        int nombre = NOMBRES[this.choixNombre.getIndexSelectionne()];
        int disponiblesNiveau = this.banque.getQuestionsARetravailler(niveau).size();
        if (disponiblesNiveau == 0) {
            Dialogues.information("Aucune question", "Aucune question à retravailler pour ce niveau de difficulté.");
            return;
        }
        if (disponiblesNiveau < nombre) {
            boolean poursuivre = Dialogues.confirmer("Questions insuffisantes",
                    "Vous avez demandé " + nombre + " questions, mais seulement " + disponiblesNiveau
                            + " question(s) sont à retravailler.\n\nVoulez-vous poursuivre avec "
                            + disponiblesNiveau + " question(s) ?",
                    "Poursuivre");
            if (!poursuivre) {
                return;
            }
        }
        this.application.lancerQuestionnaire(this.banque.creerQuestionnairePerfectionnement(niveau, nombre), null);
    }
}
