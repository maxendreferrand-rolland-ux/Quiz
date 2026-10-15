package org.example.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Notice d'utilisation, notamment le format du fichier CSV d'importation.
 */
public class VueNotice extends BorderPane {

    public VueNotice(ApplicationQuiz application) {
        VBox contenu = Style.vue();
        contenu.getChildren().add(Style.titre("Notice d'utilisation"));
        contenu.getChildren().add(Style.sousTitre("Tout ce qu'il faut savoir pour utiliser l'application Quiz."));

        contenu.getChildren().add(section("Jouer",
                "Depuis « Accueil & jouer », choisissez une difficulté (ou « Indifférent »), éventuellement une "
                        + "catégorie, et le nombre de questions (5, 10 ou 20). Si la banque ne contient pas assez de "
                        + "questions, l'application vous indique combien elle peut en proposer et vous demande si vous "
                        + "souhaitez poursuivre.\n\n"
                        + "Les questions et leurs propositions sont présentées dans un ordre aléatoire. Vous pouvez "
                        + "passer une question, revenir en arrière avec les pastilles numérotées et valider à tout "
                        + "moment : une question sans réponse compte comme fausse. Le résultat affiche le nombre et le "
                        + "pourcentage de bonnes réponses, puis le corrigé avec le feedback de chaque question."));

        contenu.getChildren().add(section("Perfectionnement avancé",
                "Ce mode propose 5, 10, 20 ou 30 questions tirées au hasard parmi celles auxquelles vous avez mal "
                        + "répondu (ou pas répondu) dans l'historique. Une question n'y figure plus dès que votre "
                        + "réponse la plus récente est juste. Vous pouvez filtrer par difficulté."));

        contenu.getChildren().add(section("Catégories et questions",
                "Chaque question appartient à une seule catégorie. La catégorie « Général » existe toujours et ne "
                        + "peut être ni renommée ni supprimée. Deux catégories ne peuvent pas porter le même nom "
                        + "(majuscules et espaces ne comptent pas). Supprimer une catégorie supprime ses questions, "
                        + "après confirmation.\n\n"
                        + "Une question comporte un niveau (1 facile, 2 moyen, 3 difficile), un libellé, une "
                        + "proposition juste, de 1 à 4 propositions fausses et un feedback facultatif. Deux questions "
                        + "sont identiques si elles ont la même catégorie, le même libellé et les mêmes propositions "
                        + "(le niveau et le feedback ne comptent pas) : une question identique est refusée."));

        VBox sectionCsv = section("Format du fichier CSV",
                "Préparez une question par ligne dans un tableur, puis enregistrez au format « CSV (séparateur : "
                        + "point-virgule) ». Le point-virgule est le séparateur attendu ; si le fichier n'en contient "
                        + "pas, la virgule est utilisée. Les colonnes sont, dans l'ordre :");
        sectionCsv.getChildren().add(creerTableauExemple());
        sectionCsv.getChildren().add(Style.label(
                "• Colonnes 1 à 5 obligatoires : catégorie, niveau (1, 2 ou 3), libellé, proposition juste et au moins "
                        + "une proposition fausse.\n"
                        + "• Colonnes 6 à 8 facultatives : jusqu'à 3 autres propositions fausses (4 au total).\n"
                        + "• Colonne 9 facultative : le feedback. Il doit toujours être dans la 9e colonne, même si "
                        + "certaines propositions fausses sont vides.\n"
                        + "• Une première ligne d'en-tête (contenant le mot « Niveau ») est reconnue et ignorée. Les "
                        + "lignes vides sont ignorées.\n"
                        + "• Un texte contenant un point-virgule doit être entre guillemets (le tableur le fait "
                        + "automatiquement).\n"
                        + "• Les fichiers enregistrés en UTF-8 ou par Excel (Windows-1252) sont acceptés.\n\n"
                        + "À l'importation, les catégories inconnues sont créées automatiquement, les questions "
                        + "identiques à une question existante ne sont pas importées, et chaque ligne incorrecte est "
                        + "signalée avec son numéro et la cause de l'erreur. Les autres lignes sont importées "
                        + "normalement.", ""));
        contenu.getChildren().add(sectionCsv);

        contenu.getChildren().add(section("Partage réseau",
                "Les deux ordinateurs doivent être sur le même réseau.\n"
                        + "1. Sur l'ordinateur qui reçoit : « Partage réseau » → « Démarrer la réception ». Notez "
                        + "l'adresse IP et le port affichés (modifiables dans les Paramètres).\n"
                        + "2. Sur l'ordinateur qui envoie : saisissez cette adresse IP et ce port, choisissez les "
                        + "questions (toute la banque, des catégories ou des questions précises) puis « Envoyer ».\n"
                        + "Les deux ordinateurs calculent une clé secrète commune par un échange de Diffie-Hellman ; "
                        + "les questions sont chiffrées avec l'algorithme de Vigenère avant l'envoi. À la réception, "
                        + "elles sont vérifiées comme lors d'un import CSV. La connexion se ferme toute seule à la fin."));

        contenu.getChildren().add(section("Historique",
                "Les derniers tests (10 par défaut) sont conservés avec leur date, leur catégorie, leur niveau et "
                        + "leur pourcentage de réussite. Vous pouvez consulter leur corrigé, les supprimer ou les "
                        + "refaire : un message compare alors votre nouveau score au précédent. Les questions d'un test "
                        + "sont mémorisées telles qu'elles étaient : un test reste consultable et rejouable même si ses "
                        + "questions ont été modifiées ou supprimées depuis."));

        contenu.getChildren().add(section("Paramètres et sauvegarde",
                "Les paramètres permettent de saisir votre nom, le nombre de tests mémorisés et les paramètres "
                        + "réseau. Toutes les données sont enregistrées automatiquement après chaque modification "
                        + "dans le fichier quiz_data.ser, placé dans le dossier de l'application."));

        this.setCenter(Style.defilable(contenu));
    }

    private static VBox section(String titre, String texte) {
        Label paragraphe = new Label(texte);
        paragraphe.setWrapText(true);
        return Style.carte(Style.label(titre, "titre-section"), paragraphe);
    }

    private static GridPane creerTableauExemple() {
        String[][] lignes = {
            {"Catégorie", "Niveau", "Libellé", "Juste", "Fausse 1", "Fausse 2", "Fausse 3", "Fausse 4", "Feedback"},
            {"Bases du langage Java", "1", "Quel est le type de '9' ?", "char", "String", "int", "", "", "Apostrophes = caractère"},
            {"Tennis", "2", "Surface de Roland-Garros ?", "Terre battue", "Gazon", "", "", "", ""}
        };
        GridPane tableau = new GridPane();
        tableau.getStyleClass().add("tableau-exemple");
        for (int ligne = 0; ligne < lignes.length; ligne++) {
            for (int colonne = 0; colonne < lignes[ligne].length; colonne++) {
                Label cellule = new Label(lignes[ligne][colonne]);
                cellule.getStyleClass().add("cellule");
                if (ligne == 0) {
                    cellule.getStyleClass().add("cellule-entete");
                }
                cellule.setMaxWidth(Double.MAX_VALUE);
                cellule.setMaxHeight(Double.MAX_VALUE);
                cellule.setStyle("-fx-font-size: 12px;");
                tableau.add(cellule, colonne, ligne);
            }
        }
        return tableau;
    }
}
