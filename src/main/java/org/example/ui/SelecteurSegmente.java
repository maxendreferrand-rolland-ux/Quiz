package org.example.ui;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

/**
 * Groupe de boutons côte à côte dont un seul est sélectionné (ex. : 5 | 10 | 20).
 * Il y a toujours un bouton sélectionné.
 */
public class SelecteurSegmente extends HBox {

    private ToggleGroup groupe;
    private ArrayList<ToggleButton> boutons;
    private Runnable actionChangement;

    /**
     * @param libelles texte de chaque bouton
     * @param indexInitial bouton sélectionné au départ
     */
    public SelecteurSegmente(String[] libelles, int indexInitial) {
        this.groupe = new ToggleGroup();
        this.boutons = new ArrayList<>();
        this.actionChangement = null;
        for (int i = 0; i < libelles.length; i++) {
            ToggleButton bouton = new ToggleButton(libelles[i]);
            bouton.setToggleGroup(this.groupe);
            bouton.getStyleClass().add("segment");
            if (i == 0) {
                bouton.getStyleClass().add("premier");
            }
            if (i == libelles.length - 1) {
                bouton.getStyleClass().add("dernier");
            }
            this.boutons.add(bouton);
            this.getChildren().add(bouton);
        }
        this.boutons.get(indexInitial).setSelected(true);
        this.groupe.selectedToggleProperty().addListener((observable, ancien, nouveau) -> {
            if (nouveau == null) {
                ancien.setSelected(true);
            } else if (this.actionChangement != null) {
                this.actionChangement.run();
            }
        });
    }

    public int getIndexSelectionne() {
        return this.boutons.indexOf(this.groupe.getSelectedToggle());
    }

    public void setIndexSelectionne(int index) {
        this.boutons.get(index).setSelected(true);
    }

    /**
     * @param action traitement exécuté à chaque changement de sélection
     */
    public void setActionChangement(Runnable action) {
        this.actionChangement = action;
    }
}
