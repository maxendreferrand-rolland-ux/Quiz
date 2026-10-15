# Quiz – Test de connaissances (SAE S3, IUT de Rodez)

Application JavaFX (sans FXML) permettant de tester ses connaissances par des quiz :
gestion des catégories et des questions, import CSV, partage chiffré entre deux ordinateurs,
historique rejouable et mode « Perfectionnement avancé ».

## Lancer le projet

Prérequis : JDK 21 et Maven.

```bash
mvn javafx:run          # lance l'application depuis les sources
mvn test                # lance les tests unitaires
mvn package             # produit target/Quiz.jar (double-clic pour lancer)
mvn package -Ptous-os   # même jar, mais utilisable sous Windows, Linux et macOS
```

Dans IntelliJ, lancer la classe `org.example.Lanceur` (et non `ApplicationQuiz` directement).

Les données sont enregistrées dans `quiz_data.ser`, dans le dossier depuis lequel l'application est lancée.
Au premier lancement, ce fichier est créé avec la catégorie « Général » et 28 questions initiales
(« Bases du langage Java », « Orthographe », « Général »).

Le dossier `exemples/` contient deux fichiers CSV pour tester l'import : un fichier correct
et un fichier contenant volontairement des erreurs (utile pour les tests d'intégration du dossier).

## Organisation du code

| Paquetage | Contenu |
|---|---|
| `org.example` | `Lanceur` : point d'entrée du jar exécutable |
| `org.example.model` | `Categorie`, `Question`, `Questionnaire`, `Tentative`, `Parametres`, `BanqueDeDonnees` (toutes sérialisables), `DonneesInitiales`, `GestionnaireSerialisation`, `OutilsTexte` |
| `org.example.service` | `ServiceCsv` + `RapportImport` (import/export CSV), `DiffieHellmanVigenereCrypto`, `PeerNetworkService` + `EcouteurReseau` (sockets) |
| `org.example.ui` | `ApplicationQuiz` (fenêtre, menu latéral) et une classe par vue, `FormulaireQuestion`, `Dialogues`, `Style` (CSS écrit en Java), `SelecteurSegmente` |

Tests JUnit 5 (`src/test/java`) : `TestQuestion`, `TestBanqueDeDonnees`, `TestServiceCsv`,
`TestDiffieHellmanVigenereCrypto`, `TestPeerNetworkService` (transfert réel sur 127.0.0.1).

## Choix de conception

- **Questions identiques** : même catégorie (casse et espaces ignorés), même libellé et même ensemble de
  propositions, quel que soit l'ordre. Le niveau et le feedback ne comptent pas.
- **Historique et questions modifiées/supprimées** : une `Tentative` garde une copie de ses questions.
  Un test reste consultable et rejouable même si ses questions ont changé ; il est rejoué avec les questions
  d'origine pour que la comparaison des scores reste juste, et l'utilisateur en est prévenu.
- **Perfectionnement avancé** : les questions retenues sont celles dont la réponse la plus récente dans
  l'historique est fausse ou absente. Les séances de perfectionnement sont enregistrées dans l'historique,
  ce qui permet à une question de sortir de la liste une fois réussie.
- **CSV** : séparateur `;` (`,` si la première ligne n'en contient pas), champs entre guillemets acceptés,
  feedback toujours en 9e colonne, en-tête reconnue, UTF-8 ou Windows-1252 (Excel).
  Les catégories ne sont créées que pour des lignes valides.
- **Réseau** : le pair qui reçoit ouvre un `ServerSocket` et accepte une seule connexion ; le pair qui envoie
  s'y connecte avec un `Socket`. Chaque échange tourne dans son propre thread, l'interface est mise à jour
  par `Platform.runLater`, et la connexion se ferme à la fin. Les questions transitent au format CSV
  et sont vérifiées à la réception exactement comme un import de fichier.

## Chiffrement

1. **Diffie-Hellman** avec le groupe 14 de la RFC 3526 (p premier de 2048 bits, g = 2).
   Chacun tire une clé privée a, envoie g^a mod p, et calcule le secret s = (g^b)^a mod p.
2. **Dérivation de la clé** : s est écrit en base 64, chaque chiffre (0 à 63) devenant une lettre de l'alphabet
   Base64 (`A`=0 … `Z`=25, `a`=26 … `z`=51, `0`=52 … `9`=61, `+`=62, `/`=63).
   Exemple : 4097 = 1×64² + 0×64 + 1 donne la clé `BAB`. Avec p de 2048 bits, la clé fait environ 342 caractères.
3. **Vigenère** : le texte (UTF-8, accents compris) est d'abord encodé en Base64 pour n'avoir que 64 symboles,
   puis chaque symbole m est remplacé par (m + k) mod 64, où k est la lettre de clé correspondante.
   Le déchiffrement calcule (c − k) mod 64 puis décode le Base64. Le caractère de remplissage `=` n'est pas chiffré.

Exemple vérifié par les tests (petits nombres) : p = 23, g = 5, a = 6, b = 15 → A = 8, B = 19, secret = 2, clé `C`.
