package org.example.service;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de l'échange de Diffie-Hellman et du chiffrement de Vigenère.
 */
class TestDiffieHellmanVigenereCrypto {

    @Test
    void exempleDuCoursAvecPetitsNombres() {
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DiffieHellmanVigenereCrypto alice = new DiffieHellmanVigenereCrypto(p, g, BigInteger.valueOf(6));
        DiffieHellmanVigenereCrypto bob = new DiffieHellmanVigenereCrypto(p, g, BigInteger.valueOf(15));
        assertEquals(BigInteger.valueOf(8), alice.getClePublique());
        assertEquals(BigInteger.valueOf(19), bob.getClePublique());
        alice.calculerSecretPartage(bob.getClePublique());
        bob.calculerSecretPartage(alice.getClePublique());
        assertEquals(BigInteger.TWO, alice.getSecretPartage());
        assertEquals(alice.getSecretPartage(), bob.getSecretPartage());
        assertEquals("C", alice.getCleVigenere());
    }

    @Test
    void derivationDeLaCleEnBase64() {
        assertEquals("B", DiffieHellmanVigenereCrypto.deriverCle(BigInteger.ONE));
        assertEquals("BA", DiffieHellmanVigenereCrypto.deriverCle(BigInteger.valueOf(64)));
        assertEquals("BAB", DiffieHellmanVigenereCrypto.deriverCle(BigInteger.valueOf(4097)));
        assertEquals("//", DiffieHellmanVigenereCrypto.deriverCle(BigInteger.valueOf(4095)));
        assertThrows(IllegalArgumentException.class, () -> DiffieHellmanVigenereCrypto.deriverCle(BigInteger.ZERO));
    }

    @Test
    void vigenereSurAlphabetBase64() {
        /* A=0, B=1 : chaque lettre est décalée d'une position, / revient à A, = n'est pas touché */
        assertEquals("BCD/A=", DiffieHellmanVigenereCrypto.chiffrerVigenere("ABC+/=", "B"));
        assertEquals("ABC+/=", DiffieHellmanVigenereCrypto.dechiffrerVigenere("BCD/A=", "B"));
        assertEquals("ACAC", DiffieHellmanVigenereCrypto.chiffrerVigenere("AAAA", "AC"));
    }

    @Test
    void echangeCompletAvecParametresStandards() {
        DiffieHellmanVigenereCrypto emetteur = new DiffieHellmanVigenereCrypto();
        DiffieHellmanVigenereCrypto recepteur = new DiffieHellmanVigenereCrypto();
        emetteur.calculerSecretPartage(recepteur.getClePublique());
        recepteur.calculerSecretPartage(emetteur.getClePublique());
        assertEquals(emetteur.getCleVigenere(), recepteur.getCleVigenere());
        assertTrue(emetteur.getCleVigenere().length() > 300);

        String message = "Orthographe;2;Complétez : « Elle s'est ___ »;lavé;lavée;;;;Accord € ✓\nLigne 2";
        String chiffre = emetteur.chiffrer(message);
        assertNotEquals(message, chiffre);
        assertTrue(chiffre.matches("[A-Za-z0-9+/=]*"));
        assertEquals(message, recepteur.dechiffrer(chiffre));
    }

    @Test
    void mauvaiseCleOuCleInvalide() {
        DiffieHellmanVigenereCrypto crypto = new DiffieHellmanVigenereCrypto();
        assertThrows(IllegalStateException.class, () -> crypto.chiffrer("test"));
        assertThrows(IllegalArgumentException.class, () -> crypto.calculerSecretPartage(BigInteger.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> crypto.calculerSecretPartage(DiffieHellmanVigenereCrypto.P_DEFAUT));
    }
}
