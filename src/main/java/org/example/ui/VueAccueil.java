package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.Question;
import org.example.model.Tentative;

import java.util.ArrayList;

/**
 * Accueil : statistiques rapides et préparation d'un questionnaire
 * (difficulté, catégorie facultative, nombre de questions).
 */
public class VueAccueil extends BorderPane {

    private static final int[] NOMBRES = {5, 10, 20};
    private static final String TOUTES = "Toutes les catégories";

    private ApplicationQuiz application;
    private BanqueDeDonnees banque;
    private SelecteurSegmente choixNiveau;
    private SelecteurSegmente choixNombre;
    private ComboBox<String> choixCategorie;
    private Label disponibles;

    public VueAccueil(ApplicationQuiz application) {
        this.application = application;
        this.banque = application.getBanque();
        VBox contenu = Style.vue();

        String nom = this.banque.getParametres().getNomJoueur();
        if (nom.isEmpty()) {
            contenu.getChildren().add(Style.titre("Bienvenue !"));
        } else {
            contenu.getChildren().add(Style.titre("Bonjour " + nom + " !"));
        }
        contenu.getChildren().add(Style.sousTitre(
                "Choisissez vos critères et lancez un questionnaire pour tester vos connaissances."));

        HBox tuiles = new HBox(14);
        tuiles.getChildren().add(Style.tuile(String.valueOf(this.banque.getQuestions().size()),
                "questions dans la banque"));
        tuiles.getChildren().add(Style.tuile(String.valueOf(this.banque.getCategories().size()), "catégories"));
        tuiles.getChildren().add(Style.tuile(String.valueOf(this.banque.getHistorique().size()),
                "tests dans l'historique"));
        tuiles.getChildren().add(Style.tuile(this.calculerMoyenne(), "réussite moyenne (historique)"));
        contenu.getChildren().add(tuiles);

        contenu.getChildren().add(this.creerCartePreparation());

        if (nom.isEmpty()) {
            Label astuce = Style.label("Astuce : renseignez votre nom dans les Paramètres pour des messages "
                    + "de résultat personnalisés.", "message-info");
            astuce.setMaxWidth(Double.MAX_VALUE);
            contenu.getChildren().add(astuce);
        }
        this.setCenter(Style.defilable(contenu));
    }

    private String calculerMoyenne() {
        ArrayList<Tentative> historique = this.banque.getHistorique();
        if (historique.isEmpty()) {
            return "–";
        }
        int total = 0;
        for (Tentative tentative : historique) {
            total = total + tentative.getPourcentage();
        }
        return Math.round(total / (double) historique.size()) + " %";
    }

    private VBox creerCartePreparation() {
        this.choixNiveau = new SelecteurSegmente(new String[]{"Indifférent", "1 · Facile", "2 · Moyen", "3 · Difficile"}, 0);
        this.choixNombre = new SelecteurSegmente(new String[]{"5", "10", "20"}, 1);
        this.choixCategorie = new ComboBox<>();
        this.choixCategorie.getItems().add(TOUTES);
        for (Categorie categorie : this.banque.getCategories()) {
            this.choixCategorie.getItems().add(categorie.getNom());
        }
        this.choixCategorie.getSelectionModel().select(0);
        this.choixCategorie.setPrefWidth(320);

        GridPane grille = new GridPane();
        grille.setHgap(18);
        grille.setVgap(14);
        grille.add(Style.label("Difficulté", "texte-gras"), 0, 0);
        grille.add(this.choixNiveau, 1, 0);
        grille.add(Style.label("Catégorie", "texte-gras"), 0, 1);
        grille.add(this.choixCategorie, 1, 1);
        grille.add(Style.label("Nombre de questions", "texte-gras"), 0, 2);
        grille.add(this.choixNombre, 1, 2);

        this.disponibles = Style.texteDiscret("");
        this.choixNiveau.setActionChangement(this::mettreAJourDisponibles);
        this.choixNombre.setActionChangement(this::mettreAJourDisponibles);
        this.choixCategorie.setOnAction(evenement -> this.mettreAJourDisponibles());
        this.mettreAJourDisponibles();

        Button lancer = Style.boutonPrincipal("Lancer le questionnaire");
        lancer.setDefaultButton(true);
        lancer.setOnAction(evenement -> this.lancer());
        HBox actions = new HBox(16, lancer, this.disponibles);
        actions.setAlignment(Pos.CENTER_LEFT);

        return Style.carte(Style.titreCarte("Nouveau questionnaire"), grille, actions);
    }

    private int getNiveau() {
        return this.choixNiveau.getIndexSelectionne();
    }

    private Categorie getCategorie() {
        if (this.choixCategorie.getSelectionModel().getSelectedIndex() <= 0) {
            return null;
        }
        return this.banque.chercherCategorie(this.choixCategorie.getValue());
    }

    private void mettreAJourDisponibles() {
        int nombre = this.banque.rechercherQuestions(this.getNiveau(), this.getCategorie()).size();
        if (nombre == 0) {
            this.disponibles.setText("Aucune question ne correspond à ces critères.");
        } else {
            this.disponibles.setText(nombre + " question(s) disponible(s) avec ces critères.");
        }
    }

    private void lancer() {
        int nombre = NOMBRES[this.choixNombre.getIndexSelectionne()];
        ArrayList<Question> disponiblesCriteres = this.banque.rechercherQuestions(this.getNiveau(), this.getCategorie());
        if (disponiblesCriteres.isEmpty()) {
            Dialogues.information("Aucune question", "Aucune question de la banque ne correspond à ces critères. "
                    + "Choisissez un autre niveau ou une autre catégorie, ou ajoutez des questions.");
            return;
        }
        if (disponiblesCriteres.size() < nombre) {
            boolean poursuivre = Dialogues.confirmer("Questions insuffisantes",
                    "Vous avez demandé " + nombre + " questions, mais la banque ne peut en proposer que "
                            + disponiblesCriteres.size() + " avec ces critères.\n\nVoulez-vous poursuivre avec "
                            + disponiblesCriteres.size() + " question(s) ?",
                    "Poursuivre");
            if (!poursuivre) {
                return;
            }
        }
        this.application.lancerQuestionnaire(
                this.banque.creerQuestionnaire(this.getNiveau(), this.getCategorie(), nombre), null);
    }
}
