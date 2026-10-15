package org.example.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Feuille de style de l'application (écrite en Java, chargée sous forme d'URL data:)
 * et petites fabriques de composants pour garder un style homogène.
 */
public class Style {

    private static final String CSS = """
            .root {
                -fx-font-family: "Segoe UI", "Helvetica Neue", "Liberation Sans", Arial, sans-serif;
                -fx-font-size: 14px;
                -fx-background-color: #f3f5fa;
                -fx-accent: #3056d3;
                -fx-focus-color: #3056d3;
            }
            .menu-lateral {
                -fx-background-color: #172036;
                -fx-padding: 22 14 18 14;
                -fx-spacing: 3;
                -fx-pref-width: 250;
                -fx-min-width: 250;
            }
            .menu-logo { -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-padding: 0 10; }
            .menu-sous-titre { -fx-text-fill: #8b97b5; -fx-font-size: 12px; -fx-padding: 0 10 10 10; }
            .menu-section { -fx-text-fill: #5f6b8a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 14 10 4 10; }
            .bouton-menu {
                -fx-background-color: transparent;
                -fx-text-fill: #c9d1e6;
                -fx-alignment: center-left;
                -fx-padding: 9 12;
                -fx-background-radius: 8;
                -fx-cursor: hand;
            }
            .bouton-menu:hover { -fx-background-color: #24304d; -fx-text-fill: white; }
            .bouton-menu.actif { -fx-background-color: #3056d3; -fx-text-fill: white; -fx-font-weight: bold; }
            .menu-pied { -fx-text-fill: #8b97b5; -fx-font-size: 12px; -fx-padding: 0 10; }

            .vue { -fx-padding: 28 32 32 32; -fx-spacing: 18; }
            .titre-vue { -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #111a2e; }
            .sous-titre { -fx-text-fill: #55607a; -fx-font-size: 14px; }
            .titre-carte { -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #111a2e; }
            .titre-section { -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111a2e; -fx-padding: 6 0 0 0; }
            .texte-discret { -fx-text-fill: #6b7690; -fx-font-size: 13px; }
            .texte-gras { -fx-font-weight: bold; -fx-text-fill: #1c2640; }
            .carte {
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-border-color: #e3e8f2;
                -fx-border-radius: 14;
                -fx-padding: 20 22;
                -fx-spacing: 12;
            }
            .carte-juste { -fx-border-color: #e3e8f2 #e3e8f2 #e3e8f2 #2e9e5b; -fx-border-width: 1 1 1 5; }
            .carte-fausse { -fx-border-color: #e3e8f2 #e3e8f2 #e3e8f2 #d64545; -fx-border-width: 1 1 1 5; }
            .tuile {
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-border-color: #e3e8f2;
                -fx-border-radius: 14;
                -fx-padding: 14 18;
                -fx-spacing: 2;
            }
            .tuile-valeur { -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3056d3; }
            .tuile-libelle { -fx-text-fill: #6b7690; -fx-font-size: 13px; }

            .button { -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16; }
            .bouton-principal { -fx-background-color: #3056d3; -fx-text-fill: white; -fx-font-weight: bold; }
            .bouton-principal:hover { -fx-background-color: #2445b5; }
            .bouton-secondaire { -fx-background-color: #e8ecf5; -fx-text-fill: #1c2640; }
            .bouton-secondaire:hover { -fx-background-color: #dbe1ee; }
            .bouton-danger { -fx-background-color: #fdecec; -fx-text-fill: #b42318; }
            .bouton-danger:hover { -fx-background-color: #f9d6d6; }
            .bouton-lien { -fx-background-color: transparent; -fx-text-fill: #3056d3; -fx-padding: 2 0; -fx-underline: true; }

            .segment {
                -fx-background-color: #eef1f7;
                -fx-text-fill: #1c2640;
                -fx-background-radius: 0;
                -fx-border-color: #d5dbe8;
                -fx-border-width: 1 0 1 1;
                -fx-padding: 7 16;
                -fx-cursor: hand;
            }
            .segment.premier { -fx-background-radius: 8 0 0 8; -fx-border-radius: 8 0 0 8; }
            .segment.dernier { -fx-background-radius: 0 8 8 0; -fx-border-radius: 0 8 8 0; -fx-border-width: 1; }
            .segment.premier.dernier { -fx-background-radius: 8; -fx-border-radius: 8; }
            .segment:selected { -fx-background-color: #3056d3; -fx-text-fill: white; -fx-border-color: #3056d3; }

            .etiquette {
                -fx-background-color: #e7ecfb;
                -fx-text-fill: #2b48b0;
                -fx-padding: 3 10;
                -fx-background-radius: 20;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
            }
            .etiquette-verte { -fx-background-color: #e3f6ea; -fx-text-fill: #17733b; }
            .etiquette-rouge { -fx-background-color: #fdecec; -fx-text-fill: #b42318; }
            .etiquette-grise { -fx-background-color: #eef0f4; -fx-text-fill: #55607a; }

            .libelle-question { -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111a2e; }
            .proposition {
                -fx-background-color: #f5f7fb;
                -fx-background-radius: 10;
                -fx-border-color: #e1e6f0;
                -fx-border-radius: 10;
                -fx-padding: 12 16;
                -fx-font-size: 15px;
                -fx-text-fill: #1c2640;
                -fx-cursor: hand;
            }
            .proposition:hover { -fx-border-color: #9fb2ec; }
            .proposition:selected { -fx-background-color: #eaf0ff; -fx-border-color: #3056d3; }
            .pastille {
                -fx-min-width: 34; -fx-min-height: 34; -fx-pref-width: 34; -fx-pref-height: 34;
                -fx-padding: 0;
                -fx-background-radius: 17;
                -fx-background-color: #e8ecf5;
                -fx-text-fill: #55607a;
                -fx-font-size: 12px;
            }
            .pastille-repondue { -fx-background-color: #cfdaf9; -fx-text-fill: #1f3a9a; -fx-font-weight: bold; }
            .pastille-courante { -fx-border-color: #3056d3; -fx-border-width: 2; -fx-border-radius: 17; }

            .score { -fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #3056d3; }
            .phrase-resultat { -fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #111a2e; }
            .message-succes { -fx-background-color: #e3f6ea; -fx-text-fill: #17733b; -fx-padding: 12 16; -fx-background-radius: 10; }
            .message-alerte { -fx-background-color: #fff4e0; -fx-text-fill: #8a5300; -fx-padding: 12 16; -fx-background-radius: 10; }
            .message-info { -fx-background-color: #eef3ff; -fx-text-fill: #233c8f; -fx-padding: 12 16; -fx-background-radius: 10; }
            .message-erreur { -fx-text-fill: #b42318; -fx-font-weight: bold; }
            .feedback {
                -fx-background-color: #f6f7fb;
                -fx-padding: 10 14;
                -fx-background-radius: 8;
                -fx-text-fill: #3a4560;
                -fx-font-style: italic;
            }
            .reponse-juste { -fx-text-fill: #17733b; -fx-font-weight: bold; }
            .reponse-fausse { -fx-text-fill: #b42318; -fx-font-weight: bold; }

            .defilement, .defilement > .viewport { -fx-background-color: transparent; }
            .defilement { -fx-padding: 0; -fx-background-insets: 0; }
            .progress-bar > .bar { -fx-background-color: #3056d3; -fx-background-radius: 6; -fx-background-insets: 0; -fx-padding: 4; }
            .progress-bar > .track { -fx-background-color: #e3e8f2; -fx-background-radius: 6; }
            .journal { -fx-font-family: "Consolas", "DejaVu Sans Mono", monospace; -fx-font-size: 12px; }
            .cellule { -fx-border-color: #d5dbe8; -fx-border-width: 0 1 1 0; -fx-padding: 6 10; -fx-background-color: white; }
            .cellule-entete { -fx-background-color: #eef1f7; -fx-font-weight: bold; }
            .tableau-exemple { -fx-border-color: #d5dbe8; -fx-border-width: 1 0 0 1; }
            .dialog-pane { -fx-background-color: white; }
            """;

    private Style() {
    }

    /**
     * @return la feuille de style sous forme d'URL data:
     */
    public static String getFeuilleDeStyle() {
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(CSS.getBytes(StandardCharsets.UTF_8));
    }

    public static void appliquer(Scene scene) {
        scene.getStylesheets().add(getFeuilleDeStyle());
    }

    public static void appliquer(DialogPane panneau) {
        panneau.getStylesheets().add(getFeuilleDeStyle());
    }

    public static Label label(String texte, String classe) {
        Label label = new Label(texte);
        if (!classe.isEmpty()) {
            label.getStyleClass().add(classe);
        }
        label.setWrapText(true);
        return label;
    }

    public static Label titre(String texte) {
        return label(texte, "titre-vue");
    }

    public static Label sousTitre(String texte) {
        return label(texte, "sous-titre");
    }

    public static Label titreCarte(String texte) {
        return label(texte, "titre-carte");
    }

    public static Label texteDiscret(String texte) {
        return label(texte, "texte-discret");
    }

    public static VBox carte(Node... enfants) {
        VBox carte = new VBox(enfants);
        carte.getStyleClass().add("carte");
        return carte;
    }

    public static VBox vue() {
        VBox contenu = new VBox();
        contenu.getStyleClass().add("vue");
        return contenu;
    }

    public static Button bouton(String texte, String classe) {
        Button bouton = new Button(texte);
        bouton.getStyleClass().add(classe);
        bouton.setMinWidth(Region.USE_PREF_SIZE);
        return bouton;
    }

    public static Button boutonPrincipal(String texte) {
        return bouton(texte, "bouton-principal");
    }

    public static Button boutonSecondaire(String texte) {
        return bouton(texte, "bouton-secondaire");
    }

    public static Button boutonDanger(String texte) {
        return bouton(texte, "bouton-danger");
    }

    public static Button boutonLien(String texte) {
        return bouton(texte, "bouton-lien");
    }

    /**
     * @param texte texte de l'étiquette
     * @param couleur "" (bleu), "etiquette-verte", "etiquette-rouge" ou "etiquette-grise"
     */
    public static Label etiquette(String texte, String couleur) {
        Label etiquette = new Label(texte);
        etiquette.getStyleClass().add("etiquette");
        if (!couleur.isEmpty()) {
            etiquette.getStyleClass().add(couleur);
        }
        return etiquette;
    }

    /**
     * @return un espace extensible pour pousser les éléments suivants à droite d'une HBox
     */
    public static Region espace() {
        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        return espace;
    }

    /**
     * @param contenu contenu à rendre défilable verticalement
     */
    public static ScrollPane defilable(Node contenu) {
        ScrollPane defilement = new ScrollPane(contenu);
        defilement.getStyleClass().add("defilement");
        defilement.setFitToWidth(true);
        defilement.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return defilement;
    }

    /**
     * Tuile de statistique (grande valeur + libellé).
     */
    public static VBox tuile(String valeur, String libelle) {
        VBox tuile = new VBox(label(valeur, "tuile-valeur"), label(libelle, "tuile-libelle"));
        tuile.getStyleClass().add("tuile");
        HBox.setHgrow(tuile, Priority.ALWAYS);
        tuile.setMaxWidth(Double.MAX_VALUE);
        return tuile;
    }
}
