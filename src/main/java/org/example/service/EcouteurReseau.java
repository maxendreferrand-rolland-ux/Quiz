package org.example.service;

/**
 * Reçoit les événements d'un échange réseau. Les méthodes sont appelées depuis
 * le thread réseau : l'interface graphique doit utiliser Platform.runLater.
 */
public interface EcouteurReseau {

    /** étape de l'échange à afficher dans le journal */
    void information(String message);

    /** données reçues et déchiffrées (côté réception uniquement) */
    void donneesRecues(String contenu);

    /** problème ayant interrompu l'échange */
    void erreur(String message);

    /** fin de l'échange (avec ou sans erreur), la connexion est fermée */
    void termine();
}
