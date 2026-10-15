package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.Question;
import org.example.service.RapportImport;
import org.example.service.ServiceCsv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Importation d'un fichier CSV de questions (avec compte rendu détaillé)
 * et exportation de la banque au même format.
 */
public class VueImportCsv extends BorderPane {

    private static final String TOUTES = "Toutes les catégories";

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private Label resume;
    private TextArea rapport;
    private ComboBox<String> choixExport;

    public VueImportCsv(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();
        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Import / export CSV"));
        contenu.getChildren().add(Style.sousTitre("Préparez vos questions dans un tableur (Excel, LibreOffice…), "
                + "enregistrez-les au format CSV puis importez-les ici."));
        contenu.getChildren().add(this.creerCarteImport());
        contenu.getChildren().add(this.creerCarteExport());
        this.setCenter(Style.defilable(contenu));
    }

    private VBox creerCarteImport() {
        Label format = Style.label("Une question par ligne, colonnes dans cet ordre (séparateur point-virgule) :\n"
                + "Catégorie ; Niveau (1, 2 ou 3) ; Libellé ; Proposition juste ; Fausse 1 ; [Fausse 2] ; [Fausse 3] ; "
                + "[Fausse 4] ; [Feedback]\nLe feedback, s'il existe, est toujours dans la 9e colonne.", "texte-discret");
        Button choisir = Style.boutonPrincipal("Choisir un fichier CSV…");
        choisir.setOnAction(evenement -> this.importer());
        Button notice = Style.boutonLien("Format détaillé dans la notice");
        notice.setOnAction(evenement -> this.application.naviguerVers(ApplicationQuiz.MENU_NOTICE));
        HBox actions = new HBox(16, choisir, notice);
        actions.setAlignment(Pos.CENTER_LEFT);

        this.resume = Style.label("", "texte-gras");
        this.resume.setVisible(false);
        this.resume.setManaged(false);
        this.rapport = new TextArea();
        this.rapport.setEditable(false);
        this.rapport.setWrapText(true);
        this.rapport.setPrefRowCount(12);
        this.rapport.getStyleClass().add("journal");
        this.rapport.setVisible(false);
        this.rapport.setManaged(false);

        return Style.carte(Style.titreCarte("Importer des questions"), format, actions, this.resume, this.rapport);
    }

    private VBox creerCarteExport() {
        this.choixExport = new ComboBox<>();
        this.choixExport.getItems().add(TOUTES);
        for (Categorie categorie : this.banque.getCategories()) {
            this.choixExport.getItems().add(categorie.getNom());
        }
        this.choixExport.getSelectionModel().select(0);
        Button exporter = Style.boutonSecondaire("Exporter…");
        exporter.setOnAction(evenement -> this.exporter());
        HBox ligne = new HBox(12, this.choixExport, exporter);
        ligne.setAlignment(Pos.CENTER_LEFT);
        return Style.carte(Style.titreCarte("Exporter des questions"),
                Style.texteDiscret("Le fichier produit s'ouvre directement dans Excel et peut être réimporté "
                        + "sur un autre ordinateur."), ligne);
    }

    private FileChooser creerSelecteur(String titre) {
        FileChooser selecteur = new FileChooser();
        selecteur.setTitle(titre);
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));
        selecteur.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        return selecteur;
    }

    private void importer() {
        File fichier = this.creerSelecteur("Importer un fichier de questions").showOpenDialog(this.application.getFenetre());
        if (fichier == null) {
            return;
        }
        RapportImport resultat = ServiceCsv.importerFichier(fichier, this.banque);
        this.application.sauvegarder();

        this.resume.setText("Fichier « " + fichier.getName() + " » : " + resultat.getResume());
        this.resume.getStyleClass().removeAll("reponse-juste", "reponse-fausse");
        if (resultat.estSansErreur()) {
            this.resume.getStyleClass().add("reponse-juste");
        } else {
            this.resume.getStyleClass().add("reponse-fausse");
        }
        this.rapport.setText(resultat.getDetail());
        this.resume.setVisible(true);
        this.resume.setManaged(true);
        this.rapport.setVisible(true);
        this.rapport.setManaged(true);

        if (resultat.getErreurFatale() != null) {
            Dialogues.erreur("Importation impossible", resultat.getResume());
        } else {
            Dialogues.information("Importation terminée", resultat.getResume()
                    + "\n\nLe détail (catégories créées, doublons et lignes en erreur) est affiché dans la page.");
        }
    }

    private void exporter() {
        ArrayList<Question> questions;
        String nomFichier;
        if (this.choixExport.getSelectionModel().getSelectedIndex() <= 0) {
            questions = this.banque.getQuestions();
            nomFichier = "questions_quiz.csv";
        } else {
            Categorie categorie = this.banque.chercherCategorie(this.choixExport.getValue());
            questions = this.banque.getQuestionsDeCategorie(categorie);
            nomFichier = categorie.getNom().replaceAll("[^A-Za-z0-9À-ÿ_-]+", "_") + ".csv";
        }
        if (questions.isEmpty()) {
            Dialogues.information("Rien à exporter", "Aucune question à exporter pour ce choix.");
            return;
        }
        FileChooser selecteur = this.creerSelecteur("Exporter les questions");
        selecteur.setInitialFileName(nomFichier);
        File fichier = selecteur.showSaveDialog(this.application.getFenetre());
        if (fichier == null) {
            return;
        }
        try {
            ServiceCsv.exporterFichier(questions, fichier);
            Dialogues.information("Exportation terminée", questions.size() + " question(s) exportée(s) dans "
                    + fichier.getAbsolutePath() + ".");
        } catch (IOException e) {
            Dialogues.erreur("Exportation impossible", "Le fichier n'a pas pu être écrit : " + e.getMessage());
        }
    }
}
