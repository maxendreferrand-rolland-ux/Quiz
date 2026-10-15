package org.example.service;

import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.OutilsTexte;
import org.example.model.Question;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Importation et exportation de questions au format CSV.
 * Format d'une ligne (séparateur ; ou , en repli) :
 * Catégorie ; Niveau ; Libellé ; Proposition juste ; Fausse 1 ; [Fausse 2] ; [Fausse 3] ; [Fausse 4] ; [Feedback]
 * Le feedback est toujours dans la 9e colonne.
 */
public class ServiceCsv {

    public static final char SEPARATEUR = ';';
    public static final char SEPARATEUR_REPLI = ',';
    public static final int NB_COLONNES_MIN = 5;
    public static final int NB_COLONNES_MAX = 9;
    public static final long TAILLE_MAX_FICHIER = 10L * 1024 * 1024;

    private ServiceCsv() {
    }

    /* ---------------------------------------------------------------- lecture */

    /**
     * Importe un fichier CSV dans la banque. Les erreurs sont décrites dans le rapport.
     * @param fichier fichier à importer
     * @param banque banque qui reçoit les questions
     * @return le rapport d'importation
     */
    public static RapportImport importerFichier(File fichier, BanqueDeDonnees banque) {
        RapportImport rapport = new RapportImport();
        if (!fichier.isFile()) {
            rapport.setErreurFatale("le fichier « " + fichier.getName() + " » est introuvable.");
            return rapport;
        }
        if (fichier.length() > TAILLE_MAX_FICHIER) {
            rapport.setErreurFatale("le fichier dépasse 10 Mo, ce n'est sans doute pas un fichier de questions.");
            return rapport;
        }
        try {
            byte[] octets = Files.readAllBytes(fichier.toPath());
            return importerTexte(decoderOctets(octets), banque);
        } catch (IOException e) {
            rapport.setErreurFatale("impossible de lire le fichier (" + e.getMessage() + ").");
            return rapport;
        }
    }

    /**
     * Décode le contenu d'un fichier : UTF-8 (avec ou sans BOM), sinon Windows-1252
     * qui est l'encodage utilisé par Excel pour le format « CSV (séparateur : point-virgule) ».
     * @param octets contenu brut du fichier
     * @return le texte
     */
    public static String decoderOctets(byte[] octets) {
        if (octets.length >= 3 && (octets[0] & 0xFF) == 0xEF && (octets[1] & 0xFF) == 0xBB
                && (octets[2] & 0xFF) == 0xBF) {
            return new String(octets, 3, octets.length - 3, StandardCharsets.UTF_8);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(octets))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(octets, Charset.forName("windows-1252"));
        }
    }

    /**
     * Importe un texte au format CSV (contenu d'un fichier ou données reçues par le réseau).
     * @param texte contenu CSV
     * @param banque banque qui reçoit les questions
     * @return le rapport d'importation
     */
    public static RapportImport importerTexte(String texte, BanqueDeDonnees banque) {
        String contenu = texte;
        if (contenu.startsWith("﻿")) {
            contenu = contenu.substring(1);
        }
        String[] lignes = contenu.split("\r\n|\r|\n", -1);
        ArrayList<String> liste = new ArrayList<>();
        for (String ligne : lignes) {
            liste.add(ligne);
        }
        return importerLignes(liste, banque);
    }

    /**
     * Importe des lignes CSV. Chaque ligne correcte et nouvelle devient une question ;
     * les catégories manquantes sont créées ; les doublons et les lignes incorrectes sont signalés.
     * @param lignes lignes du fichier
     * @param banque banque qui reçoit les questions
     * @return le rapport d'importation
     */
    public static RapportImport importerLignes(ArrayList<String> lignes, BanqueDeDonnees banque) {
        RapportImport rapport = new RapportImport();
        int premiereLigne = -1;
        for (int i = 0; i < lignes.size() && premiereLigne == -1; i++) {
            if (!lignes.get(i).trim().isEmpty()) {
                premiereLigne = i;
            }
        }
        if (premiereLigne == -1) {
            rapport.setErreurFatale("le fichier est vide.");
            return rapport;
        }
        char separateur = detecterSeparateur(lignes.get(premiereLigne));

        for (int i = premiereLigne; i < lignes.size(); i++) {
            String ligne = lignes.get(i);
            int numero = i + 1;
            if (!ligne.trim().isEmpty()) {
                try {
                    ArrayList<String> champs = decouperChamps(ligne, separateur);
                    if (i == premiereLigne && estUneLigneEnTete(champs)) {
                        rapport.setEnTeteIgnoree(true);
                    } else {
                        rapport.incrementerLignesLues();
                        importerChamps(champs, numero, banque, rapport);
                    }
                } catch (IllegalArgumentException e) {
                    rapport.ajouterErreur(numero, e.getMessage());
                }
            }
        }
        return rapport;
    }

    /**
     * @param ligne première ligne non vide du fichier
     * @return ; si la ligne en contient, sinon ,
     */
    public static char detecterSeparateur(String ligne) {
        if (ligne.indexOf(SEPARATEUR) >= 0) {
            return SEPARATEUR;
        }
        return SEPARATEUR_REPLI;
    }

    /* la ligne d'en-tête éventuelle contient le mot « niveau » à la place d'un nombre */
    private static boolean estUneLigneEnTete(ArrayList<String> champs) {
        if (champs.size() < 2) {
            return false;
        }
        return lireEntier(champs.get(1)) == null
                && OutilsTexte.normaliserSansCasse(champs.get(1)).contains("niveau");
    }

    private static Integer lireEntier(String texte) {
        try {
            return Integer.parseInt(OutilsTexte.normaliser(texte));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* vérifie une ligne déjà découpée et l'ajoute à la banque si elle est correcte et nouvelle */
    private static void importerChamps(ArrayList<String> champs, int numero, BanqueDeDonnees banque,
                                       RapportImport rapport) {
        if (champs.size() < NB_COLONNES_MIN) {
            throw new IllegalArgumentException("seulement " + champs.size() + " colonne(s) renseignée(s) ; "
                    + "il en faut au moins 5 (catégorie, niveau, libellé, proposition juste, une proposition fausse).");
        }
        if (champs.size() > NB_COLONNES_MAX) {
            throw new IllegalArgumentException(champs.size() + " colonnes renseignées ; il en faut au plus 9 "
                    + "(4 propositions fausses au maximum, puis le feedback).");
        }
        String nomCategorie = OutilsTexte.normaliser(champs.get(0));
        if (nomCategorie.isEmpty()) {
            throw new IllegalArgumentException("la catégorie (colonne 1) est vide.");
        }
        Integer niveau = lireEntier(champs.get(1));
        if (niveau == null) {
            throw new IllegalArgumentException("le niveau (colonne 2) « " + OutilsTexte.normaliser(champs.get(1))
                    + " » n'est pas un nombre ; valeurs possibles : 1, 2 ou 3.");
        }
        ArrayList<String> fausses = new ArrayList<>();
        for (int colonne = 4; colonne < champs.size() && colonne < 8; colonne++) {
            fausses.add(champs.get(colonne));
        }
        String feedback = "";
        if (champs.size() == NB_COLONNES_MAX) {
            feedback = champs.get(8);
        }

        Categorie categorie = banque.chercherCategorie(nomCategorie);
        boolean categorieACreer = categorie == null;
        if (categorieACreer) {
            categorie = new Categorie(nomCategorie);
        }
        Question question = new Question(categorie, niveau, champs.get(2), champs.get(3), fausses, feedback);

        if (banque.existeQuestionIdentique(question, null)) {
            rapport.ajouterDoublon(numero, "la question « " + question.getLibelle()
                    + " » existe déjà dans la catégorie « " + categorie.getNom() + " ».");
            return;
        }
        if (categorieACreer) {
            categorie = banque.creerCategorie(nomCategorie);
            rapport.ajouterCategorieCreee(categorie.getNom());
        }
        banque.ajouterQuestion(question);
        rapport.incrementerQuestionsImportees();
    }

    /**
     * Découpe une ligne CSV en champs. Un champ peut être entouré de guillemets
     * (il peut alors contenir le séparateur, et "" représente un guillemet).
     * Les champs vides en fin de ligne sont supprimés.
     * @param ligne ligne à découper
     * @param separateur caractère séparateur
     * @return les champs
     * @throws IllegalArgumentException si un guillemet n'est pas refermé
     */
    public static ArrayList<String> decouperChamps(String ligne, char separateur) {
        ArrayList<String> champs = new ArrayList<>();
        String champ = "";
        boolean entreGuillemets = false;
        int i = 0;
        while (i < ligne.length()) {
            char caractere = ligne.charAt(i);
            if (entreGuillemets) {
                if (caractere == '"') {
                    if (i + 1 < ligne.length() && ligne.charAt(i + 1) == '"') {
                        champ = champ + '"';
                        i++;
                    } else {
                        entreGuillemets = false;
                    }
                } else {
                    champ = champ + caractere;
                }
            } else if (caractere == '"' && champ.trim().isEmpty()) {
                entreGuillemets = true;
                champ = "";
            } else if (caractere == separateur) {
                champs.add(champ);
                champ = "";
            } else {
                champ = champ + caractere;
            }
            i++;
        }
        if (entreGuillemets) {
            throw new IllegalArgumentException("un guillemet ouvert n'est jamais refermé.");
        }
        champs.add(champ);
        while (!champs.isEmpty() && OutilsTexte.estVide(champs.get(champs.size() - 1))) {
            champs.remove(champs.size() - 1);
        }
        return champs;
    }

    /* ---------------------------------------------------------------- écriture */

    /**
     * @param question question à convertir
     * @return la ligne CSV (9 colonnes, séparateur ;)
     */
    public static String questionVersLigne(Question question) {
        ArrayList<String> fausses = question.getReponsesFausses();
        String ligne = proteger(question.getCategorie().getNom())
                + SEPARATEUR + question.getNiveau()
                + SEPARATEUR + proteger(question.getLibelle())
                + SEPARATEUR + proteger(question.getReponseJuste());
        for (int i = 0; i < Question.NB_FAUSSES_MAX; i++) {
            ligne = ligne + SEPARATEUR;
            if (i < fausses.size()) {
                ligne = ligne + proteger(fausses.get(i));
            }
        }
        return ligne + SEPARATEUR + proteger(question.getFeedback());
    }

    /* met le champ entre guillemets si nécessaire et supprime les retours à la ligne */
    private static String proteger(String champ) {
        String texte = champ.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ');
        boolean aProteger = texte.indexOf(SEPARATEUR) >= 0 || texte.indexOf('"') >= 0
                || texte.indexOf(SEPARATEUR_REPLI) >= 0;
        if (!aProteger) {
            return texte;
        }
        return "\"" + texte.replace("\"", "\"\"") + "\"";
    }

    /**
     * @param questions questions à exporter
     * @return le texte CSV, une question par ligne, sans en-tête
     */
    public static String exporterTexte(ArrayList<Question> questions) {
        String texte = "";
        for (Question question : questions) {
            texte = texte + questionVersLigne(question) + "\n";
        }
        return texte;
    }

    /**
     * Écrit un fichier CSV lisible par Excel (UTF-8 avec BOM, séparateur ;) avec une ligne d'en-tête.
     * @param questions questions à exporter
     * @param fichier fichier de destination
     * @throws IOException en cas d'erreur d'écriture
     */
    public static void exporterFichier(ArrayList<Question> questions, File fichier) throws IOException {
        String entete = "Catégorie;Niveau;Libellé;Proposition juste;Proposition fausse 1;Proposition fausse 2;"
                + "Proposition fausse 3;Proposition fausse 4;Feedback\n";
        String contenu = "﻿" + entete + exporterTexte(questions);
        Files.write(fichier.toPath(), contenu.getBytes(StandardCharsets.UTF_8));
    }
}
