package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.OutilsTexte;
import org.example.model.Question;

import java.util.ArrayList;

/**
 * Gestion des catégories (à gauche) et des questions de la catégorie sélectionnée (à droite),
 * avec le détail de la question sélectionnée.
 */
public class VueGestion extends BorderPane {

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;

    private ListView<Categorie> listeCategories;
    private Button boutonRenommer;
    private Button boutonSupprimerCategorie;

    private Label titreQuestions;
    private TextField filtre;
    private ListView<Question> listeQuestions;
    private Button boutonModifier;
    private Button boutonSupprimerQuestion;
    private VBox detail;

    public VueGestion(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();

        VBox entete = new VBox(6, Style.titre("Catégories & questions"),
                Style.sousTitre("Sélectionnez une catégorie pour consulter, créer, modifier ou supprimer ses questions. "
                        + "Double-cliquez sur une question pour la modifier."));
        entete.setPadding(new Insets(28, 32, 16, 32));
        this.setTop(entete);

        HBox colonnes = new HBox(18, this.creerColonneCategories(), this.creerColonneQuestions());
        colonnes.setPadding(new Insets(0, 32, 28, 32));
        this.setCenter(colonnes);

        this.rafraichir(this.banque.getCategorieGenerale(), null);
    }

    private VBox creerColonneCategories() {
        this.listeCategories = new ListView<>();
        this.listeCategories.setCellFactory(liste -> new ListCell<Categorie>() {
            @Override
            protected void updateItem(Categorie categorie, boolean vide) {
                super.updateItem(categorie, vide);
                if (vide || categorie == null) {
                    this.setText(null);
                } else {
                    this.setText(categorie.getNom() + "  (" + VueGestion.this.banque.compterQuestions(categorie) + ")");
                }
            }
        });
        this.listeCategories.getSelectionModel().selectedItemProperty().addListener(
                (observable, ancienne, nouvelle) -> this.afficherQuestionsDe(nouvelle));
        VBox.setVgrow(this.listeCategories, Priority.ALWAYS);

        Button nouvelle = Style.boutonPrincipal("Nouvelle");
        nouvelle.setOnAction(evenement -> this.creerCategorie());
        this.boutonRenommer = Style.boutonSecondaire("Renommer");
        this.boutonRenommer.setOnAction(evenement -> this.renommerCategorie());
        this.boutonSupprimerCategorie = Style.boutonDanger("Supprimer");
        this.boutonSupprimerCategorie.setOnAction(evenement -> this.supprimerCategorie());
        FlowPane actions = new FlowPane(8, 8, nouvelle, this.boutonRenommer, this.boutonSupprimerCategorie);

        VBox colonne = Style.carte(Style.titreCarte("Catégories"), this.listeCategories, actions,
                Style.texteDiscret("« Général » ne peut être ni renommée ni supprimée."));
        colonne.setPrefWidth(340);
        colonne.setMinWidth(300);
        return colonne;
    }

    private VBox creerColonneQuestions() {
        this.titreQuestions = Style.titreCarte("");
        this.filtre = new TextField();
        this.filtre.setPromptText("Filtrer les questions…");
        this.filtre.textProperty().addListener((observable, ancien, nouveau) -> this.remplirQuestions(null));

        this.listeQuestions = new ListView<>();
        this.listeQuestions.setCellFactory(liste -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question question, boolean vide) {
                super.updateItem(question, vide);
                this.setPrefWidth(0);
                if (vide || question == null) {
                    this.setText(null);
                } else {
                    this.setText("[Niveau " + question.getNiveau() + "]  " + question.getLibelle());
                }
            }
        });
        this.listeQuestions.getSelectionModel().selectedItemProperty().addListener(
                (observable, ancienne, nouvelle) -> this.afficherDetail(nouvelle));
        this.listeQuestions.setOnMouseClicked(evenement -> {
            if (evenement.getClickCount() == 2 && this.listeQuestions.getSelectionModel().getSelectedItem() != null) {
                this.modifierQuestion();
            }
        });
        this.listeQuestions.setPrefHeight(240);
        VBox.setVgrow(this.listeQuestions, Priority.ALWAYS);

        Button nouvelle = Style.boutonPrincipal("Nouvelle question");
        nouvelle.setOnAction(evenement -> this.creerQuestion());
        this.boutonModifier = Style.boutonSecondaire("Modifier");
        this.boutonModifier.setOnAction(evenement -> this.modifierQuestion());
        this.boutonSupprimerQuestion = Style.boutonDanger("Supprimer");
        this.boutonSupprimerQuestion.setOnAction(evenement -> this.supprimerQuestion());
        HBox actions = new HBox(8, nouvelle, this.boutonModifier, this.boutonSupprimerQuestion);

        this.detail = new VBox(8);
        this.detail.setStyle("-fx-background-color: #f6f7fb; -fx-background-radius: 10; -fx-padding: 14;");

        VBox colonne = Style.carte(this.titreQuestions, this.filtre, this.listeQuestions, actions, this.detail);
        HBox.setHgrow(colonne, Priority.ALWAYS);
        return colonne;
    }

    /**
     * Recharge les listes et sélectionne la catégorie et la question indiquées.
     */
    private void rafraichir(Categorie categorieASelectionner, Question questionASelectionner) {
        Categorie categorie = categorieASelectionner;
        if (categorie == null) {
            categorie = this.banque.getCategorieGenerale();
        }
        this.listeCategories.getItems().setAll(this.banque.getCategories());
        this.listeCategories.getSelectionModel().select(categorie);
        this.listeCategories.refresh();
        this.afficherQuestionsDe(categorie);
        this.remplirQuestions(questionASelectionner);
    }

    private Categorie getCategorieSelectionnee() {
        return this.listeCategories.getSelectionModel().getSelectedItem();
    }

    private Question getQuestionSelectionnee() {
        return this.listeQuestions.getSelectionModel().getSelectedItem();
    }

    private void afficherQuestionsDe(Categorie categorie) {
        boolean modifiable = categorie != null && !categorie.estGenerale();
        this.boutonRenommer.setDisable(!modifiable);
        this.boutonSupprimerCategorie.setDisable(!modifiable);
        if (categorie == null) {
            this.titreQuestions.setText("Aucune catégorie sélectionnée");
        } else {
            this.titreQuestions.setText("Questions de « " + categorie.getNom() + " »");
        }
        this.remplirQuestions(null);
    }

    private void remplirQuestions(Question questionASelectionner) {
        Categorie categorie = this.getCategorieSelectionnee();
        ArrayList<Question> affichees = new ArrayList<>();
        if (categorie != null) {
            String recherche = OutilsTexte.normaliserSansCasse(this.filtre.getText());
            for (Question question : this.banque.getQuestionsDeCategorie(categorie)) {
                if (recherche.isEmpty() || question.getLibelle().toLowerCase().contains(recherche)) {
                    affichees.add(question);
                }
            }
        }
        this.listeQuestions.getItems().setAll(affichees);
        if (questionASelectionner != null) {
            this.listeQuestions.getSelectionModel().select(questionASelectionner);
        }
        this.afficherDetail(this.getQuestionSelectionnee());
    }

    private void afficherDetail(Question question) {
        this.boutonModifier.setDisable(question == null);
        this.boutonSupprimerQuestion.setDisable(question == null);
        this.detail.getChildren().clear();
        if (question == null) {
            this.detail.getChildren().add(Style.texteDiscret("Sélectionnez une question pour afficher son détail."));
            return;
        }
        GridPane grille = new GridPane();
        grille.setHgap(14);
        grille.setVgap(6);
        String fausses = "";
        for (String fausse : question.getReponsesFausses()) {
            if (!fausses.isEmpty()) {
                fausses = fausses + "\n";
            }
            fausses = fausses + "• " + fausse;
        }
        String feedback = question.getFeedback();
        if (feedback.isEmpty()) {
            feedback = "(aucun)";
        }
        this.ajouterLigne(grille, 0, "Catégorie", question.getCategorie().getNom());
        this.ajouterLigne(grille, 1, "Niveau", String.valueOf(question.getNiveau()));
        this.ajouterLigne(grille, 2, "Libellé", question.getLibelle());
        this.ajouterLigne(grille, 3, "Proposition juste", question.getReponseJuste());
        this.ajouterLigne(grille, 4, "Propositions fausses", fausses);
        this.ajouterLigne(grille, 5, "Feedback", feedback);
        this.detail.getChildren().add(grille);
    }

    private void ajouterLigne(GridPane grille, int ligne, String intitule, String valeur) {
        Label etiquette = Style.label(intitule, "texte-gras");
        etiquette.setMinWidth(150);
        etiquette.setAlignment(Pos.TOP_LEFT);
        grille.add(etiquette, 0, ligne);
        Label contenu = new Label(valeur);
        contenu.setWrapText(true);
        grille.add(contenu, 1, ligne);
    }

    /* ---------------------------------------------------------------- catégories */

    private void creerCategorie() {
        String nom = Dialogues.saisir("Nouvelle catégorie", "Nom de la catégorie :", "");
        if (nom == null) {
            return;
        }
        try {
            Categorie categorie = this.banque.creerCategorie(nom);
            this.application.sauvegarder();
            this.rafraichir(categorie, null);
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Catégorie non créée", e.getMessage());
        }
    }

    private void renommerCategorie() {
        Categorie categorie = this.getCategorieSelectionnee();
        if (categorie == null) {
            return;
        }
        String nom = Dialogues.saisir("Renommer la catégorie", "Nouveau nom :", categorie.getNom());
        if (nom == null) {
            return;
        }
        try {
            this.banque.renommerCategorie(categorie, nom);
            this.application.sauvegarder();
            this.rafraichir(categorie, null);
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Renommage impossible", e.getMessage());
        }
    }

    private void supprimerCategorie() {
        Categorie categorie = this.getCategorieSelectionnee();
        if (categorie == null) {
            return;
        }
        int nombre = this.banque.compterQuestions(categorie);
        String message = "Supprimer la catégorie « " + categorie.getNom() + " » ?";
        if (nombre > 0) {
            message = "La catégorie « " + categorie.getNom() + " » contient " + nombre + " question(s).\n"
                    + "Sa suppression entraînera la suppression de toutes ces questions.\n\nConfirmer la suppression ?";
        }
        if (!Dialogues.confirmer("Supprimer une catégorie", message, "Supprimer")) {
            return;
        }
        try {
            this.banque.supprimerCategorie(categorie);
            this.application.sauvegarder();
            this.rafraichir(null, null);
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Suppression impossible", e.getMessage());
        }
    }

    /* ---------------------------------------------------------------- questions */

    private void creerQuestion() {
        FormulaireQuestion formulaire = new FormulaireQuestion(this.banque, null, this.getCategorieSelectionnee());
        Question question = formulaire.demander();
        if (question != null) {
            try {
                this.banque.ajouterQuestion(question);
            } catch (IllegalArgumentException e) {
                Dialogues.erreur("Question non ajoutée", e.getMessage());
            }
        }
        this.application.sauvegarder();
        if (question != null) {
            this.rafraichir(question.getCategorie(), question);
        } else {
            this.rafraichir(this.getCategorieSelectionnee(), null);
        }
    }

    private void modifierQuestion() {
        Question question = this.getQuestionSelectionnee();
        if (question == null) {
            return;
        }
        FormulaireQuestion formulaire = new FormulaireQuestion(this.banque, question, null);
        Question nouvellesValeurs = formulaire.demander();
        if (nouvellesValeurs != null) {
            try {
                this.banque.modifierQuestion(question, nouvellesValeurs);
            } catch (IllegalArgumentException e) {
                Dialogues.erreur("Question non modifiée", e.getMessage());
            }
        }
        this.application.sauvegarder();
        this.rafraichir(question.getCategorie(), question);
    }

    private void supprimerQuestion() {
        Question question = this.getQuestionSelectionnee();
        if (question == null) {
            return;
        }
        boolean confirme = Dialogues.confirmer("Supprimer une question",
                "Supprimer définitivement la question « " + question.getLibelle() + " » ?\n"
                        + "Les tests de l'historique qui l'utilisent restent consultables et rejouables.",
                "Supprimer");
        if (confirme) {
            this.banque.supprimerQuestion(question);
            this.application.sauvegarder();
            this.rafraichir(question.getCategorie(), null);
        }
    }
}
