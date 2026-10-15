package org.example.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Chiffrement des échanges réseau.
 * 1) Échange de Diffie-Hellman : chaque pair tire une clé privée a, envoie g^a mod p,
 *    et calcule le secret partagé (g^b)^a mod p = (g^a)^b mod p.
 * 2) Dérivation de la clé : le secret est écrit en base 64 ; chaque « chiffre » est
 *    une lettre de l'alphabet Base64 (A=0, B=1, ..., /=63). On obtient une clé texte.
 * 3) Vigenère : le message (texte UTF-8) est d'abord converti en Base64 pour n'avoir
 *    que 64 symboles possibles, puis chaque symbole est décalé de la valeur de la lettre
 *    de clé correspondante, modulo 64. Le caractère de remplissage = n'est pas chiffré.
 */
public class DiffieHellmanVigenereCrypto {

    /** nombre premier de 2048 bits du groupe 14 (RFC 3526) */
    public static final BigInteger P_DEFAUT = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
                    + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
                    + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
                    + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                    + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
                    + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
                    + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
                    + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
                    + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"
                    + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"
                    + "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);

    /** générateur associé */
    public static final BigInteger G_DEFAUT = BigInteger.valueOf(2);

    /** alphabet Base64 : la position d'un caractère est sa valeur (de 0 à 63) */
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static final BigInteger TAILLE_ALPHABET = BigInteger.valueOf(64);

    private BigInteger p;
    private BigInteger g;
    private BigInteger clePrivee;
    private BigInteger clePublique;
    private BigInteger secretPartage;
    private String cleVigenere;

    /**
     * Utilise les paramètres standards et tire une clé privée au hasard.
     */
    public DiffieHellmanVigenereCrypto() {
        this(P_DEFAUT, G_DEFAUT);
    }

    /**
     * @param p nombre premier
     * @param g générateur
     */
    public DiffieHellmanVigenereCrypto(BigInteger p, BigInteger g) {
        this(p, g, tirerClePrivee(p));
    }

    /**
     * Constructeur à clé privée imposée (exemples et tests).
     * @param p nombre premier
     * @param g générateur
     * @param clePrivee clé privée, comprise entre 2 et p - 2
     */
    public DiffieHellmanVigenereCrypto(BigInteger p, BigInteger g, BigInteger clePrivee) {
        this.p = p;
        this.g = g;
        this.clePrivee = clePrivee;
        this.clePublique = g.modPow(clePrivee, p);
        this.secretPartage = null;
        this.cleVigenere = null;
    }

    /* tire un entier au hasard entre 2 et p - 2 */
    private static BigInteger tirerClePrivee(BigInteger p) {
        SecureRandom hasard = new SecureRandom();
        BigInteger maximum = p.subtract(BigInteger.valueOf(3));
        BigInteger cle = new BigInteger(p.bitLength(), hasard);
        while (cle.compareTo(maximum) > 0) {
            cle = new BigInteger(p.bitLength(), hasard);
        }
        return cle.add(BigInteger.TWO);
    }

    /**
     * @return g^a mod p, à envoyer au pair
     */
    public BigInteger getClePublique() {
        return this.clePublique;
    }

    public BigInteger getSecretPartage() {
        return this.secretPartage;
    }

    public String getCleVigenere() {
        return this.cleVigenere;
    }

    /**
     * Calcule le secret partagé à partir de la clé publique du pair, puis en dérive la clé de Vigenère.
     * @param clePubliquePair valeur g^b mod p reçue du pair
     * @throws IllegalArgumentException si la clé reçue n'est pas valide
     */
    public void calculerSecretPartage(BigInteger clePubliquePair) {
        BigInteger maximum = this.p.subtract(BigInteger.ONE);
        if (clePubliquePair == null || clePubliquePair.compareTo(BigInteger.ONE) <= 0
                || clePubliquePair.compareTo(maximum) >= 0) {
            throw new IllegalArgumentException("La clé publique reçue du pair n'est pas valide.");
        }
        this.secretPartage = clePubliquePair.modPow(this.clePrivee, this.p);
        this.cleVigenere = deriverCle(this.secretPartage);
    }

    /**
     * Écrit le secret en base 64 : on divise successivement par 64 et chaque reste
     * donne une lettre de l'alphabet (le chiffre de poids fort est placé en premier).
     * Exemple : 4097 = 1×64² + 0×64 + 1, donc la clé est « BAB ».
     * @param secret entier strictement positif
     * @return la clé texte
     */
    public static String deriverCle(BigInteger secret) {
        if (secret.signum() <= 0) {
            throw new IllegalArgumentException("Le secret partagé doit être strictement positif.");
        }
        String cle = "";
        BigInteger reste = secret;
        while (reste.signum() > 0) {
            int chiffre = reste.mod(TAILLE_ALPHABET).intValue();
            cle = ALPHABET.charAt(chiffre) + cle;
            reste = reste.divide(TAILLE_ALPHABET);
        }
        return cle;
    }

    /**
     * Chiffre un texte avec la clé dérivée de l'échange de Diffie-Hellman.
     * @param texteClair texte quelconque (accents, symboles…)
     * @return le texte chiffré, composé uniquement de caractères Base64
     */
    public String chiffrer(String texteClair) {
        verifierCle();
        String base64 = Base64.getEncoder().encodeToString(texteClair.getBytes(StandardCharsets.UTF_8));
        return chiffrerVigenere(base64, this.cleVigenere);
    }

    /**
     * @param texteChiffre texte produit par chiffrer avec la même clé
     * @return le texte clair
     * @throws IllegalArgumentException si les données ne sont pas un texte chiffré valide
     */
    public String dechiffrer(String texteChiffre) {
        verifierCle();
        String base64 = dechiffrerVigenere(texteChiffre, this.cleVigenere);
        try {
            return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Les données reçues sont corrompues ou la clé ne correspond pas.");
        }
    }

    private void verifierCle() {
        if (this.cleVigenere == null) {
            throw new IllegalStateException("L'échange de Diffie-Hellman n'a pas encore eu lieu.");
        }
    }

    /**
     * Vigenère sur l'alphabet Base64 : c = (m + k) mod 64.
     * Les caractères hors alphabet sont recopiés et ne font pas avancer la clé.
     * @param texte texte composé de caractères Base64
     * @param cle clé composée de caractères Base64
     * @return le texte chiffré
     */
    public static String chiffrerVigenere(String texte, String cle) {
        return appliquerVigenere(texte, cle, 1);
    }

    /**
     * Opération inverse : m = (c - k) mod 64.
     * @param texte texte chiffré
     * @param cle clé utilisée pour chiffrer
     * @return le texte déchiffré
     */
    public static String dechiffrerVigenere(String texte, String cle) {
        return appliquerVigenere(texte, cle, -1);
    }

    private static String appliquerVigenere(String texte, String cle, int sens) {
        if (cle == null || cle.isEmpty()) {
            throw new IllegalArgumentException("La clé de Vigenère est vide.");
        }
        char[] resultat = new char[texte.length()];
        int positionCle = 0;
        for (int i = 0; i < texte.length(); i++) {
            char caractere = texte.charAt(i);
            int valeur = ALPHABET.indexOf(caractere);
            if (valeur == -1) {
                resultat[i] = caractere;
            } else {
                int decalage = ALPHABET.indexOf(cle.charAt(positionCle % cle.length()));
                if (decalage == -1) {
                    throw new IllegalArgumentException("La clé contient un caractère hors de l'alphabet Base64.");
                }
                int nouvelleValeur = Math.floorMod(valeur + sens * decalage, 64);
                resultat[i] = ALPHABET.charAt(nouvelleValeur);
                positionCle++;
            }
        }
        return new String(resultat);
    }
}
