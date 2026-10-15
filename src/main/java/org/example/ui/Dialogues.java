package org.example.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Boîtes de dialogue standard de l'application (information, erreur, confirmation, saisie).
 */
public class Dialogues {

    private static Window fenetre = null;

    private Dialogues() {
    }

    public static void setFenetre(Window fenetrePrincipale) {
        fenetre = fenetrePrincipale;
    }

    /**
     * Rattache une boîte de dialogue à la fenêtre principale et lui applique le style.
     */
    public static void preparer(Dialog<?> dialogue) {
        if (fenetre != null && fenetre.isShowing()) {
            dialogue.initOwner(fenetre);
        }
        Style.appliquer(dialogue.getDialogPane());
        dialogue.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }

    private static Alert creer(Alert.AlertType type, String titre, String message) {
        Alert alerte = new Alert(type);
        preparer(alerte);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        Label contenu = new Label(message);
        contenu.setWrapText(true);
        contenu.setMaxWidth(520);
        alerte.getDialogPane().setContent(contenu);
        return alerte;
    }

    public static void information(String titre, String message) {
        creer(Alert.AlertType.INFORMATION, titre, message).showAndWait();
    }

    public static void erreur(String titre, String message) {
        creer(Alert.AlertType.ERROR, titre, message).showAndWait();
    }

    /**
     * @param titre titre de la fenêtre
     * @param message question posée
     * @param texteConfirmer texte du bouton de confirmation
     * @return true si l'utilisateur confirme
     */
    public static boolean confirmer(String titre, String message, String texteConfirmer) {
        Alert alerte = creer(Alert.AlertType.CONFIRMATION, titre, message);
        ButtonType oui = new ButtonType(texteConfirmer, ButtonBar.ButtonData.OK_DONE);
        ButtonType non = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerte.getButtonTypes().setAll(oui, non);
        Optional<ButtonType> choix = alerte.showAndWait();
        return choix.isPresent() && choix.get() == oui;
    }

    /**
     * @param titre titre de la fenêtre
     * @param message texte d'explication
     * @param valeurInitiale texte proposé au départ
     * @return le texte saisi, ou null si l'utilisateur annule
     */
    public static String saisir(String titre, String message, String valeurInitiale) {
        TextInputDialog dialogue = new TextInputDialog(valeurInitiale);
        preparer(dialogue);
        dialogue.setTitle(titre);
        dialogue.setHeaderText(null);
        dialogue.setContentText(message);
        ButtonType valider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogue.getDialogPane().getButtonTypes().setAll(valider, annuler);
        Optional<String> resultat = dialogue.showAndWait();
        if (resultat.isPresent()) {
            return resultat.get();
        }
        return null;
    }
}
