package org.example.model;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Paramètres de l'application : nom du joueur, taille de l'historique, paramètres réseau.
 */
public class Parametres implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int LIMITE_HISTORIQUE_DEFAUT = 10;
    public static final int LIMITE_HISTORIQUE_MAX = 100;
    public static final int PORT_DEFAUT = 6000;
    public static final int PORT_MIN = 1024;
    public static final int PORT_MAX = 65535;

    private String nomJoueur;
    private int limiteHistorique;
    private String adresseIp;
    private int port;
    private String adresseIpPair;
    private int portPair;

    /**
     * Paramètres par défaut, avec l'adresse IP de la machine détectée automatiquement.
     */
    public Parametres() {
        this.nomJoueur = "";
        this.limiteHistorique = LIMITE_HISTORIQUE_DEFAUT;
        this.adresseIp = detecterAdresseIp();
        this.port = PORT_DEFAUT;
        this.adresseIpPair = "";
        this.portPair = PORT_DEFAUT;
    }

    public String getNomJoueur() {
        return this.nomJoueur;
    }

    public void setNomJoueur(String nomJoueur) {
        String nom = OutilsTexte.normaliser(nomJoueur);
        if (nom.length() > 40) {
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 40 caractères.");
        }
        this.nomJoueur = nom;
    }

    public int getLimiteHistorique() {
        return this.limiteHistorique;
    }

    public void setLimiteHistorique(int limiteHistorique) {
        if (limiteHistorique < 1 || limiteHistorique > LIMITE_HISTORIQUE_MAX) {
            throw new IllegalArgumentException("Le nombre de tests mémorisés doit être compris entre 1 et "
                    + LIMITE_HISTORIQUE_MAX + ".");
        }
        this.limiteHistorique = limiteHistorique;
    }

    public String getAdresseIp() {
        return this.adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = verifierAdresse(adresseIp);
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = verifierPort(port);
    }

    public String getAdresseIpPair() {
        return this.adresseIpPair;
    }

    public void setAdresseIpPair(String adresseIpPair) {
        this.adresseIpPair = verifierAdresse(adresseIpPair);
    }

    public int getPortPair() {
        return this.portPair;
    }

    public void setPortPair(int portPair) {
        this.portPair = verifierPort(portPair);
    }

    /**
     * @param adresse adresse IP ou nom de machine
     * @return l'adresse nettoyée
     * @throws IllegalArgumentException si l'adresse est vide ou contient des espaces
     */
    public static String verifierAdresse(String adresse) {
        String adresseNettoyee = OutilsTexte.normaliser(adresse);
        if (adresseNettoyee.isEmpty()) {
            throw new IllegalArgumentException("L'adresse IP est vide.");
        }
        if (adresseNettoyee.contains(" ")) {
            throw new IllegalArgumentException("L'adresse IP ne doit pas contenir d'espace.");
        }
        return adresseNettoyee;
    }

    /**
     * @param port numéro de port
     * @return le port s'il est valide
     * @throws IllegalArgumentException si le port est hors de l'intervalle autorisé
     */
    public static int verifierPort(int port) {
        if (port < PORT_MIN || port > PORT_MAX) {
            throw new IllegalArgumentException("Le port doit être compris entre " + PORT_MIN + " et " + PORT_MAX + ".");
        }
        return port;
    }

    /**
     * Si l'adresse enregistrée n'appartient plus à la machine (changement de réseau),
     * elle est remplacée par l'adresse détectée.
     */
    public void mettreAJourAdresseIpSiNecessaire() {
        ArrayList<String> adresses = listerAdressesLocales();
        if (!adresses.isEmpty() && !adresses.contains(this.adresseIp)) {
            this.adresseIp = adresses.get(0);
        }
    }

    /**
     * @return la première adresse IPv4 de la machine sur le réseau local, ou 127.0.0.1
     */
    public static String detecterAdresseIp() {
        ArrayList<String> adresses = listerAdressesLocales();
        if (adresses.isEmpty()) {
            return "127.0.0.1";
        }
        return adresses.get(0);
    }

    /**
     * @return les adresses IPv4 de la machine (hors boucle locale), réseaux privés en premier
     */
    public static ArrayList<String> listerAdressesLocales() {
        ArrayList<String> privees = new ArrayList<>();
        ArrayList<String> autres = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface carte = interfaces.nextElement();
                if (carte.isUp() && !carte.isLoopback() && !carte.isVirtual()) {
                    Enumeration<InetAddress> adresses = carte.getInetAddresses();
                    while (adresses.hasMoreElements()) {
                        InetAddress adresse = adresses.nextElement();
                        if (adresse instanceof Inet4Address && !adresse.isLinkLocalAddress()) {
                            if (adresse.isSiteLocalAddress()) {
                                privees.add(adresse.getHostAddress());
                            } else {
                                autres.add(adresse.getHostAddress());
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            /* aucune carte réseau lisible : la liste reste vide */
        }
        privees.addAll(autres);
        return privees;
    }
}
