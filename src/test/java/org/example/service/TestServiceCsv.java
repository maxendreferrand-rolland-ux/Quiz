package org.example.service;

import org.example.model.BanqueDeDonnees;
import org.example.model.Categorie;
import org.example.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de l'importation et de l'exportation CSV.
 */
class TestServiceCsv {

    private BanqueDeDonnees banque;

    @BeforeEach
    void initialiser() {
        this.banque = new BanqueDeDonnees();
    }

    @Test
    void decouperChampsAvecGuillemets() {
        assertEquals(Arrays.asList("a", "b", "c"), ServiceCsv.decouperChamps("a;b;c;;;", ';'));
        assertEquals(Arrays.asList("Java", "1", "a;b", "dit \"oui\""),
                ServiceCsv.decouperChamps("Java;1;\"a;b\";\"dit \"\"oui\"\"\"", ';'));
        assertEquals(Arrays.asList("x", "", "y"), ServiceCsv.decouperChamps("x,,y", ','));
        assertThrows(IllegalArgumentException.class, () -> ServiceCsv.decouperChamps("a;\"b;c", ';'));
    }

    @Test
    void importLignesValidesDeUneAQuatreFaussesAvecEtSansFeedback() {
        String texte = "Catégorie;Niveau;Libellé;Juste;Fausse 1;Fausse 2;Fausse 3;Fausse 4;Feedback\n"
                + "Tennis;1;Combien de sets max chez les hommes en Grand Chelem ?;5;3\n"
                + "Tennis;2;Surface de Roland-Garros ?;Terre battue;Gazon;Dur;;;Ocre !\n"
                + "Cinéma;3;Réalisateur de Psychose ?;Hitchcock;Kubrick;Spielberg;Lynch;Fincher;\n"
                + "\n"
                + "Général;1;Couleur du ciel ?;Bleu;Vert;;;;\n";
        RapportImport rapport = ServiceCsv.importerTexte(texte, this.banque);
        assertTrue(rapport.estSansErreur(), rapport.getDetail());
        assertTrue(rapport.estEnTeteIgnoree());
        assertEquals(4, rapport.getQuestionsImportees());
        assertEquals(Arrays.asList("Tennis", "Cinéma"), rapport.getCategoriesCreees());

        Categorie tennis = this.banque.chercherCategorie("tennis");
        Question terre = this.banque.getQuestionsDeCategorie(tennis).get(1);
        assertEquals(2, terre.getReponsesFausses().size());
        assertEquals("Ocre !", terre.getFeedback());
        Question psychose = this.banque.getQuestionsDeCategorie(this.banque.chercherCategorie("Cinéma")).get(0);
        assertEquals(4, psychose.getReponsesFausses().size());
    }

    @Test
    void importSignaleErreursEtDoublonsAvecNumeroDeLigne() {
        String texte = "Java;1;Q valide;oui;non\n"
                + "Java;1;Q valide;non;oui\n"
                + "Java;deux;Q;oui;non\n"
                + "Java;5;Q;oui;non\n"
                + "Java;1;Q sans fausse;oui\n"
                + ";1;Q;oui;non\n"
                + "Java;1;Q;oui;a;b;c;d;fb;en trop\n"
                + "Java;1;Q;oui;oui\n"
                + "Nouvelle;1;\"guillemet;non fermé\n";
        RapportImport rapport = ServiceCsv.importerTexte(texte, this.banque);
        assertEquals(1, rapport.getQuestionsImportees());
        assertEquals(1, rapport.getDoublons().size());
        assertTrue(rapport.getDoublons().get(0).startsWith("Ligne 2 :"));
        ArrayList<String> erreurs = rapport.getErreurs();
        assertEquals(7, erreurs.size(), rapport.getDetail());
        assertTrue(erreurs.get(0).startsWith("Ligne 3 :") && erreurs.get(0).contains("n'est pas un nombre"));
        assertTrue(erreurs.get(1).startsWith("Ligne 4 :") && erreurs.get(1).contains("1, 2 ou 3"));
        assertTrue(erreurs.get(2).contains("au moins 5"));
        assertTrue(erreurs.get(3).contains("catégorie"));
        assertTrue(erreurs.get(4).contains("au plus 9"));
        assertTrue(erreurs.get(5).contains("plusieurs fois"));
        assertTrue(erreurs.get(6).startsWith("Ligne 9 :"));
        /* une catégorie n'est jamais créée pour une ligne en erreur */
        assertNull(this.banque.chercherCategorie("Nouvelle"));
        assertEquals(Arrays.asList("Java"), rapport.getCategoriesCreees());
    }

    @Test
    void separateurVirguleEnRepli() {
        RapportImport rapport = ServiceCsv.importerTexte("Sport,1,Question ?,oui,non,peut-être", this.banque);
        assertEquals(1, rapport.getQuestionsImportees(), rapport.getDetail());
        assertNotNull(this.banque.chercherCategorie("Sport"));
    }

    @Test
    void fichierVide() {
        RapportImport rapport = ServiceCsv.importerTexte("\n  \n", this.banque);
        assertNotNull(rapport.getErreurFatale());
    }

    @Test
    void exportPuisReimportIdentique() throws Exception {
        BanqueDeDonnees source = BanqueDeDonnees.creerAvecDonneesInitiales();
        File fichier = File.createTempFile("export", ".csv");
        ServiceCsv.exporterFichier(source.getQuestions(), fichier);

        RapportImport rapport = ServiceCsv.importerFichier(fichier, this.banque);
        assertTrue(rapport.estSansErreur(), rapport.getDetail());
        assertEquals(source.getQuestions().size(), rapport.getQuestionsImportees());
        for (int i = 0; i < source.getQuestions().size(); i++) {
            Question originale = source.getQuestions().get(i);
            Question importee = this.banque.getQuestions().get(i);
            assertTrue(originale.estIdentique(importee));
            assertEquals(originale.getNiveau(), importee.getNiveau());
            assertEquals(originale.getFeedback(), importee.getFeedback());
        }
        /* un second import ne crée que des doublons */
        RapportImport second = ServiceCsv.importerFichier(fichier, this.banque);
        assertEquals(0, second.getQuestionsImportees());
        assertEquals(source.getQuestions().size(), second.getDoublons().size());
    }

    @Test
    void fichierExcelEnWindows1252() throws Exception {
        File fichier = File.createTempFile("excel", ".csv");
        Files.write(fichier.toPath(), "Géographie;1;Où est Rodez ?;Aveyron;Lot\r\n".getBytes(Charset.forName("windows-1252")));
        RapportImport rapport = ServiceCsv.importerFichier(fichier, this.banque);
        assertEquals(1, rapport.getQuestionsImportees(), rapport.getDetail());
        assertNotNull(this.banque.chercherCategorie("Géographie"));
    }
}
