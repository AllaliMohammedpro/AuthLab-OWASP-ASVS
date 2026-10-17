# AuthLab — TP 1 : Authentification sécurisée (OWASP ASVS 5.0.0)

Application pédagogique **Spring Boot 3.5.6 + React 18 + Vite + H2** réalisée pour la **Tâche 2 du TP IRISI 3**.

Ce projet présente un parcours minimal : **inscription → connexion → profil privé → changement de mot de passe → déconnexion**. Il ne crée aucun administrateur ou compte prédéfini et ne contient aucune question secrète.

> **Attention : démonstration locale, pas produit prêt pour la production.** Les références ASVS indiquent des contrôles implémentés ou à vérifier, et non une certification. La consigne du cours interdit l'utilisation de l'IA : étudier, modifier et comprendre le code, respecter les règles de votre enseignant et décrire de manière transparente l'aide éventuellement reçue.

## 1. Prérequis Windows

Installer et vérifier :

- **JDK 17 ou 21** : `java -version`
- **Apache Maven 3.9+** : `mvn -version`
- **Node.js 20+** et **npm** : `node -v` et `npm -v`
- **VS Code**, extensions recommandées : Extension Pack for Java, Spring Boot Extension Pack, ES7+ React snippets (facultative)
- Connexion Internet la première fois (dépendances Maven, npm et liste SecLists)

Si PowerShell refuse le script, il est possible d'utiliser le programme Python alternatif sans changer la politique d'exécution : `py scripts\prepare_password_list.py`.

## 2. Extraire et ouvrir le projet

1. Extraire le ZIP dans un dossier, par exemple `C:\TP-ASVS\AuthLab`.
2. Ouvrir VS Code : **File → Open Folder** → sélectionner le dossier `ASVS_AuthLab_SpringBoot_React`.
3. Ouvrir un terminal **PowerShell** (Terminal → New Terminal).

## 3. Préparer la liste des mots de passe (OBLIGATOIRE)

Depuis la **racine** du projet, exécuter :

```powershell
.\prepare-password-list.ps1
```

Ou avec Python :

```powershell
py scripts\prepare_password_list.py
```

Le script télécharge la liste **SecLists / 10k-most-common.txt**, filtre les entrées de 8 à 128 caractères et conserve les 3 000 premières entrées admissibles. Il enregistre `backend/config/common-passwords.txt`. La liste est exclue de Git. Le backend refuse de démarrer sans au moins 3 000 entrées. Source et licence MIT : https://github.com/danielmiessler/SecLists.

**Pourquoi ?** ASVS v5.0.0-6.2.4 demande de comparer les mots de passe choisis aux 3 000 mots de passe les plus courants **compatibles avec la politique de longueur**. Ne pas inventer une liste de 3 000 valeurs aléatoires pour simuler ce contrôle.

## 4. Lancer Spring Boot (terminal 1)

À la racine du projet :

```powershell
cd backend
mvn clean test
mvn spring-boot:run
```

Laisser ce terminal ouvert. Attendre le message `Started AuthLabApplication`. L'API est accessible à **http://localhost:8080**. Le fichier de base H2 est créé sous `backend/data/` au premier démarrage.

> Si `mvn` n'est pas reconnu, installer Maven puis rouvrir VS Code. Le projet utilise le dossier `backend` comme répertoire de travail (important pour le fichier de mots de passe et H2).

## 5. Lancer React (terminal 2)

Ouvrir un **nouveau terminal** à la racine :

```powershell
cd frontend
npm install
npm run dev
```

Ouvrir dans le navigateur **http://localhost:5173**.

Le proxy de développement Vite redirige automatiquement `/api` vers `http://127.0.0.1:8080`. **N'ouvrez pas directement l'interface sur le port 8080** : le frontend React est sur 5173.

## 6. Tests manuels à réaliser

| N° | Action | Résultat attendu | ASVS |
|---|---|---|---|
| 1 | Créer un compte avec un mot de passe de 7 caractères | Rejet | 6.2.1 |
| 2 | Essayer `password1` à l'inscription | Rejet car trop courant | 6.2.4 |
| 3 | Créer un compte avec une **phrase longue** unique, même sans chiffres ni symbole | Acceptation (si elle est absente de la liste) | 6.2.5 |
| 4 | Copier/coller un mot de passe depuis un gestionnaire | Autorisé | 6.2.7 |
| 5 | Se connecter avec les bons identifiants | Accès au tableau de bord | 6.3.1 |
| 6 | Essayer 5 mots de passe incorrects sur la même adresse depuis le même poste, puis une 6e tentative | HTTP 429 et limitation temporaire (~5 min) | 6.3.1 |
| 7 | Changer le mot de passe avec un **mauvais ancien mot de passe** | Rejet | 6.2.3 |
| 8 | Changer le mot de passe avec le bon ancien et un nouveau secret sûr | Succès | 6.2.2–3 |
| 9 | Se déconnecter, puis recharger le navigateur | La page privée n'est plus accessible | Protection session (V7) |
| 10 | Chercher des questions secrètes dans inscription / récupération | Aucune question secrète proposée | 6.4.2 |

**Test automatisé de base (terminal 3)** : lancer d'abord le backend, puis exécuter à la racine :

```powershell
.\scripts\test-api.ps1
```

Le script crée un compte unique, se connecte, accède au profil, change son mot de passe, se déconnecte et s'assure que le profil n'est plus accessible.

## 7. Architecture du projet

```text
ASVS_AuthLab_SpringBoot_React/
├─ backend/
│  ├─ pom.xml
│  ├─ config/                     # common-passwords.txt généré par le script
│  ├─ src/main/java/com/irisi/authlab/
│  │  ├─ config/SecurityConfig.java
│  │  ├─ controller/AuthController.java
│  │  ├─ dto/AuthDtos.java
│  │  ├─ entity/UserAccount.java
│  │  ├─ exception/
│  │  ├─ repository/UserAccountRepository.java
│  │  ├─ security/LoginRateLimiter.java
│  │  └─ service/PasswordPolicy.java
│  ├─ src/main/resources/application.properties
│  └─ src/test/java/.../PasswordPolicyTest.java
├─ frontend/
│  ├─ package.json
│  ├─ vite.config.js
│  ├─ index.html
│  └─ src/{main.jsx,api.js,style.css}
├─ scripts/{prepare_password_list.py,test-api.ps1}
├─ prepare-password-list.ps1
└─ docs/ASVS_MAPPING.md
```

## 8. Explications de sécurité

- **Session côté serveur** : Spring Security utilise `JSESSIONID` dans un cookie **HttpOnly** et `SameSite=Lax`. Le JavaScript ne stocke ni JWT ni mot de passe. À la connexion, l'identifiant de session existant est renouvelé.
- **CSRF** : `GET /api/auth/csrf` délivre un jeton, puis React envoie `X-CSRF-TOKEN` sur les requêtes POST. Le backend vérifie ce jeton via Spring Security.
- **Mot de passe** : le serveur stocke uniquement un hachage PBKDF2-HMAC-SHA256 avec sel aléatoire, sans transformer le mot de passe saisi. Longueur 8–128 caractères Unicode ; pas d'obligation de majuscule, chiffre ou symbole.
- **Limitation des essais** : après 5 échecs pour une même combinaison IP/e-mail sur 5 minutes, ou 25 échecs pour une IP, le serveur retourne HTTP 429. Pas de désactivation définitive des comptes. Les compteurs sont en mémoire pour ce laboratoire mono-instance.
- **Base locale H2** : pour simplifier le TP. Données persistées entre deux lancements, mais **pas chiffrées au repos**.
- **6.4.1 non applicable** : l'application ne génère ni mot de passe initial ni code d'activation. Si cette fonction est ajoutée, il faudra appliquer l'exigence.
- **MFA non incluse** : les exigences générales sur MFA commencent au niveau L2. **Ne pas présenter ce TP comme conforme L2 ou L3.**

## 9. Limites de sécurité / passage en production

Il manque une procédure complète de récupération de compte, MFA, vérification des e-mails, notifications avancées et journalisation d'audit. Pour la production, utiliser HTTPS avec `server.servlet.session.cookie.secure=true`, un stockage de données géré avec sauvegardes, des migrations de schéma, une configuration sûre des proxys et un rate-limiting distribué avec gestion des abus (IP usurpées, VPN, IP partagées). Un audit réel doit vérifier l'ensemble du périmètre applicatif et des exigences applicables.

## 10. Commandes pour arrêter et relancer

- Arrêter chaque terminal avec **Ctrl+C**.
- Redémarrer le backend : `cd backend` puis `mvn spring-boot:run`.
- Redémarrer React : `cd frontend` puis `npm run dev`.
- Tester Java uniquement : `cd backend` puis `mvn test`.
- Construire le frontend : `cd frontend` puis `npm run build`.

Consulter `docs/ASVS_MAPPING.md` pour un plan de justification et des éléments à vérifier dans le rapport de TP.
