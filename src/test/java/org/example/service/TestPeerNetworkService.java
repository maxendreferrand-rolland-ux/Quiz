package org.example.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration du transfert chiffré entre deux pairs sur la machine locale.
 */
class TestPeerNetworkService {

    /* écouteur qui mémorise les événements */
    private static class Journal implements EcouteurReseau {
        private ArrayList<String> erreurs = new ArrayList<>();
        private String donnees = null;
        private CountDownLatch fin = new CountDownLatch(1);

        @Override
        public void information(String message) {
        }

        @Override
        public void donneesRecues(String contenu) {
            this.donnees = contenu;
        }

        @Override
        public void erreur(String message) {
            this.erreurs.add(message);
        }

        @Override
        public void termine() {
            this.fin.countDown();
        }
    }

    @Test
    void transfertChiffreEntreDeuxPairs() throws Exception {
        int port = 6111;
        PeerNetworkService recepteur = new PeerNetworkService();
        PeerNetworkService emetteur = new PeerNetworkService();
        Journal journalReception = new Journal();
        Journal journalEnvoi = new Journal();
        String contenu = "Java;1;Quel est le type de '9' ?;char;String;int;;;Accents : éèà ✓\n";

        recepteur.demarrerReception(port, journalReception);
        Thread.sleep(300);
        assertTrue(recepteur.estEnEcoute());
        emetteur.envoyer("127.0.0.1", port, contenu, journalEnvoi);

        assertTrue(journalEnvoi.fin.await(15, TimeUnit.SECONDS));
        assertTrue(journalReception.fin.await(15, TimeUnit.SECONDS));
        assertTrue(journalEnvoi.erreurs.isEmpty(), journalEnvoi.erreurs.toString());
        assertTrue(journalReception.erreurs.isEmpty(), journalReception.erreurs.toString());
        assertEquals(contenu, journalReception.donnees);
        assertFalse(recepteur.estEnEcoute());
    }

    @Test
    void envoiVersUnPairAbsent() throws Exception {
        Journal journal = new Journal();
        new PeerNetworkService().envoyer("127.0.0.1", 6112, "x", journal);
        assertTrue(journal.fin.await(15, TimeUnit.SECONDS));
        assertEquals(1, journal.erreurs.size());
    }

    @Test
    void arretDeLaReception() throws Exception {
        PeerNetworkService recepteur = new PeerNetworkService();
        Journal journal = new Journal();
        recepteur.demarrerReception(6113, journal);
        Thread.sleep(300);
        recepteur.arreterReception();
        assertTrue(journal.fin.await(5, TimeUnit.SECONDS));
        assertTrue(journal.erreurs.isEmpty());
        assertFalse(recepteur.estEnEcoute());
    }
}
