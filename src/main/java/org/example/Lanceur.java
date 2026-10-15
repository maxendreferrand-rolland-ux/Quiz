package org.example;

import javafx.application.Application;
import org.example.ui.ApplicationQuiz;

/**
 * Point d'entrée du jar exécutable.
 * Cette classe n'hérite pas de Application : c'est ce qui permet de lancer
 * l'application JavaFX depuis un jar contenant toutes les dépendances.
 */
public class Lanceur {

    public static void main(String[] args) {
        Application.launch(ApplicationQuiz.class, args);
    }
}
