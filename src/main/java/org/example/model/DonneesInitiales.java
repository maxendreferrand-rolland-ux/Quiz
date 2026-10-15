package org.example.model;

import java.util.ArrayList;

/**
 * Banque de questions livrée avec l'application (créée au premier lancement).
 */
public class DonneesInitiales {

    public static final String CATEGORIE_JAVA = "Bases du langage Java";
    public static final String CATEGORIE_ORTHOGRAPHE = "Orthographe";

    private DonneesInitiales() {
    }

    /**
     * Ajoute les catégories et questions initiales à la banque.
     * @param banque banque à remplir
     */
    public static void remplir(BanqueDeDonnees banque) {
        Categorie generale = banque.getCategorieGenerale();
        Categorie java = banque.creerCategorie(CATEGORIE_JAVA);
        Categorie orthographe = banque.creerCategorie(CATEGORIE_ORTHOGRAPHE);

        /* catégorie Général */
        ajouter(banque, generale, 1, "Quelle est la capitale de l'Australie ?", "Canberra",
                "Sydney est la plus grande ville, mais la capitale fédérale est Canberra.",
                "Sydney", "Melbourne", "Perth");
        ajouter(banque, generale, 1, "Combien de côtés possède un hexagone ?", "6",
                "Hexa vient du grec « six ».",
                "5", "7", "8");
        ajouter(banque, generale, 2, "Dans quel département se trouve la ville de Rodez ?", "Aveyron",
                "Rodez est la préfecture de l'Aveyron (12).",
                "Lot", "Tarn", "Cantal");

        /* catégorie Bases du langage Java */
        ajouter(banque, java, 1, "Quel est le type de l'expression suivante '9' ?", "char",
                "Les apostrophes simples délimitent un caractère : '9' est de type char. \"9\" serait une String.",
                "String", "int");
        ajouter(banque, java, 1, "Quel mot-clé permet de déclarer une constante en Java ?", "final",
                "final interdit toute nouvelle affectation. const est un mot réservé mais il n'est pas utilisé en Java.",
                "const", "static", "constant");
        ajouter(banque, java, 1, "Quel symbole termine une instruction en Java ?", ";",
                "Chaque instruction simple se termine par un point-virgule.",
                ".", ":", "!");
        ajouter(banque, java, 1, "Combien vaut l'expression 7 / 2 en Java ?", "3",
                "7 et 2 sont des int : la division est entière, la partie décimale est perdue.",
                "3.5", "4");
        ajouter(banque, java, 1, "Quel opérateur teste l'égalité de deux valeurs de type int ?", "==",
                "= est l'affectation ; == compare deux valeurs primitives.",
                "=", "equals", "===");
        ajouter(banque, java, 2, "Combien vaut l'expression 7 % 3 ?", "1",
                "% donne le reste de la division entière : 7 = 2 × 3 + 1.",
                "2", "0", "2.33");
        ajouter(banque, java, 2, "Quelle méthode compare le contenu de deux chaînes s1 et s2 ?", "s1.equals(s2)",
                "== compare les références des objets ; equals compare les caractères des chaînes.",
                "s1 == s2", "s1.same(s2)", "s1 = s2");
        ajouter(banque, java, 2, "Quel est l'indice du dernier élément d'un tableau t ?", "t.length - 1",
                "Les indices commencent à 0, le dernier vaut donc t.length - 1. length est un attribut, pas une méthode.",
                "t.length", "t.length()", "t.size() - 1");
        ajouter(banque, java, 2, "Qu'affiche l'instruction System.out.println(1 + 2 + \"3\"); ?", "33",
                "L'évaluation se fait de gauche à droite : 1 + 2 donne 3 (int), puis 3 + \"3\" donne la chaîne \"33\".",
                "123", "6", "15");
        ajouter(banque, java, 2, "Quelle est la valeur par défaut d'un attribut de type boolean ?", "false",
                "Les attributs numériques valent 0, les boolean false et les références null.",
                "true", "null", "0");
        ajouter(banque, java, 3, "Que vaut l'expression (int) 3.99 ?", "3",
                "La conversion vers int tronque vers zéro, elle n'arrondit pas.",
                "4", "3.99", "Erreur de compilation");
        ajouter(banque, java, 3, "Quelle exception est levée par Integer.parseInt(\"douze\") ?", "NumberFormatException",
                "parseInt lève une NumberFormatException quand la chaîne ne représente pas un entier.",
                "ArithmeticException", "IllegalStateException", "ClassCastException");
        ajouter(banque, java, 3, "Que vaut x après : int x = 5; x += x++ + ++x; ?", "17",
                "x += e équivaut à x = x + e. La valeur 5 de x est lue en premier, puis x++ vaut 5 (x passe à 6) "
                        + "et ++x vaut 7 : 5 + 5 + 7 = 17.",
                "16", "18", "22");
        ajouter(banque, java, 3, "Quelle collection n'accepte pas de doublons ?", "HashSet",
                "Un Set (comme HashSet) ne contient jamais deux éléments égaux au sens de equals.",
                "ArrayList", "LinkedList", "Vector");

        /* catégorie Orthographe */
        ajouter(banque, orthographe, 1, "Quelle est la bonne orthographe ?", "Quand même",
                "On écrit « quand même », en deux mots et sans trait d'union.",
                "Comme même", "Quand-même");
        ajouter(banque, orthographe, 1, "Complétez : « Ils ___ partis hier. »", "sont",
                "On peut remplacer par « étaient » : il s'agit du verbe être.",
                "son", "sons");
        ajouter(banque, orthographe, 1, "Complétez : « Il ___ beau aujourd'hui. »", "fait",
                "Verbe faire au présent, 3e personne du singulier : il fait.",
                "fais", "fé");
        ajouter(banque, orthographe, 2, "Complétez : « Les fleurs que j'ai ___ sont fanées. » (cueillir)", "cueillies",
                "Avec avoir, le participe passé s'accorde avec le COD placé avant : « que » reprend « les fleurs », "
                        + "féminin pluriel.",
                "cueilli", "cueillis", "cueillie");
        ajouter(banque, orthographe, 2, "Quelle est la bonne orthographe ?", "dilemme",
                "Dilemme s'écrit avec deux m, comme « lemme ».",
                "dilemne", "dillemme", "dilème");
        ajouter(banque, orthographe, 2, "Complétez : « Elle s'est ___ les mains. » (laver)", "lavé",
                "Le COD « les mains » est placé après le verbe : le participe passé ne s'accorde pas.",
                "lavée", "lavées", "laver");
        ajouter(banque, orthographe, 2, "Complétez : « Ci-___ la facture demandée. »", "joint",
                "En tête de phrase, devant le nom, « ci-joint » est invariable.",
                "jointe", "joins");
        ajouter(banque, orthographe, 3, "Quel est le pluriel de « un chou-fleur » ?", "des choux-fleurs",
                "Les deux mots sont des noms : ils prennent tous les deux la marque du pluriel.",
                "des chou-fleurs", "des choux-fleur", "des chous-fleurs");
        ajouter(banque, orthographe, 3, "Complétez : « Les dix heures qu'il a ___ l'ont reposé. » (dormir)", "dormi",
                "« que » est ici un complément de durée et non un COD : le participe passé reste invariable.",
                "dormies", "dormis");
        ajouter(banque, orthographe, 3, "Quelle phrase est correcte ?", "Elle s'est permis de répondre.",
                "« se » est complément d'objet indirect (permettre à soi) : le participe passé reste invariable.",
                "Elle s'est permise de répondre.", "Elle s'est permit de répondre.");
        ajouter(banque, orthographe, 3, "Complétez : « ___ soient vos raisons, je ne changerai pas d'avis. »",
                "Quelles que",
                "Devant le verbe être, « quel » s'accorde avec le sujet (ici « vos raisons ») et « que » reste séparé.",
                "Quelque", "Quel que", "Quelques");
    }

    private static void ajouter(BanqueDeDonnees banque, Categorie categorie, int niveau, String libelle,
                                String juste, String feedback, String... fausses) {
        ArrayList<String> listeFausses = new ArrayList<>();
        for (String fausse : fausses) {
            listeFausses.add(fausse);
        }
        banque.ajouterQuestion(new Question(categorie, niveau, libelle, juste, listeFausses, feedback));
    }
}
