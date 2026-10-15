package org.example.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.Question;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Boîte de dialogue de création ou de modification d'une question.
 * Elle ne se ferme qu'une fois la question valide et non identique à une question existante.
 */
public class FormulaireQuestion extends Dialog<ButtonType> {

    private BanqueDeDonnees banque;
    private Question existante;
    private Question resultat;
    private ButtonType boutonEnregistrer;

    private ComboBox<Categorie> choixCategorie;
    private SelecteurSegmente choixNiveau;
    private TextArea champLibelle;
    private TextField champJuste;
    private ArrayList<TextField> champsFausses;
    private TextArea champFeedback;
    private Label messageErreur;

    /**
     * @param banque banque de questions
     * @param existante question à modifier, ou null pour une création
     * @param categorieParDefaut catégorie proposée pour une création (peut être null)
     */
    public FormulaireQuestion(BanqueDeDonnees banque, Question existante, Categorie categorieParDefaut) {
        this.banque = banque;
        this.existante = existante;
        this.resultat = null;
        Dialogues.preparer(this);
        if (existante == null) {
            this.setTitle("Nouvelle question");
        } else {
            this.setTitle("Modifier la question");
        }
        this.setHeaderText(null);
        this.setResizable(true);

        this.boutonEnregistrer = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().addAll(this.boutonEnregistrer, annuler);
        this.getDialogPane().setContent(this.creerFormulaire());
        this.getDialogPane().setPrefWidth(680);

        if (existante != null) {
            this.remplir(existante);
        } else {
            Categorie categorie = categorieParDefaut;
            if (categorie == null) {
                categorie = banque.getCategorieGenerale();
            }
            this.choixCategorie.getSelectionModel().select(categorie);
        }

        Node bouton = this.getDialogPane().lookupButton(this.boutonEnregistrer);
        bouton.addEventFilter(ActionEvent.ACTION, evenement -> {
            if (!this.valider()) {
                evenement.consume();
            }
        });
        Platform.runLater(() -> this.champLibelle.requestFocus());
    }

    private VBox creerFormulaire() {
        this.choixCategorie = new ComboBox<>();
        this.choixCategorie.getItems().setAll(this.banque.getCategories());
        this.choixCategorie.setPrefWidth(300);
        Button nouvelleCategorie = Style.boutonLien("Nouvelle catégorie…");
        nouvelleCategorie.setOnAction(evenement -> this.creerCategorie());
        HBox ligneCategorie = new HBox(12, this.choixCategorie, nouvelleCategorie);
        ligneCategorie.setAlignment(Pos.CENTER_LEFT);

        this.choixNiveau = new SelecteurSegmente(new String[]{"1 · Facile", "2 · Moyen", "3 · Difficile"}, 0);

        this.champLibelle = new TextArea();
        this.champLibelle.setWrapText(true);
        this.champLibelle.setPrefRowCount(2);
        this.champLibelle.setPromptText("Texte de la question");

        this.champJuste = new TextField();
        this.champJuste.setPromptText("La seule proposition correcte");

        VBox fausses = new VBox(6);
        this.champsFausses = new ArrayList<>();
        for (int i = 1; i <= Question.NB_FAUSSES_MAX; i++) {
            TextField champ = new TextField();
            if (i == 1) {
                champ.setPromptText("Proposition fausse 1 (obligatoire)");
            } else {
                champ.setPromptText("Proposition fausse " + i + " (facultative)");
            }
            this.champsFausses.add(champ);
            fausses.getChildren().add(champ);
        }

        this.champFeedback = new TextArea();
        this.champFeedback.setWrapText(true);
        this.champFeedback.setPrefRowCount(2);
        this.champFeedback.setPromptText("Explication affichée dans le corrigé (facultatif)");

        this.champLibelle.setMinHeight(Region.USE_PREF_SIZE);
        this.champFeedback.setMinHeight(Region.USE_PREF_SIZE);
        GridPane grille = new GridPane();
        grille.setHgap(16);
        grille.setVgap(12);
        ColumnConstraints colonneIntitules = new ColumnConstraints();
        colonneIntitules.setMinWidth(165);
        ColumnConstraints colonneChamps = new ColumnConstraints();
        colonneChamps.setHgrow(Priority.ALWAYS);
        grille.getColumnConstraints().addAll(colonneIntitules, colonneChamps);
        grille.add(Style.label("Catégorie", "texte-gras"), 0, 0);
        grille.add(ligneCategorie, 1, 0);
        grille.add(Style.label("Difficulté", "texte-gras"), 0, 1);
        grille.add(this.choixNiveau, 1, 1);
        grille.add(Style.label("Libellé", "texte-gras"), 0, 2);
        grille.add(this.champLibelle, 1, 2);
        grille.add(Style.label("Proposition juste", "texte-gras"), 0, 3);
        grille.add(this.champJuste, 1, 3);
        grille.add(Style.label("Propositions fausses", "texte-gras"), 0, 4);
        grille.add(fausses, 1, 4);
        grille.add(Style.label("Feedback", "texte-gras"), 0, 5);
        grille.add(this.champFeedback, 1, 5);

        this.messageErreur = Style.label("", "message-erreur");
        return new VBox(14, grille, this.messageErreur);
    }

    private void remplir(Question question) {
        Categorie categorie = this.banque.chercherCategorie(question.getCategorie().getNom());
        this.choixCategorie.getSelectionModel().select(categorie);
        this.choixNiveau.setIndexSelectionne(question.getNiveau() - 1);
        this.champLibelle.setText(question.getLibelle());
        this.champJuste.setText(question.getReponseJuste());
        ArrayList<String> fausses = question.getReponsesFausses();
        for (int i = 0; i < fausses.size(); i++) {
            this.champsFausses.get(i).setText(fausses.get(i));
        }
        this.champFeedback.setText(question.getFeedback());
    }

    private void creerCategorie() {
        String nom = Dialogues.saisir("Nouvelle catégorie", "Nom de la catégorie :", "");
        if (nom == null) {
            return;
        }
        try {
            Categorie categorie = this.banque.creerCategorie(nom);
            this.choixCategorie.getItems().setAll(this.banque.getCategories());
            this.choixCategorie.getSelectionModel().select(categorie);
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Catégorie non créée", e.getMessage());
        }
    }

    private boolean valider() {
        Categorie categorie = this.choixCategorie.getValue();
        ArrayList<String> fausses = new ArrayList<>();
        for (TextField champ : this.champsFausses) {
            fausses.add(champ.getText());
        }
        try {
            Question question = new Question(categorie, this.choixNiveau.getIndexSelectionne() + 1,
                    this.champLibelle.getText(), this.champJuste.getText(), fausses, this.champFeedback.getText());
            if (this.banque.existeQuestionIdentique(question, this.existante)) {
                this.messageErreur.setText("Une question identique (même libellé et mêmes propositions) "
                        + "existe déjà dans la catégorie « " + categorie.getNom() + " ».");
                return false;
            }
            this.resultat = question;
            return true;
        } catch (IllegalArgumentException e) {
            this.messageErreur.setText(e.getMessage());
            return false;
        }
    }

    /**
     * Affiche le formulaire et attend sa fermeture.
     * @return la question saisie (non encore ajoutée à la banque), ou null si l'utilisateur annule
     */
    public Question demander() {
        Optional<ButtonType> choix = this.showAndWait();
        if (choix.isPresent() && choix.get() == this.boutonEnregistrer) {
            return this.resultat;
        }
        return null;
    }
}
