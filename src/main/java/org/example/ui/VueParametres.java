package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.OutilsTexte;
import org.example.model.Parametres;

/**
 * Paramètres : nom du joueur, nombre de tests mémorisés, adresse IP et port réseau.
 */
public class VueParametres extends BorderPane {

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private TextField champNom;
    private TextField champLimite;
    private ComboBox<String> choixIp;
    private TextField champPort;

    public VueParametres(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();
        Parametres parametres = this.banque.getParametres();

        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Paramètres"));

        this.champNom = new TextField(parametres.getNomJoueur());
        this.champNom.setPromptText("Votre prénom");
        this.champNom.setPrefColumnCount(22);
        this.champLimite = new TextField(String.valueOf(parametres.getLimiteHistorique()));
        this.champLimite.setPrefColumnCount(5);
        GridPane grilleJoueur = this.creerGrille();
        grilleJoueur.add(Style.label("Nom du joueur", "texte-gras"), 0, 0);
        grilleJoueur.add(this.champNom, 1, 0);
        grilleJoueur.add(Style.label("Tests mémorisés", "texte-gras"), 0, 1);
        grilleJoueur.add(new HBox(10, this.champLimite,
                Style.texteDiscret("entre 1 et " + Parametres.LIMITE_HISTORIQUE_MAX + " (10 par défaut)")), 1, 1);
        contenu.getChildren().add(Style.carte(Style.titreCarte("Joueur"),
                Style.texteDiscret("Votre nom personnalise les messages de résultat."), grilleJoueur));

        this.choixIp = new ComboBox<>();
        this.choixIp.setEditable(true);
        this.choixIp.getItems().setAll(Parametres.listerAdressesLocales());
        this.choixIp.getEditor().setText(parametres.getAdresseIp());
        this.choixIp.setPrefWidth(220);
        Button detecter = Style.boutonLien("Détecter automatiquement");
        detecter.setOnAction(evenement -> this.choixIp.getEditor().setText(Parametres.detecterAdresseIp()));
        HBox ligneIp = new HBox(12, this.choixIp, detecter);
        ligneIp.setAlignment(Pos.CENTER_LEFT);
        this.champPort = new TextField(String.valueOf(parametres.getPort()));
        this.champPort.setPrefColumnCount(6);
        GridPane grilleReseau = this.creerGrille();
        grilleReseau.add(Style.label("Adresse IP", "texte-gras"), 0, 0);
        grilleReseau.add(ligneIp, 1, 0);
        grilleReseau.add(Style.label("Port d'écoute", "texte-gras"), 0, 1);
        grilleReseau.add(new HBox(10, this.champPort,
                Style.texteDiscret("entre " + Parametres.PORT_MIN + " et " + Parametres.PORT_MAX)), 1, 1);
        contenu.getChildren().add(Style.carte(Style.titreCarte("Réseau"),
                Style.texteDiscret("Ces valeurs sont détectées automatiquement au premier lancement. "
                        + "Communiquez-les au pair qui vous envoie des questions."), grilleReseau));

        Button enregistrer = Style.boutonPrincipal("Enregistrer");
        enregistrer.setDefaultButton(true);
        enregistrer.setOnAction(evenement -> this.enregistrer());
        contenu.getChildren().add(enregistrer);

        contenu.getChildren().add(Style.texteDiscret("Fichier de données : "
                + application.getGestionnaire().getFichier().getAbsolutePath()));
        this.setCenter(Style.defilable(contenu));
    }

    private GridPane creerGrille() {
        GridPane grille = new GridPane();
        grille.setHgap(18);
        grille.setVgap(12);
        return grille;
    }

    private static int lireEntier(String texte, String nomChamp) {
        try {
            return Integer.parseInt(texte.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nomChamp + " doit être un nombre entier.");
        }
    }

    private void enregistrer() {
        Parametres parametres = this.banque.getParametres();
        try {
            String nom = this.champNom.getText();
            if (OutilsTexte.normaliser(nom).length() > 40) {
                throw new IllegalArgumentException("Le nom ne doit pas dépasser 40 caractères.");
            }
            int limite = lireEntier(this.champLimite.getText(), "Le nombre de tests mémorisés");
            if (limite < 1 || limite > Parametres.LIMITE_HISTORIQUE_MAX) {
                throw new IllegalArgumentException("Le nombre de tests mémorisés doit être compris entre 1 et "
                        + Parametres.LIMITE_HISTORIQUE_MAX + ".");
            }
            String adresse = Parametres.verifierAdresse(this.choixIp.getEditor().getText());
            int port = Parametres.verifierPort(lireEntier(this.champPort.getText(), "Le port"));

            int tailleHistorique = this.banque.getHistorique().size();
            if (limite < tailleHistorique) {
                boolean confirme = Dialogues.confirmer("Réduire l'historique", "L'historique contient "
                        + tailleHistorique + " tests : les " + (tailleHistorique - limite)
                        + " plus anciens seront supprimés. Continuer ?", "Continuer");
                if (!confirme) {
                    return;
                }
            }
            boolean portModifie = port != parametres.getPort();
            parametres.setNomJoueur(nom);
            parametres.setLimiteHistorique(limite);
            parametres.setAdresseIp(adresse);
            parametres.setPort(port);
            this.banque.appliquerLimiteHistorique();
            this.application.sauvegarder();
            this.application.rafraichirMenu();
            String message = "Paramètres enregistrés.";
            if (portModifie && this.application.getServiceReseau().estEnEcoute()) {
                message = message + "\nLe nouveau port sera utilisé à la prochaine réception.";
            }
            Dialogues.information("Paramètres", message);
        } catch (IllegalArgumentException e) {
            Dialogues.erreur("Paramètres incorrects", e.getMessage());
        }
    }
}
