package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.model.Question;
import org.example.model.Questionnaire;
import org.example.model.Tentative;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Passage d'un questionnaire : une question à la fois, propositions dans un ordre aléatoire,
 * possibilité de passer une question, de revenir en arrière et de valider à tout moment.
 */
public class VueQuestionnaire extends BorderPane {

    private ApplicationQuiz application;
    private Questionnaire questionnaire;
    private Tentative tentativePrecedente;
    private ArrayList<ArrayList<String>> ordresPropositions;
    private ArrayList<String> reponses;
    private int indexCourant;

    private Label numero;
    private Label compteur;
    private ProgressBar progression;
    private VBox zoneQuestion;
    private FlowPane pastilles;
    private Button precedente;
    private Button suivante;

    /**
     * @param application application principale
     * @param questionnaire questionnaire à faire passer
     * @param tentativePrecedente tentative refaite (comparaison des scores à la fin), ou null
     */
    public VueQuestionnaire(ApplicationQuiz application, Questionnaire questionnaire, Tentative tentativePrecedente) {
        this.application = application;
        this.questionnaire = questionnaire;
        this.tentativePrecedente = tentativePrecedente;
        this.ordresPropositions = new ArrayList<>();
        this.reponses = new ArrayList<>();
        for (Question question : questionnaire.getQuestions()) {
            ArrayList<String> propositions = question.getToutesLesPropositions();
            Collections.shuffle(propositions);
            this.ordresPropositions.add(propositions);
            this.reponses.add(null);
        }
        this.setTop(this.creerEntete());
        this.zoneQuestion = new VBox(16);
        this.zoneQuestion.getStyleClass().add("carte");
        VBox centre = new VBox(this.zoneQuestion);
        centre.setPadding(new Insets(0, 32, 16, 32));
        this.setCenter(Style.defilable(centre));
        this.setBottom(this.creerNavigation());
        this.afficherQuestion(0);
    }

    private VBox creerEntete() {
        String titre = "Questionnaire";
        if (this.tentativePrecedente != null) {
            titre = "Nouvelle tentative du test du " + this.tentativePrecedente.getDateFormatee();
        } else if (this.questionnaire.estPerfectionnement()) {
            titre = "Perfectionnement avancé";
        }
        Button abandonner = Style.boutonSecondaire("Abandonner");
        abandonner.setOnAction(evenement -> this.abandonner());
        HBox ligneTitre = new HBox(12, Style.titre(titre), Style.espace(), abandonner);
        ligneTitre.setAlignment(Pos.CENTER_LEFT);

        HBox criteres = new HBox(8,
                Style.etiquette(this.questionnaire.getLibelleCategorie(), ""),
                Style.etiquette(this.questionnaire.getLibelleNiveau(), "etiquette-grise"));
        this.numero = Style.label("", "texte-gras");
        this.compteur = Style.texteDiscret("");
        HBox ligneInfos = new HBox(12, criteres, Style.espace(), this.compteur);
        ligneInfos.setAlignment(Pos.CENTER_LEFT);

        this.progression = new ProgressBar(0);
        this.progression.setMaxWidth(Double.MAX_VALUE);

        VBox entete = new VBox(12, ligneTitre, ligneInfos, this.progression, this.numero);
        entete.setPadding(new Insets(28, 32, 14, 32));
        return entete;
    }

    private HBox creerNavigation() {
        this.precedente = Style.boutonSecondaire("← Précédente");
        this.precedente.setOnAction(evenement -> this.afficherQuestion(this.indexCourant - 1));
        this.suivante = Style.boutonSecondaire("Suivante →");
        this.suivante.setOnAction(evenement -> this.afficherQuestion(this.indexCourant + 1));
        Button valider = Style.boutonPrincipal("Valider le questionnaire");
        valider.setOnAction(evenement -> this.valider());

        this.pastilles = new FlowPane(6, 6);
        this.pastilles.setAlignment(Pos.CENTER);
        HBox.setHgrow(this.pastilles, Priority.ALWAYS);

        this.precedente.setMinWidth(Region.USE_PREF_SIZE);
        this.suivante.setMinWidth(Region.USE_PREF_SIZE);
        valider.setMinWidth(Region.USE_PREF_SIZE);
        HBox navigation = new HBox(12, this.precedente, this.pastilles, this.suivante, valider);
        navigation.setAlignment(Pos.CENTER);
        navigation.setPadding(new Insets(12, 32, 24, 32));
        return navigation;
    }

    private void afficherQuestion(int index) {
        this.indexCourant = index;
        Question question = this.questionnaire.getQuestion(index);
        this.numero.setText("Question " + (index + 1) + " sur " + this.questionnaire.getNombreQuestions());

        HBox etiquettes = new HBox(8,
                Style.etiquette(question.getCategorie().getNom(), ""),
                Style.etiquette("Niveau " + question.getNiveau(), "etiquette-grise"));
        Label libelle = Style.label(question.getLibelle(), "libelle-question");

        ToggleGroup groupe = new ToggleGroup();
        VBox propositions = new VBox(10);
        for (String proposition : this.ordresPropositions.get(index)) {
            RadioButton choix = new RadioButton(proposition);
            choix.setToggleGroup(groupe);
            choix.setWrapText(true);
            choix.setMaxWidth(Double.MAX_VALUE);
            choix.getStyleClass().add("proposition");
            if (proposition.equals(this.reponses.get(index))) {
                choix.setSelected(true);
            }
            choix.setOnAction(evenement -> {
                this.reponses.set(this.indexCourant, proposition);
                this.mettreAJourNavigation();
            });
            propositions.getChildren().add(choix);
        }

        Button effacer = Style.boutonLien("Effacer ma réponse");
        effacer.setOnAction(evenement -> {
            this.reponses.set(this.indexCourant, null);
            groupe.selectToggle(null);
            this.mettreAJourNavigation();
        });
        Label aide = Style.texteDiscret("Vous pouvez passer une question : sans réponse, elle sera comptée comme fausse.");
        HBox basQuestion = new HBox(16, effacer, Style.espace(), aide);
        basQuestion.setAlignment(Pos.CENTER_LEFT);

        this.zoneQuestion.getChildren().setAll(etiquettes, libelle, propositions, basQuestion);
        this.mettreAJourNavigation();
    }

    private int compterReponses() {
        int nombre = 0;
        for (String reponse : this.reponses) {
            if (reponse != null) {
                nombre++;
            }
        }
        return nombre;
    }

    private void mettreAJourNavigation() {
        int total = this.questionnaire.getNombreQuestions();
        this.precedente.setDisable(this.indexCourant == 0);
        this.suivante.setDisable(this.indexCourant == total - 1);
        if (this.reponses.get(this.indexCourant) == null) {
            this.suivante.setText("Passer →");
        } else {
            this.suivante.setText("Suivante →");
        }
        int repondues = this.compterReponses();
        this.progression.setProgress(repondues / (double) total);
        this.compteur.setText(repondues + " réponse(s) sur " + total);

        this.pastilles.getChildren().clear();
        for (int i = 0; i < total; i++) {
            Button pastille = new Button(String.valueOf(i + 1));
            pastille.getStyleClass().add("pastille");
            if (this.reponses.get(i) != null) {
                pastille.getStyleClass().add("pastille-repondue");
            }
            if (i == this.indexCourant) {
                pastille.getStyleClass().add("pastille-courante");
            }
            int cible = i;
            pastille.setOnAction(evenement -> this.afficherQuestion(cible));
            this.pastilles.getChildren().add(pastille);
        }
    }

    private void valider() {
        int sansReponse = this.questionnaire.getNombreQuestions() - this.compterReponses();
        if (sansReponse > 0) {
            boolean confirme = Dialogues.confirmer("Valider le questionnaire",
                    sansReponse + " question(s) sans réponse seront comptées comme fausses.\n\nValider quand même ?",
                    "Valider");
            if (!confirme) {
                return;
            }
        }
        Tentative tentative = new Tentative(this.questionnaire, this.reponses);
        this.application.getBanque().ajouterTentative(tentative);
        this.application.sauvegarder();
        this.application.terminerQuestionnaire();
        this.application.afficher(new VueResultat(this.application, tentative, this.tentativePrecedente));
    }

    private void abandonner() {
        boolean confirme = Dialogues.confirmer("Abandonner",
                "Abandonner ce questionnaire ? Vos réponses ne seront pas enregistrées.", "Abandonner");
        if (confirme) {
            this.application.terminerQuestionnaire();
            this.application.afficherAccueil();
        }
    }
}
