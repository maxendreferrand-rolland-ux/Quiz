package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de la classe Question.
 */
class TestQuestion {

    private static ArrayList<String> liste(String... valeurs) {
        return new ArrayList<>(Arrays.asList(valeurs));
    }

    private static Question exemple(String nomCategorie, int niveau, String feedback, String... fausses) {
        return new Question(new Categorie(nomCategorie), niveau, "Quel est le type de '9' ?", "char",
                liste(fausses), feedback);
    }

    @Test
    void constructeurValideNettoieLesTextes() {
        Question question = new Question(new Categorie("Java"), 2, "  Quel   type ?  ", " char ",
                liste("String", "", "  ", "int"), null);
        assertEquals("Quel type ?", question.getLibelle());
        assertEquals("char", question.getReponseJuste());
        assertEquals(liste("String", "int"), question.getReponsesFausses());
        assertEquals("", question.getFeedback());
        assertFalse(question.aUnFeedback());
    }

    @Test
    void constructeurRefuseLesDonneesIncorrectes() {
        Categorie categorie = new Categorie("Java");
        assertThrows(IllegalArgumentException.class,
                () -> new Question(null, 1, "Libellé", "juste", liste("faux"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 0, "Libellé", "juste", liste("faux"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 4, "Libellé", "juste", liste("faux"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "  ", "juste", liste("faux"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "Libellé", "", liste("faux"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "Libellé", "juste", liste("", " "), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "Libellé", "juste", liste("a", "b", "c", "d", "e"), ""));
        /* une proposition fausse identique à la juste (casse ignorée) */
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "Libellé", "juste", liste("JUSTE"), ""));
        /* deux propositions fausses identiques */
        assertThrows(IllegalArgumentException.class,
                () -> new Question(categorie, 1, "Libellé", "juste", liste("faux", "faux"), ""));
    }

    @Test
    void niveauxLimitesAcceptes() {
        assertEquals(1, exemple("Java", 1, "", "String").getNiveau());
        assertEquals(3, exemple("Java", 3, "", "String").getNiveau());
        assertEquals(4, exemple("Java", 1, "", "a", "b", "c", "d").getReponsesFausses().size());
    }

    @Test
    void estIdentiqueIgnoreNiveauFeedbackEtOrdre() {
        Question question1 = exemple("Java", 1, "feedback 1", "String", "int");
        Question question2 = exemple(" java ", 3, "autre feedback", "int", "String");
        assertTrue(question1.estIdentique(question2));
        assertTrue(question2.estIdentique(question1));
    }

    @Test
    void estIdentiqueDetecteLesDifferences() {
        Question reference = exemple("Java", 1, "", "String", "int");
        assertFalse(reference.estIdentique(exemple("Orthographe", 1, "", "String", "int")));
        assertFalse(reference.estIdentique(exemple("Java", 1, "", "String")));
        assertFalse(reference.estIdentique(exemple("Java", 1, "", "String", "long")));
        assertFalse(reference.estIdentique(new Question(new Categorie("Java"), 1, "Autre libellé ?", "char",
                liste("String", "int"), "")));
        assertFalse(reference.estIdentique(null));
    }

    @Test
    void copierProduitUneQuestionIndependante() {
        Question originale = exemple("Java", 2, "fb", "String", "int");
        Question copie = originale.copier();
        assertNotSame(originale, copie);
        assertNotSame(originale.getCategorie(), copie.getCategorie());
        assertTrue(originale.estIdentique(copie));
        assertEquals(originale.getNiveau(), copie.getNiveau());
        assertEquals(originale.getFeedback(), copie.getFeedback());
    }

    @Test
    void estReponseJuste() {
        Question question = exemple("Java", 1, "", "String");
        assertTrue(question.estReponseJuste("char"));
        assertTrue(question.estReponseJuste(" char "));
        assertFalse(question.estReponseJuste("String"));
        assertFalse(question.estReponseJuste(null));
    }
}
