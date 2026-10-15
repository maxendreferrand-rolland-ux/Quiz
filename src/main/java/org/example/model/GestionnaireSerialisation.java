package org.example.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sauvegarde et chargement de toutes les données dans un fichier unique, par sérialisation.
 */
public class GestionnaireSerialisation {

    public static final String NOM_FICHIER = "quiz_data.ser";

    private File fichier;

    /**
     * Gestionnaire utilisant le fichier quiz_data.ser du dossier courant.
     */
    public GestionnaireSerialisation() {
        this(new File(NOM_FICHIER));
    }

    public GestionnaireSerialisation(File fichier) {
        this.fichier = fichier;
    }

    public File getFichier() {
        return this.fichier;
    }

    public boolean fichierExiste() {
        return this.fichier.exists();
    }

    /**
     * Charge les données. Si le fichier n'existe pas (premier lancement),
     * une banque contenant les questions initiales est créée.
     * @return la banque chargée
     * @throws IOException si le fichier existe mais ne peut pas être lu
     */
    public BanqueDeDonnees charger() throws IOException {
        if (!this.fichierExiste()) {
            return BanqueDeDonnees.creerAvecDonneesInitiales();
        }
        try (ObjectInputStream entree = new ObjectInputStream(new FileInputStream(this.fichier))) {
            Object objet = entree.readObject();
            if (!(objet instanceof BanqueDeDonnees)) {
                throw new IOException("Le fichier " + this.fichier.getName() + " ne contient pas de données du Quiz.");
            }
            return (BanqueDeDonnees) objet;
        } catch (ClassNotFoundException e) {
            throw new IOException("Le fichier " + this.fichier.getName()
                    + " a été créé par une version incompatible de l'application.", e);
        }
    }

    /**
     * Sauvegarde les données. L'écriture se fait d'abord dans un fichier temporaire
     * pour ne jamais abîmer la sauvegarde précédente en cas de problème.
     * @param banque données à sauvegarder
     * @throws IOException en cas d'erreur d'écriture
     */
    public void sauvegarder(BanqueDeDonnees banque) throws IOException {
        File dossier = this.fichier.getAbsoluteFile().getParentFile();
        File temporaire = new File(dossier, this.fichier.getName() + ".tmp");
        try (ObjectOutputStream sortie = new ObjectOutputStream(new FileOutputStream(temporaire))) {
            sortie.writeObject(banque);
        }
        Files.move(temporaire.toPath(), this.fichier.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Renomme un fichier illisible pour le conserver et repartir sur des données neuves.
     * @return le fichier sous son nouveau nom, ou null si le renommage a échoué
     */
    public File mettreDeCote() {
        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File copie = new File(this.fichier.getAbsoluteFile().getParentFile(),
                this.fichier.getName() + ".illisible-" + horodatage);
        if (this.fichier.renameTo(copie)) {
            return copie;
        }
        return null;
    }
}
