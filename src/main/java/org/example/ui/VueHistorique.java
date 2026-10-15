package org.example.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Question;
import org.example.model.Tentative;

/**
 * Historique des derniers tests : consultation du corrigé, rejeu avec comparaison
 * des scores, suppression.
 */
public class VueHistorique extends BorderPane {

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private TableView<Tentative> table;
    private Button boutonCorrige;
    private Button boutonRefaire;
    private Button boutonSupprimer;
    private Button boutonVider;

    public VueHistorique(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();

        VBox entete = new VBox(6, Style.titre("Historique"),
                Style.sousTitre("Les " + this.banque.getParametres().getLimiteHistorique()
                        + " derniers tests sont conservés (valeur modifiable dans les paramètres). "
                        + "Double-cliquez sur un test pour voir son corrigé."));
        entete.setPadding(new Insets(28, 32, 16, 32));
        this.setTop(entete);

        this.table = new TableView<>();
        this.table.setPlaceholder(Style.texteDiscret("Aucun test dans l'historique pour le moment."));
        this.ajouterColonne("Date et heure", 0.22, "date");
        this.ajouterColonne("Catégorie", 0.26, "categorie");
        this.ajouterColonne("Niveau", 0.13, "niveau");
        this.ajouterColonne("Questions", 0.11, "questions");
        this.ajouterColonne("Bonnes réponses", 0.15, "justes");
        this.ajouterColonne("Réussite", 0.11, "pourcentage");
        this.table.getSelectionModel().selectedItemProperty().addListener(
                (observable, ancienne, nouvelle) -> this.mettreAJourBoutons());
        this.table.setOnMouseClicked(evenement -> {
            if (evenement.getClickCount() == 2 && this.getSelection() != null) {
                this.voirCorrige();
            }
        });
        VBox.setVgrow(this.table, Priority.ALWAYS);

        this.boutonCorrige = Style.boutonSecondaire("Voir le corrigé");
        this.boutonCorrige.setOnAction(evenement -> this.voirCorrige());
        this.boutonRefaire = Style.boutonPrincipal("Refaire ce test");
        this.boutonRefaire.setOnAction(evenement -> this.refaire());
        this.boutonSupprimer = Style.boutonDanger("Supprimer ce test");
        this.boutonSupprimer.setOnAction(evenement -> this.supprimer());
        this.boutonVider = Style.boutonDanger("Vider l'historique");
        this.boutonVider.setOnAction(evenement -> this.vider());
        HBox actions = new HBox(10, this.boutonRefaire, this.boutonCorrige, this.boutonSupprimer, Style.espace(),
                this.boutonVider);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox carte = Style.carte(this.table, actions);
        BorderPane.setMargin(carte, new Insets(0, 32, 28, 32));
        this.setCenter(carte);
        this.rafraichir();
    }

    private void ajouterColonne(String titre, double proportion, String champ) {
        TableColumn<Tentative, String> colonne = new TableColumn<>(titre);
        colonne.setCellValueFactory(donnees -> new SimpleStringProperty(this.valeur(donnees.getValue(), champ)));
        colonne.prefWidthProperty().bind(this.table.widthProperty().subtract(20).multiply(proportion));
        colonne.setSortable(false);
        this.table.getColumns().add(colonne);
    }

    private String valeur(Tentative tentative, String champ) {
        switch (champ) {
            case "date":
                return tentative.getDateFormatee();
            case "categorie":
                return tentative.getQuestionnaire().getLibelleCategorie();
            case "niveau":
                return tentative.getQuestionnaire().getLibelleNiveau();
            case "questions":
                return String.valueOf(tentative.getNombreQuestions());
            case "justes":
                return tentative.getNombreReponsesJustes() + " / " + tentative.getNombreQuestions();
            default:
                return tentative.getPourcentage() + " %";
        }
    }

    private void rafraichir() {
        this.table.getItems().setAll(this.banque.getHistorique());
        this.mettreAJourBoutons();
    }

    private Tentative getSelection() {
        return this.table.getSelectionModel().getSelectedItem();
    }

    private void mettreAJourBoutons() {
        boolean aucune = this.getSelection() == null;
        this.boutonCorrige.setDisable(aucune);
        this.boutonRefaire.setDisable(aucune);
        this.boutonSupprimer.setDisable(aucune);
        this.boutonVider.setDisable(this.table.getItems().isEmpty());
    }

    private void voirCorrige() {
        Tentative tentative = this.getSelection();
        if (tentative != null) {
            this.application.afficher(new VueCorrige(this.application, tentative, this));
        }
    }

    private void refaire() {
        Tentative tentative = this.getSelection();
        if (tentative == null) {
            return;
        }
        int modifiees = 0;
        for (Question question : tentative.getQuestionnaire().getQuestions()) {
            if (!this.banque.existeQuestionIdentique(question, null)) {
                modifiees++;
            }
        }
        if (modifiees > 0) {
            Dialogues.information("Questions modifiées depuis ce test",
                    modifiees + " question(s) de ce test ont été modifiées ou supprimées de la banque depuis.\n"
                            + "Le test est rejoué avec les questions telles qu'elles étaient à l'époque, "
                            + "pour que la comparaison avec votre score précédent reste juste.");
        }
        this.application.lancerQuestionnaire(tentative.creerQuestionnaireRejeu(), tentative);
    }

    private void supprimer() {
        Tentative tentative = this.getSelection();
        if (tentative == null) {
            return;
        }
        if (Dialogues.confirmer("Supprimer un test", "Supprimer le test du " + tentative.getDateFormatee()
                + " de l'historique ?", "Supprimer")) {
            this.banque.supprimerTentative(tentative);
            this.application.sauvegarder();
            this.rafraichir();
        }
    }

    private void vider() {
        if (Dialogues.confirmer("Vider l'historique", "Supprimer les " + this.table.getItems().size()
                + " tests de l'historique ? Cette action est définitive.", "Tout supprimer")) {
            this.banque.viderHistorique();
            this.application.sauvegarder();
            this.rafraichir();
        }
    }
}
