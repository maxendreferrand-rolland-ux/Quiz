package org.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Ensemble des données de l'application : catégories, banque de questions,
 * historique des tentatives et paramètres. C'est cet objet qui est sérialisé.
 */
public class BanqueDeDonnees implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Categorie> categories;
    private ArrayList<Question> questions;
    private ArrayList<Tentative> historique;
    private Parametres parametres;

    /**
     * Banque contenant uniquement la catégorie « Général ».
     */
    public BanqueDeDonnees() {
        this.categories = new ArrayList<>();
        this.questions = new ArrayList<>();
        this.historique = new ArrayList<>();
        this.parametres = new Parametres();
        this.categories.add(new Categorie(Categorie.NOM_GENERALE));
    }

    /**
     * @return une banque remplie avec les questions livrées avec l'application
     */
    public static BanqueDeDonnees creerAvecDonneesInitiales() {
        BanqueDeDonnees banque = new BanqueDeDonnees();
        DonneesInitiales.remplir(banque);
        return banque;
    }

    public Parametres getParametres() {
        return this.parametres;
    }

    /* ---------------------------------------------------------------- catégories */

    /**
     * @return copie de la liste des catégories (« Général » en premier)
     */
    public ArrayList<Categorie> getCategories() {
        return new ArrayList<>(this.categories);
    }

    public Categorie getCategorieGenerale() {
        return this.categories.get(0);
    }

    /**
     * @param nom nom recherché (casse et espaces ignorés)
     * @return la catégorie portant ce nom, ou null
     */
    public Categorie chercherCategorie(String nom) {
        for (Categorie categorie : this.categories) {
            if (categorie.aLeMemeNomQue(nom)) {
                return categorie;
            }
        }
        return null;
    }

    /**
     * @param nom nom de la nouvelle catégorie
     * @return la catégorie créée
     * @throws IllegalArgumentException si le nom est vide, trop long ou déjà utilisé
     */
    public Categorie creerCategorie(String nom) {
        Categorie nouvelle = new Categorie(nom);
        if (this.chercherCategorie(nouvelle.getNom()) != null) {
            throw new IllegalArgumentException("Une catégorie nommée « " + nouvelle.getNom() + " » existe déjà.");
        }
        this.categories.add(nouvelle);
        return nouvelle;
    }

    /**
     * @param categorie catégorie à renommer
     * @param nouveauNom nouveau nom
     * @throws IllegalArgumentException s'il s'agit de « Général », si le nom est invalide ou déjà pris
     */
    public void renommerCategorie(Categorie categorie, String nouveauNom) {
        if (categorie.estGenerale()) {
            throw new IllegalArgumentException("La catégorie « Général » ne peut pas être renommée.");
        }
        String nomVerifie = Categorie.verifierNom(nouveauNom);
        Categorie existante = this.chercherCategorie(nomVerifie);
        if (existante != null && existante != categorie) {
            throw new IllegalArgumentException("Une catégorie nommée « " + nomVerifie + " » existe déjà.");
        }
        categorie.renommer(nomVerifie);
    }

    /**
     * Supprime une catégorie et toutes ses questions.
     * @param categorie catégorie à supprimer
     * @throws IllegalArgumentException s'il s'agit de « Général »
     */
    public void supprimerCategorie(Categorie categorie) {
        if (categorie.estGenerale()) {
            throw new IllegalArgumentException("La catégorie « Général » ne peut pas être supprimée.");
        }
        this.questions.removeAll(this.getQuestionsDeCategorie(categorie));
        this.categories.remove(categorie);
    }

    /* ---------------------------------------------------------------- questions */

    public ArrayList<Question> getQuestions() {
        return new ArrayList<>(this.questions);
    }

    public ArrayList<Question> getQuestionsDeCategorie(Categorie categorie) {
        ArrayList<Question> resultat = new ArrayList<>();
        for (Question question : this.questions) {
            if (question.getCategorie() == categorie) {
                resultat.add(question);
            }
        }
        return resultat;
    }

    public int compterQuestions(Categorie categorie) {
        return this.getQuestionsDeCategorie(categorie).size();
    }

    /**
     * @param question question à tester
     * @param questionIgnoree question à ne pas prendre en compte (celle qu'on modifie), peut être null
     * @return true si la banque contient déjà une question identique
     */
    public boolean existeQuestionIdentique(Question question, Question questionIgnoree) {
        for (Question existante : this.questions) {
            if (existante != questionIgnoree && existante.estIdentique(question)) {
                return true;
            }
        }
        return false;
    }

    /* retrouve la catégorie de la banque correspondant à celle de la question */
    private Categorie categorieDeLaBanque(Question question) {
        Categorie categorie = this.chercherCategorie(question.getCategorie().getNom());
        if (categorie == null) {
            throw new IllegalArgumentException("La catégorie « " + question.getCategorie().getNom()
                    + " » n'existe pas.");
        }
        return categorie;
    }

    /**
     * @param question question à ajouter
     * @throws IllegalArgumentException si sa catégorie n'existe pas ou si une question identique existe
     */
    public void ajouterQuestion(Question question) {
        Categorie categorie = this.categorieDeLaBanque(question);
        if (this.existeQuestionIdentique(question, null)) {
            throw new IllegalArgumentException("Une question identique existe déjà dans la catégorie « "
                    + categorie.getNom() + " ».");
        }
        question.setCategorie(categorie);
        this.questions.add(question);
    }

    /**
     * Remplace toutes les informations d'une question (y compris sa catégorie).
     * @param originale question de la banque à modifier
     * @param nouvellesValeurs question contenant les nouvelles informations
     * @throws IllegalArgumentException si la catégorie n'existe pas ou si une question identique existe
     */
    public void modifierQuestion(Question originale, Question nouvellesValeurs) {
        Categorie categorie = this.categorieDeLaBanque(nouvellesValeurs);
        if (this.existeQuestionIdentique(nouvellesValeurs, originale)) {
            throw new IllegalArgumentException("Une question identique existe déjà dans la catégorie « "
                    + categorie.getNom() + " ».");
        }
        originale.modifierDepuis(nouvellesValeurs, categorie);
    }

    public void supprimerQuestion(Question question) {
        this.questions.remove(question);
    }

    /**
     * @param niveau niveau voulu ou Questionnaire.NIVEAU_INDIFFERENT
     * @param categorie catégorie voulue ou null pour toutes
     * @return les questions de la banque répondant aux critères
     */
    public ArrayList<Question> rechercherQuestions(int niveau, Categorie categorie) {
        ArrayList<Question> resultat = new ArrayList<>();
        for (Question question : this.questions) {
            boolean bonNiveau = niveau == Questionnaire.NIVEAU_INDIFFERENT || question.getNiveau() == niveau;
            boolean bonneCategorie = categorie == null || question.getCategorie() == categorie;
            if (bonNiveau && bonneCategorie) {
                resultat.add(question);
            }
        }
        return resultat;
    }

    /**
     * Tire au hasard au plus « nombre » questions répondant aux critères.
     * @param niveau niveau voulu ou Questionnaire.NIVEAU_INDIFFERENT
     * @param categorie catégorie voulue ou null pour toutes
     * @param nombre nombre de questions souhaité
     * @return le questionnaire (éventuellement incomplet)
     */
    public Questionnaire creerQuestionnaire(int niveau, Categorie categorie, int nombre) {
        ArrayList<Question> candidates = this.rechercherQuestions(niveau, categorie);
        String nomCategorie = null;
        if (categorie != null) {
            nomCategorie = categorie.getNom();
        }
        return new Questionnaire(niveau, nomCategorie, nombre, false, tirerAuHasard(candidates, nombre));
    }

    private static ArrayList<Question> tirerAuHasard(ArrayList<Question> candidates, int nombre) {
        ArrayList<Question> melange = new ArrayList<>(candidates);
        Collections.shuffle(melange);
        ArrayList<Question> resultat = new ArrayList<>();
        for (int i = 0; i < melange.size() && i < nombre; i++) {
            resultat.add(melange.get(i));
        }
        return resultat;
    }

    /* ---------------------------------------------------------------- perfectionnement */

    /**
     * Questions de l'historique dont la réponse la plus récente est fausse ou absente.
     * Une même question n'apparaît qu'une fois.
     * @param niveau niveau voulu ou Questionnaire.NIVEAU_INDIFFERENT
     * @return les questions à retravailler
     */
    public ArrayList<Question> getQuestionsARetravailler(int niveau) {
        ArrayList<Question> dejaVues = new ArrayList<>();
        ArrayList<Question> aRetravailler = new ArrayList<>();
        /* l'historique est rangé du plus récent au plus ancien */
        for (Tentative tentative : this.historique) {
            for (int i = 0; i < tentative.getNombreQuestions(); i++) {
                Question question = tentative.getQuestionnaire().getQuestion(i);
                if (!contientIdentique(dejaVues, question)) {
                    dejaVues.add(question);
                    boolean bonNiveau = niveau == Questionnaire.NIVEAU_INDIFFERENT || question.getNiveau() == niveau;
                    if (!tentative.estJuste(i) && bonNiveau) {
                        aRetravailler.add(question);
                    }
                }
            }
        }
        return aRetravailler;
    }

    private static boolean contientIdentique(ArrayList<Question> liste, Question question) {
        for (Question element : liste) {
            if (element.estIdentique(question)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param niveau niveau voulu ou Questionnaire.NIVEAU_INDIFFERENT
     * @param nombre 5, 10, 20 ou 30
     * @return un questionnaire de perfectionnement (éventuellement incomplet)
     */
    public Questionnaire creerQuestionnairePerfectionnement(int niveau, int nombre) {
        ArrayList<Question> candidates = this.getQuestionsARetravailler(niveau);
        return new Questionnaire(niveau, null, nombre, true, tirerAuHasard(candidates, nombre));
    }

    /* ---------------------------------------------------------------- historique */

    /**
     * @return copie de l'historique, du test le plus récent au plus ancien
     */
    public ArrayList<Tentative> getHistorique() {
        return new ArrayList<>(this.historique);
    }

    /**
     * Ajoute une tentative en tête d'historique puis supprime les plus anciennes au-delà de la limite.
     * @param tentative tentative à mémoriser
     */
    public void ajouterTentative(Tentative tentative) {
        this.historique.add(0, tentative);
        this.appliquerLimiteHistorique();
    }

    public void supprimerTentative(Tentative tentative) {
        this.historique.remove(tentative);
    }

    public void viderHistorique() {
        this.historique.clear();
    }

    /**
     * Supprime les tentatives les plus anciennes au-delà de la limite des paramètres.
     */
    public void appliquerLimiteHistorique() {
        while (this.historique.size() > this.parametres.getLimiteHistorique()) {
            this.historique.remove(this.historique.size() - 1);
        }
    }
}
