package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.Parametres;
import org.example.model.Question;
import org.example.service.EcouteurReseau;
import org.example.service.PeerNetworkService;
import org.example.service.RapportImport;
import org.example.service.ServiceCsv;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Partage de questions entre deux ordinateurs : réception (attente d'un pair)
 * ou envoi (de toute la banque, de catégories ou de questions choisies).
 * Cette vue est conservée par l'application pour garder le journal et l'état de la réception.
 */
public class VueReseau extends BorderPane {

    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private PeerNetworkService service;

    private Label infosLocales;
    private Button boutonReception;
    private Label etatReception;

    private TextField champIp;
    private TextField champPort;
    private RadioButton modeTout;
    private RadioButton modeCategories;
    private RadioButton modeQuestions;
    private VBox zoneSelection;
    private ArrayList<CheckBox> casesCategories;
    private ArrayList<CheckBox> casesQuestions;
    private Label resumeSelection;
    private Button boutonEnvoyer;

    private TextArea journal;

    public VueReseau(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();
        this.service = application.getServiceReseau();
        this.casesCategories = new ArrayList<>();
        this.casesQuestions = new ArrayList<>();

        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Partage réseau"));
        contenu.getChildren().add(Style.sousTitre("Échangez des questions avec un autre ordinateur du même réseau. "
                + "Les données sont chiffrées (Diffie-Hellman + Vigenère) et la connexion se ferme "
                + "automatiquement à la fin de l'échange."));

        VBox colonneGauche = new VBox(18, this.creerCarteReception());
        VBox colonneDroite = new VBox(18, this.creerCarteEnvoi());
        HBox.setHgrow(colonneGauche, Priority.ALWAYS);
        HBox.setHgrow(colonneDroite, Priority.ALWAYS);
        colonneGauche.setPrefWidth(380);
        colonneDroite.setPrefWidth(480);
        HBox colonnes = new HBox(18, colonneGauche, colonneDroite);
        contenu.getChildren().add(colonnes);

        this.journal = new TextArea();
        this.journal.setEditable(false);
        this.journal.setWrapText(true);
        this.journal.setPrefRowCount(8);
        this.journal.getStyleClass().add("journal");
        contenu.getChildren().add(Style.carte(Style.titreCarte("Journal des échanges"), this.journal));

        this.setCenter(Style.defilable(contenu));
        this.rafraichir();
    }

    /**
     * Met à jour les paramètres affichés et les listes de sélection (appelée à chaque affichage).
     */
    public void rafraichir() {
        Parametres parametres = this.banque.getParametres();
        this.infosLocales.setText("Adresse IP de cet ordinateur : " + parametres.getAdresseIp()
                + "\nPort d'écoute : " + parametres.getPort());
        if (this.champIp.getText().isEmpty()) {
            this.champIp.setText(parametres.getAdresseIpPair());
            this.champPort.setText(String.valueOf(parametres.getPortPair()));
        }
        this.construireSelection();
        this.mettreAJourEtatReception();
    }

    /* ---------------------------------------------------------------- réception */

    private VBox creerCarteReception() {
        this.infosLocales = Style.label("", "texte-gras");
        Button modifier = Style.boutonLien("Modifier dans les paramètres");
        modifier.setOnAction(evenement -> this.application.naviguerVers(ApplicationQuiz.MENU_PARAMETRES));
        Label explication = Style.texteDiscret("1. Démarrez la réception.\n2. Communiquez votre adresse IP et votre "
                + "port à votre pair.\n3. Il vous envoie ses questions : elles sont vérifiées et ajoutées comme "
                + "lors d'un import CSV.");
        this.boutonReception = Style.boutonPrincipal("Démarrer la réception");
        this.boutonReception.setOnAction(evenement -> this.basculerReception());
        this.etatReception = Style.texteDiscret("");
        HBox actions = new HBox(12, this.boutonReception, this.etatReception);
        actions.setAlignment(Pos.CENTER_LEFT);
        return Style.carte(Style.titreCarte("Recevoir des questions"), this.infosLocales, modifier, explication, actions);
    }

    private void mettreAJourEtatReception() {
        if (this.service.estEnEcoute()) {
            this.boutonReception.setText("Arrêter la réception");
            this.etatReception.setText("En attente d'un pair…");
        } else {
            this.boutonReception.setText("Démarrer la réception");
            this.etatReception.setText("Réception arrêtée.");
        }
    }

    private void basculerReception() {
        if (this.service.estEnEcoute()) {
            this.service.arreterReception();
            return;
        }
        int port = this.banque.getParametres().getPort();
        this.service.demarrerReception(port, new EcouteurReseau() {
            @Override
            public void information(String message) {
                Platform.runLater(() -> VueReseau.this.journaliser(message));
            }

            @Override
            public void donneesRecues(String contenu) {
                Platform.runLater(() -> VueReseau.this.integrerDonnees(contenu));
            }

            @Override
            public void erreur(String message) {
                Platform.runLater(() -> {
                    VueReseau.this.journaliser("ERREUR : " + message);
                    Dialogues.erreur("Réception", message);
                });
            }

            @Override
            public void termine() {
                Platform.runLater(() -> VueReseau.this.mettreAJourEtatReception());
            }
        });
        this.mettreAJourEtatReception();
    }

    private void integrerDonnees(String contenu) {
        RapportImport rapport = ServiceCsv.importerTexte(contenu, this.banque);
        this.application.sauvegarder();
        this.journaliser("Intégration des questions reçues :\n" + rapport.getDetail());
        this.construireSelection();
        Dialogues.information("Questions reçues", rapport.getResume()
                + "\n\nLe détail est affiché dans le journal de la page « Partage réseau ».");
    }

    /* ---------------------------------------------------------------- envoi */

    private VBox creerCarteEnvoi() {
        this.champIp = new TextField();
        this.champIp.setPromptText("ex. 192.168.1.20");
        this.champPort = new TextField();
        this.champPort.setPrefColumnCount(6);
        GridPane pair = new GridPane();
        pair.setHgap(12);
        pair.setVgap(10);
        pair.add(Style.label("Adresse IP du pair", "texte-gras"), 0, 0);
        pair.add(this.champIp, 1, 0);
        pair.add(Style.label("Port du pair", "texte-gras"), 0, 1);
        pair.add(this.champPort, 1, 1);

        ToggleGroup groupe = new ToggleGroup();
        this.modeTout = new RadioButton("Toute la banque");
        this.modeCategories = new RadioButton("Une ou plusieurs catégories");
        this.modeQuestions = new RadioButton("Questions choisies une par une");
        this.modeTout.setToggleGroup(groupe);
        this.modeCategories.setToggleGroup(groupe);
        this.modeQuestions.setToggleGroup(groupe);
        this.modeTout.setSelected(true);
        groupe.selectedToggleProperty().addListener((observable, ancien, nouveau) -> this.construireSelection());
        VBox modes = new VBox(6, this.modeTout, this.modeCategories, this.modeQuestions);

        this.zoneSelection = new VBox(6);
        this.zoneSelection.setPadding(new Insets(4));
        ScrollPane defilement = new ScrollPane(this.zoneSelection);
        defilement.setFitToWidth(true);
        defilement.setPrefViewportHeight(210);
        defilement.setStyle("-fx-background-color: #f6f7fb; -fx-background-radius: 10; -fx-padding: 8;");

        this.resumeSelection = Style.texteDiscret("");
        this.boutonEnvoyer = Style.boutonPrincipal("Envoyer");
        this.boutonEnvoyer.setOnAction(evenement -> this.envoyer());
        HBox actions = new HBox(12, this.boutonEnvoyer, this.resumeSelection);
        actions.setAlignment(Pos.CENTER_LEFT);

        return Style.carte(Style.titreCarte("Envoyer des questions"),
                Style.texteDiscret("Le pair doit avoir démarré la réception avant l'envoi."),
                pair, Style.label("Questions à envoyer", "texte-gras"), modes, defilement, actions);
    }

    private void construireSelection() {
        this.zoneSelection.getChildren().clear();
        this.casesCategories.clear();
        this.casesQuestions.clear();
        if (this.modeTout.isSelected()) {
            this.zoneSelection.getChildren().add(Style.texteDiscret("Toutes les questions de la banque ("
                    + this.banque.getQuestions().size() + ") seront envoyées avec leur catégorie."));
        } else if (this.modeCategories.isSelected()) {
            for (Categorie categorie : this.banque.getCategories()) {
                CheckBox caseCategorie = new CheckBox(categorie.getNom() + " (" + this.banque.compterQuestions(categorie)
                        + " question(s))");
                caseCategorie.setUserData(categorie);
                caseCategorie.setOnAction(evenement -> this.mettreAJourResume());
                this.casesCategories.add(caseCategorie);
                this.zoneSelection.getChildren().add(caseCategorie);
            }
        } else {
            Button toutCocher = Style.boutonLien("Tout cocher");
            toutCocher.setOnAction(evenement -> this.cocherToutesLesQuestions(true));
            Button toutDecocher = Style.boutonLien("Tout décocher");
            toutDecocher.setOnAction(evenement -> this.cocherToutesLesQuestions(false));
            this.zoneSelection.getChildren().add(new HBox(16, toutCocher, toutDecocher));
            for (Categorie categorie : this.banque.getCategories()) {
                ArrayList<Question> questions = this.banque.getQuestionsDeCategorie(categorie);
                if (!questions.isEmpty()) {
                    this.zoneSelection.getChildren().add(Style.label(categorie.getNom(), "texte-gras"));
                    for (Question question : questions) {
                        CheckBox caseQuestion = new CheckBox("[Niv. " + question.getNiveau() + "] " + question.getLibelle());
                        caseQuestion.setUserData(question);
                        caseQuestion.setWrapText(true);
                        caseQuestion.setOnAction(evenement -> this.mettreAJourResume());
                        this.casesQuestions.add(caseQuestion);
                        this.zoneSelection.getChildren().add(caseQuestion);
                    }
                }
            }
        }
        this.mettreAJourResume();
    }

    private void cocherToutesLesQuestions(boolean coche) {
        for (CheckBox caseQuestion : this.casesQuestions) {
            caseQuestion.setSelected(coche);
        }
        this.mettreAJourResume();
    }

    private ArrayList<Question> getSelection() {
        ArrayList<Question> selection = new ArrayList<>();
        if (this.modeTout.isSelected()) {
            selection.addAll(this.banque.getQuestions());
        } else if (this.modeCategories.isSelected()) {
            for (CheckBox caseCategorie : this.casesCategories) {
                if (caseCategorie.isSelected()) {
                    selection.addAll(this.banque.getQuestionsDeCategorie((Categorie) caseCategorie.getUserData()));
                }
            }
        } else {
            for (CheckBox caseQuestion : this.casesQuestions) {
                if (caseQuestion.isSelected()) {
                    selection.add((Question) caseQuestion.getUserData());
                }
            }
        }
        return selection;
    }

    private void mettreAJourResume() {
        this.resumeSelection.setText(this.getSelection().size() + " question(s) sélectionnée(s).");
    }

    private void envoyer() {
        ArrayList<Question> selection = this.getSelection();
        if (selection.isEmpty()) {
            Dialogues.erreur("Rien à envoyer", "Sélectionnez au moins une question à envoyer.");
            return;
        }
        String adresse;
        int port;
        try {
            adresse = Parametres.verifierAdresse(this.champIp.getText());
            port = Parametres.verifierPort(Integer.parseInt(this.champPort.getText().trim()));
        } catch (NumberFormatException e) {
            Dialogues.erreur("Paramètres du pair incorrects", "Le port doit être un nombre entier.");
            return;
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Paramètres du pair incorrects", e.getMessage());
            return;
        }
        this.banque.getParametres().setAdresseIpPair(adresse);
        this.banque.getParametres().setPortPair(port);
        this.application.sauvegarder();

        this.boutonEnvoyer.setDisable(true);
        this.journaliser("Envoi de " + selection.size() + " question(s) à " + adresse + ":" + port + ".");
        this.service.envoyer(adresse, port, ServiceCsv.exporterTexte(selection), new EcouteurReseau() {
            @Override
            public void information(String message) {
                Platform.runLater(() -> VueReseau.this.journaliser(message));
            }

            @Override
            public void donneesRecues(String contenu) {
            }

            @Override
            public void erreur(String message) {
                Platform.runLater(() -> {
                    VueReseau.this.journaliser("ERREUR : " + message);
                    Dialogues.erreur("Envoi", message);
                });
            }

            @Override
            public void termine() {
                Platform.runLater(() -> VueReseau.this.boutonEnvoyer.setDisable(false));
            }
        });
    }

    private void journaliser(String message) {
        this.journal.appendText("[" + LocalTime.now().format(FORMAT_HEURE) + "] " + message + "\n");
    }
}
