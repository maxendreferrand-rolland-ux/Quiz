package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de BanqueDeDonnees, Tentative et GestionnaireSerialisation.
 */
class TestBanqueDeDonnees {

    private BanqueDeDonnees banque;
    private Categorie java;

    private static ArrayList<String> liste(String... valeurs) {
        return new ArrayList<>(Arrays.asList(valeurs));
    }

    private Question ajouter(Categorie categorie, int niveau, String libelle) {
        Question question = new Question(categorie, niveau, libelle, "juste", liste("faux 1", "faux 2"), "");
        this.banque.ajouterQuestion(question);
        return question;
    }

    @BeforeEach
    void initialiser() {
        this.banque = new BanqueDeDonnees();
        this.java = this.banque.creerCategorie("Java");
    }

    @Test
    void categorieGeneraleProtegee() {
        Categorie generale = this.banque.getCategorieGenerale();
        assertEquals("Général", generale.getNom());
        assertThrows(IllegalArgumentException.class, () -> this.banque.supprimerCategorie(generale));
        assertThrows(IllegalArgumentException.class, () -> this.banque.renommerCategorie(generale, "Autre"));
    }

    @Test
    void pasDHomonymeDeCategorie() {
        assertThrows(IllegalArgumentException.class, () -> this.banque.creerCategorie(" JAVA "));
        assertThrows(IllegalArgumentException.class, () -> this.banque.creerCategorie("général"));
        assertThrows(IllegalArgumentException.class, () -> this.banque.creerCategorie("   "));
        Categorie tennis = this.banque.creerCategorie("Tennis");
        assertThrows(IllegalArgumentException.class, () -> this.banque.renommerCategorie(tennis, "java"));
        /* changer seulement la casse de son propre nom est autorisé */
        this.banque.renommerCategorie(tennis, "TENNIS");
        assertEquals("TENNIS", tennis.getNom());
    }

    @Test
    void suppressionCategorieSupprimeSesQuestions() {
        this.ajouter(this.java, 1, "Q1");
        this.ajouter(this.java, 2, "Q2");
        Question autre = this.ajouter(this.banque.getCategorieGenerale(), 1, "Q3");
        this.banque.supprimerCategorie(this.java);
        assertNull(this.banque.chercherCategorie("Java"));
        assertEquals(1, this.banque.getQuestions().size());
        assertSame(autre, this.banque.getQuestions().get(0));
    }

    @Test
    void doublonRefuseAjoutEtModification() {
        Question question1 = this.ajouter(this.java, 1, "Q1");
        Question question2 = this.ajouter(this.java, 1, "Q2");
        Question doublon = new Question(new Categorie("java"), 3, "Q1", "juste", liste("faux 2", "faux 1"), "fb");
        assertThrows(IllegalArgumentException.class, () -> this.banque.ajouterQuestion(doublon));
        /* modifier Q2 pour qu'elle devienne identique à Q1 est refusé */
        assertThrows(IllegalArgumentException.class, () -> this.banque.modifierQuestion(question2, doublon));
        /* modifier Q1 avec ses propres valeurs (niveau différent) est accepté */
        this.banque.modifierQuestion(question1, doublon);
        assertEquals(3, question1.getNiveau());
        assertSame(this.java, question1.getCategorie());
    }

    @Test
    void deplacerUneQuestion() {
        Question question = this.ajouter(this.java, 1, "Q1");
        Categorie tennis = this.banque.creerCategorie("Tennis");
        Question valeurs = new Question(new Categorie("Tennis"), 1, "Q1", "juste", liste("faux 1"), "");
        this.banque.modifierQuestion(question, valeurs);
        assertSame(tennis, question.getCategorie());
        assertEquals(0, this.banque.compterQuestions(this.java));
        assertEquals(1, this.banque.compterQuestions(tennis));
    }

    @Test
    void creerQuestionnaireRespecteLesCriteres() {
        for (int i = 0; i < 8; i++) {
            this.ajouter(this.java, 1, "Facile " + i);
        }
        this.ajouter(this.java, 3, "Difficile");
        this.ajouter(this.banque.getCategorieGenerale(), 1, "Général facile");

        Questionnaire complet = this.banque.creerQuestionnaire(1, this.java, 5);
        assertEquals(5, complet.getNombreQuestions());
        assertFalse(complet.estIncomplet());
        for (Question question : complet.getQuestions()) {
            assertEquals(1, question.getNiveau());
            assertSame(this.java, question.getCategorie());
        }

        Questionnaire incomplet = this.banque.creerQuestionnaire(Questionnaire.NIVEAU_INDIFFERENT, null, 20);
        assertEquals(10, incomplet.getNombreQuestions());
        assertTrue(incomplet.estIncomplet());

        assertEquals(0, this.banque.creerQuestionnaire(2, null, 5).getNombreQuestions());
    }

    @Test
    void historiqueLimiteEtPlusRecentEnPremier() {
        this.ajouter(this.java, 1, "Q1");
        this.banque.getParametres().setLimiteHistorique(3);
        for (int i = 0; i < 5; i++) {
            Questionnaire questionnaire = this.banque.creerQuestionnaire(0, null, 5);
            this.banque.ajouterTentative(new Tentative(questionnaire, liste("juste"),
                    LocalDateTime.of(2026, 10, 1 + i, 10, 0)));
        }
        ArrayList<Tentative> historique = this.banque.getHistorique();
        assertEquals(3, historique.size());
        assertEquals(5, historique.get(0).getDate().getDayOfMonth());
        assertEquals(3, historique.get(2).getDate().getDayOfMonth());
    }

    @Test
    void tentativeResisteALaSuppressionDesQuestions() {
        Question question = this.ajouter(this.java, 1, "Q1");
        Questionnaire questionnaire = this.banque.creerQuestionnaire(0, null, 5);
        Tentative tentative = new Tentative(questionnaire, liste("juste"));
        this.banque.ajouterTentative(tentative);
        this.banque.supprimerQuestion(question);
        this.banque.supprimerCategorie(this.java);

        assertEquals(1, tentative.getNombreReponsesJustes());
        assertEquals("Q1", tentative.getQuestionnaire().getQuestion(0).getLibelle());
        Questionnaire rejeu = tentative.creerQuestionnaireRejeu();
        assertEquals(1, rejeu.getNombreQuestions());
        Tentative nouvelle = new Tentative(rejeu, liste("faux 1"));
        assertTrue(nouvelle.getMessageProgression(tentative).contains("Régression de 100"));
    }

    @Test
    void scoreEtPhrases() {
        this.ajouter(this.java, 1, "Q1");
        this.ajouter(this.java, 1, "Q2");
        Questionnaire questionnaire = this.banque.creerQuestionnaire(0, null, 5);
        Tentative toutJuste = new Tentative(questionnaire, liste("juste", "juste"));
        Tentative moitie = new Tentative(questionnaire, liste("juste", null));
        Tentative rien = new Tentative(questionnaire, liste(null, "faux 1"));
        assertEquals(100, toutJuste.getPourcentage());
        assertEquals(50, moitie.getPourcentage());
        assertEquals(0, rien.getPourcentage());
        assertTrue(toutJuste.getPhraseResultat("Max").startsWith("Félicitations Max !"));
        assertTrue(moitie.getPhraseResultat("").startsWith("Pas mal !"));
        assertTrue(rien.getPhraseResultat(null).startsWith("Dommage,"));
        assertTrue(toutJuste.getMessageProgression(moitie).contains("Progression de 50"));
        assertTrue(moitie.getMessageProgression(moitie).contains("identique"));
        assertThrows(IllegalArgumentException.class, () -> new Tentative(questionnaire, liste("juste")));
    }

    @Test
    void questionsARetravaillerSelonLaReponseLaPlusRecente() {
        Question question1 = this.ajouter(this.java, 1, "Q1");
        Question question2 = this.ajouter(this.java, 2, "Q2");
        ArrayList<Question> deux = new ArrayList<>();
        deux.add(question1);
        deux.add(question2);
        Questionnaire questionnaire = new Questionnaire(0, null, 5, false, deux);
        /* ancienne tentative : tout faux ; récente : Q1 juste, Q2 sans réponse */
        this.banque.ajouterTentative(new Tentative(questionnaire, liste("faux 1", "faux 1"),
                LocalDateTime.of(2026, 10, 1, 9, 0)));
        this.banque.ajouterTentative(new Tentative(questionnaire, liste("juste", null),
                LocalDateTime.of(2026, 10, 2, 9, 0)));
        ArrayList<Question> aRetravailler = this.banque.getQuestionsARetravailler(0);
        assertEquals(1, aRetravailler.size());
        assertEquals("Q2", aRetravailler.get(0).getLibelle());
        assertEquals(0, this.banque.getQuestionsARetravailler(1).size());
        Questionnaire perfectionnement = this.banque.creerQuestionnairePerfectionnement(0, 30);
        assertTrue(perfectionnement.estPerfectionnement());
        assertEquals(1, perfectionnement.getNombreQuestions());
    }

    @Test
    void sauvegardeEtChargement() throws Exception {
        File dossier = Files.createTempDirectory("quiz").toFile();
        File fichier = new File(dossier, "quiz_data.ser");
        GestionnaireSerialisation gestionnaire = new GestionnaireSerialisation(fichier);

        BanqueDeDonnees initiale = gestionnaire.charger();
        assertTrue(initiale.getQuestions().size() >= 10);
        assertNotNull(initiale.chercherCategorie(DonneesInitiales.CATEGORIE_JAVA));
        assertNotNull(initiale.chercherCategorie(DonneesInitiales.CATEGORIE_ORTHOGRAPHE));

        initiale.getParametres().setNomJoueur("Maxendre");
        gestionnaire.sauvegarder(initiale);
        BanqueDeDonnees relue = gestionnaire.charger();
        assertEquals(initiale.getQuestions().size(), relue.getQuestions().size());
        assertEquals("Maxendre", relue.getParametres().getNomJoueur());
        /* après relecture, chaque question pointe vers une catégorie de la banque */
        Categorie javaRelue = relue.chercherCategorie(DonneesInitiales.CATEGORIE_JAVA);
        assertTrue(relue.compterQuestions(javaRelue) > 0);

        Files.write(fichier.toPath(), new byte[]{1, 2, 3});
        assertThrows(java.io.IOException.class, gestionnaire::charger);
    }
}
