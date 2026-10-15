package org.example.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.BanqueDeDonnees;
import org.example.model.GestionnaireSerialisation;
import org.example.model.Questionnaire;
import org.example.model.Tentative;
import org.example.service.PeerNetworkService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Fenêtre principale : menu latéral à gauche, vue courante au centre.
 * Les données sont chargées au démarrage et sauvegardées après chaque modification.
 */
public class ApplicationQuiz extends Application {

    public static final int MENU_ACCUEIL = 0;
    public static final int MENU_PERFECTIONNEMENT = 1;
    public static final int MENU_GESTION = 2;
    public static final int MENU_IMPORT = 3;
    public static final int MENU_RESEAU = 4;
    public static final int MENU_HISTORIQUE = 5;
    public static final int MENU_PARAMETRES = 6;
    public static final int MENU_NOTICE = 7;

    private static final String[] LIBELLES_MENU = {
        "Accueil & jouer", "Perfectionnement", "Catégories & questions", "Import / export CSV",
        "Partage réseau", "Historique", "Paramètres", "Notice d'utilisation"
    };

    private BanqueDeDonnees banque;
    private GestionnaireSerialisation gestionnaire;
    private PeerNetworkService serviceReseau;
    private Stage fenetre;
    private BorderPane racine;
    private ArrayList<Button> boutonsMenu;
    private Label piedMenu;
    private boolean questionnaireEnCours;
    private VueReseau vueReseau;
    private String messageDemarrage;

    @Override
    public void start(Stage stage) {
        this.fenetre = stage;
        this.gestionnaire = new GestionnaireSerialisation();
        this.serviceReseau = new PeerNetworkService();
        this.questionnaireEnCours = false;
        this.vueReseau = null;
        this.messageDemarrage = null;
        this.chargerDonnees();

        this.racine = new BorderPane();
        this.racine.setLeft(this.creerMenu());
        Scene scene = new Scene(this.racine, 1180, 760);
        Style.appliquer(scene);
        stage.setTitle("Quiz – Testez vos connaissances");
        stage.setMinWidth(1000);
        stage.setMinHeight(660);
        stage.setScene(scene);
        this.naviguerVers(MENU_ACCUEIL);
        stage.show();
        Dialogues.setFenetre(stage);

        if (this.messageDemarrage != null) {
            String message = this.messageDemarrage;
            Platform.runLater(() -> Dialogues.erreur("Données illisibles", message));
        }
    }

    @Override
    public void stop() {
        this.serviceReseau.arreterReception();
        try {
            this.gestionnaire.sauvegarder(this.banque);
        } catch (IOException e) {
            System.err.println("Sauvegarde impossible à la fermeture : " + e.getMessage());
        }
    }

    private void chargerDonnees() {
        boolean aSauvegarder = !this.gestionnaire.fichierExiste();
        try {
            this.banque = this.gestionnaire.charger();
        } catch (IOException e) {
            String cause = e.getMessage();
            if (cause == null) {
                cause = e.getClass().getSimpleName();
            }
            File copie = this.gestionnaire.mettreDeCote();
            this.banque = BanqueDeDonnees.creerAvecDonneesInitiales();
            this.messageDemarrage = "Le fichier de données n'a pas pu être lu (" + cause + ").\n"
                    + "L'application repart avec la banque de questions initiale.";
            if (copie != null) {
                this.messageDemarrage = this.messageDemarrage + "\nL'ancien fichier a été conservé sous le nom "
                        + copie.getName() + ".";
            }
            aSauvegarder = true;
        }
        this.banque.getParametres().mettreAJourAdresseIpSiNecessaire();
        if (aSauvegarder) {
            this.sauvegarder();
        }
    }

    private VBox creerMenu() {
        VBox menu = new VBox();
        menu.getStyleClass().add("menu-lateral");
        menu.getChildren().add(Style.label("Quiz", "menu-logo"));
        menu.getChildren().add(Style.label("Testez vos connaissances", "menu-sous-titre"));
        this.boutonsMenu = new ArrayList<>();
        for (int i = 0; i < LIBELLES_MENU.length; i++) {
            if (i == MENU_ACCUEIL) {
                menu.getChildren().add(Style.label("JOUER", "menu-section"));
            } else if (i == MENU_GESTION) {
                menu.getChildren().add(Style.label("BANQUE DE QUESTIONS", "menu-section"));
            } else if (i == MENU_HISTORIQUE) {
                menu.getChildren().add(Style.label("SUIVI", "menu-section"));
            } else if (i == MENU_PARAMETRES) {
                menu.getChildren().add(Style.label("RÉGLAGES", "menu-section"));
            }
            Button bouton = Style.bouton(LIBELLES_MENU[i], "bouton-menu");
            bouton.setMaxWidth(Double.MAX_VALUE);
            int index = i;
            bouton.setOnAction(evenement -> this.naviguerVers(index));
            this.boutonsMenu.add(bouton);
            menu.getChildren().add(bouton);
        }
        Region espace = new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);
        this.piedMenu = Style.label("", "menu-pied");
        menu.getChildren().addAll(espace, this.piedMenu);
        this.rafraichirMenu();
        return menu;
    }

    /**
     * Met à jour le nom du joueur affiché en bas du menu.
     */
    public void rafraichirMenu() {
        String nom = this.banque.getParametres().getNomJoueur();
        if (nom.isEmpty()) {
            this.piedMenu.setText("Joueur : (nom non renseigné)");
        } else {
            this.piedMenu.setText("Joueur : " + nom);
        }
    }

    /**
     * Affiche la vue d'une rubrique du menu. Si un questionnaire est en cours,
     * l'utilisateur doit confirmer son abandon.
     * @param index une des constantes MENU_...
     */
    public void naviguerVers(int index) {
        if (this.questionnaireEnCours) {
            boolean abandon = Dialogues.confirmer("Questionnaire en cours",
                    "Voulez-vous abandonner le questionnaire en cours ? Vos réponses ne seront pas enregistrées.",
                    "Abandonner");
            if (!abandon) {
                return;
            }
            this.questionnaireEnCours = false;
        }
        for (Button bouton : this.boutonsMenu) {
            bouton.getStyleClass().remove("actif");
        }
        this.boutonsMenu.get(index).getStyleClass().add("actif");
        this.racine.setCenter(this.creerVue(index));
    }

    private Node creerVue(int index) {
        switch (index) {
            case MENU_PERFECTIONNEMENT:
                return new VuePerfectionnement(this);
            case MENU_GESTION:
                return new VueGestion(this);
            case MENU_IMPORT:
                return new VueImportCsv(this);
            case MENU_RESEAU:
                if (this.vueReseau == null) {
                    this.vueReseau = new VueReseau(this);
                }
                this.vueReseau.rafraichir();
                return this.vueReseau;
            case MENU_HISTORIQUE:
                return new VueHistorique(this);
            case MENU_PARAMETRES:
                return new VueParametres(this);
            case MENU_NOTICE:
                return new VueNotice(this);
            default:
                return new VueAccueil(this);
        }
    }

    /**
     * Affiche une vue sans changer la rubrique sélectionnée dans le menu.
     */
    public void afficher(Node vue) {
        this.racine.setCenter(vue);
    }

    public void afficherAccueil() {
        this.naviguerVers(MENU_ACCUEIL);
    }

    /**
     * @param questionnaire questionnaire à faire passer
     * @param tentativePrecedente tentative refaite (pour comparer les scores), ou null
     */
    public void lancerQuestionnaire(Questionnaire questionnaire, Tentative tentativePrecedente) {
        this.questionnaireEnCours = true;
        this.afficher(new VueQuestionnaire(this, questionnaire, tentativePrecedente));
    }

    /**
     * Indique que le questionnaire en cours est terminé (validé ou abandonné).
     */
    public void terminerQuestionnaire() {
        this.questionnaireEnCours = false;
    }

    /**
     * Sauvegarde toutes les données ; prévient l'utilisateur en cas d'échec.
     */
    public void sauvegarder() {
        try {
            this.gestionnaire.sauvegarder(this.banque);
        } catch (IOException e) {
            Dialogues.erreur("Sauvegarde impossible", "Les données n'ont pas pu être enregistrées dans "
                    + this.gestionnaire.getFichier().getAbsolutePath() + ".\nCause : " + e.getMessage());
        }
    }

    public BanqueDeDonnees getBanque() {
        return this.banque;
    }

    public GestionnaireSerialisation getGestionnaire() {
        return this.gestionnaire;
    }

    public PeerNetworkService getServiceReseau() {
        return this.serviceReseau;
    }

    public Stage getFenetre() {
        return this.fenetre;
    }
}
