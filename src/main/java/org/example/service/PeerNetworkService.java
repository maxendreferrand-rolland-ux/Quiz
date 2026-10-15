package org.example.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Échange de questions entre deux pairs avec des sockets en mode connecté.
 * Le pair qui reçoit attend une connexion (ServerSocket) ; le pair qui envoie s'y connecte (Socket).
 * Chaque échange se déroule dans un thread dédié pour ne pas bloquer l'interface,
 * et la connexion est fermée automatiquement à la fin de l'échange.
 *
 * Protocole :
 *  1. l'émetteur envoie l'identifiant du protocole
 *  2. le récepteur envoie sa clé publique Diffie-Hellman, l'émetteur répond avec la sienne
 *  3. chacun calcule le secret partagé et en dérive la clé de Vigenère
 *  4. l'émetteur envoie la taille puis les données CSV chiffrées
 *  5. le récepteur déchiffre et renvoie un accusé de réception
 */
public class PeerNetworkService {

    public static final String IDENTIFIANT_PROTOCOLE = "QUIZ-P2P-1";
    public static final String ACCUSE_RECEPTION = "OK";
    public static final int DELAI_CONNEXION_MS = 10000;
    public static final int DELAI_LECTURE_MS = 30000;
    public static final int TAILLE_MAX_DONNEES = 50 * 1024 * 1024;

    private volatile ServerSocket serveur;
    private volatile boolean enEcoute;
    private volatile boolean arretDemande;

    public PeerNetworkService() {
        this.serveur = null;
        this.enEcoute = false;
        this.arretDemande = false;
    }

    public boolean estEnEcoute() {
        return this.enEcoute;
    }

    /* ---------------------------------------------------------------- réception */

    /**
     * Ouvre le port et attend, dans un thread séparé, qu'un pair envoie des questions.
     * Une seule connexion est acceptée, puis le port est refermé.
     * @param port port d'écoute
     * @param ecouteur destinataire des événements
     * @throws IllegalStateException si une réception est déjà en cours
     */
    public void demarrerReception(int port, EcouteurReseau ecouteur) {
        if (this.enEcoute) {
            throw new IllegalStateException("Une réception est déjà en cours.");
        }
        this.enEcoute = true;
        this.arretDemande = false;
        Thread thread = new Thread(() -> this.executerReception(port, ecouteur), "quiz-reception");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Arrête l'attente d'une connexion (sans effet si un échange est déjà commencé).
     */
    public void arreterReception() {
        this.arretDemande = true;
        this.fermerServeur();
    }

    private void fermerServeur() {
        ServerSocket serveurCourant = this.serveur;
        if (serveurCourant != null) {
            try {
                serveurCourant.close();
            } catch (IOException e) {
                /* la fermeture a échoué : le port sera libéré à l'arrêt de l'application */
            }
        }
    }

    private void executerReception(int port, EcouteurReseau ecouteur) {
        Socket socket = null;
        try {
            ServerSocket nouveauServeur = new ServerSocket();
            nouveauServeur.setReuseAddress(true);
            nouveauServeur.bind(new InetSocketAddress(port));
            this.serveur = nouveauServeur;
            ecouteur.information("En attente d'un pair sur le port " + port + "…");
            socket = nouveauServeur.accept();
            nouveauServeur.close();
            this.serveur = null;
            socket.setSoTimeout(DELAI_LECTURE_MS);
            ecouteur.information("Connexion établie avec " + socket.getInetAddress().getHostAddress() + ".");

            DataInputStream entree = new DataInputStream(socket.getInputStream());
            DataOutputStream sortie = new DataOutputStream(socket.getOutputStream());

            String identifiant = entree.readUTF();
            if (!IDENTIFIANT_PROTOCOLE.equals(identifiant)) {
                throw new IOException("le pair n'utilise pas le protocole de l'application Quiz.");
            }

            DiffieHellmanVigenereCrypto crypto = new DiffieHellmanVigenereCrypto();
            sortie.writeUTF(crypto.getClePublique().toString());
            sortie.flush();
            BigInteger clePair = new BigInteger(entree.readUTF());
            crypto.calculerSecretPartage(clePair);
            ecouteur.information("Échange de Diffie-Hellman terminé : clé de Vigenère de "
                    + crypto.getCleVigenere().length() + " caractères dérivée du secret partagé.");

            int taille = entree.readInt();
            if (taille < 0 || taille > TAILLE_MAX_DONNEES) {
                throw new IOException("taille de données annoncée incorrecte (" + taille + " octets).");
            }
            byte[] octets = new byte[taille];
            entree.readFully(octets);
            String texteChiffre = new String(octets, StandardCharsets.US_ASCII);
            ecouteur.information(taille + " octets chiffrés reçus, déchiffrement…");
            String texteClair = crypto.dechiffrer(texteChiffre);

            sortie.writeUTF(ACCUSE_RECEPTION);
            sortie.flush();
            ecouteur.information("Données déchiffrées, accusé de réception envoyé.");
            ecouteur.donneesRecues(texteClair);
        } catch (BindException e) {
            ecouteur.erreur("Le port " + port + " est déjà utilisé. Choisissez un autre port dans les paramètres.");
        } catch (SocketTimeoutException e) {
            ecouteur.erreur("Le pair ne répond plus (délai dépassé). Échange annulé.");
        } catch (SocketException e) {
            if (this.arretDemande) {
                ecouteur.information("Réception arrêtée.");
            } else {
                ecouteur.erreur("Erreur réseau : " + e.getMessage());
            }
        } catch (EOFException e) {
            ecouteur.erreur("La connexion a été interrompue par le pair avant la fin de l'échange.");
        } catch (IOException e) {
            ecouteur.erreur("Erreur pendant la réception : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ecouteur.erreur("Données reçues invalides : " + e.getMessage());
        } finally {
            fermer(socket);
            this.fermerServeur();
            this.serveur = null;
            this.enEcoute = false;
            ecouteur.termine();
        }
    }

    /* ---------------------------------------------------------------- envoi */

    /**
     * Se connecte au pair et lui envoie les données chiffrées, dans un thread séparé.
     * @param adresse adresse IP du pair (qui doit être en réception)
     * @param port port d'écoute du pair
     * @param contenu données à envoyer (texte CSV)
     * @param ecouteur destinataire des événements
     */
    public void envoyer(String adresse, int port, String contenu, EcouteurReseau ecouteur) {
        Thread thread = new Thread(() -> this.executerEnvoi(adresse, port, contenu, ecouteur), "quiz-envoi");
        thread.setDaemon(true);
        thread.start();
    }

    private void executerEnvoi(String adresse, int port, String contenu, EcouteurReseau ecouteur) {
        Socket socket = new Socket();
        try {
            ecouteur.information("Connexion à " + adresse + ":" + port + "…");
            socket.connect(new InetSocketAddress(adresse, port), DELAI_CONNEXION_MS);
            socket.setSoTimeout(DELAI_LECTURE_MS);
            ecouteur.information("Connecté au pair.");

            DataInputStream entree = new DataInputStream(socket.getInputStream());
            DataOutputStream sortie = new DataOutputStream(socket.getOutputStream());

            sortie.writeUTF(IDENTIFIANT_PROTOCOLE);
            sortie.flush();
            BigInteger clePair = new BigInteger(entree.readUTF());
            DiffieHellmanVigenereCrypto crypto = new DiffieHellmanVigenereCrypto();
            sortie.writeUTF(crypto.getClePublique().toString());
            sortie.flush();
            crypto.calculerSecretPartage(clePair);
            ecouteur.information("Échange de Diffie-Hellman terminé : clé de Vigenère de "
                    + crypto.getCleVigenere().length() + " caractères dérivée du secret partagé.");

            String texteChiffre = crypto.chiffrer(contenu);
            byte[] octets = texteChiffre.getBytes(StandardCharsets.US_ASCII);
            sortie.writeInt(octets.length);
            sortie.write(octets);
            sortie.flush();
            ecouteur.information(octets.length + " octets chiffrés envoyés, attente de l'accusé de réception…");

            String reponse = entree.readUTF();
            if (ACCUSE_RECEPTION.equals(reponse)) {
                ecouteur.information("Le pair a bien reçu les questions. Connexion fermée.");
            } else {
                ecouteur.erreur("Réponse inattendue du pair : " + reponse);
            }
        } catch (UnknownHostException e) {
            ecouteur.erreur("Adresse inconnue : " + adresse);
        } catch (ConnectException e) {
            ecouteur.erreur("Impossible de joindre " + adresse + ":" + port
                    + ". Vérifiez que le pair a bien démarré la réception et que l'adresse et le port sont corrects.");
        } catch (SocketTimeoutException e) {
            ecouteur.erreur("Le pair ne répond pas (délai dépassé).");
        } catch (EOFException e) {
            ecouteur.erreur("La connexion a été fermée par le pair avant la fin de l'échange.");
        } catch (IOException e) {
            ecouteur.erreur("Erreur pendant l'envoi : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ecouteur.erreur("Échange refusé : " + e.getMessage());
        } finally {
            fermer(socket);
            ecouteur.termine();
        }
    }

    private static void fermer(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                /* rien à faire de plus */
            }
        }
    }
}
