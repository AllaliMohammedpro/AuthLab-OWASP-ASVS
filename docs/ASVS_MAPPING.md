# Correspondance OWASP ASVS 5.0.0 — Chapitre V6, Niveau 1

Base : document OWASP ASVS 5.0.0, chapitre V6 « Authentication », pages 44–52 du document. Ce tableau est une aide à la **vérification** et ne constitue pas une attestation de conformité.

| Référence | Attendu ASVS L1 | Où voir dans le code ? | Vérification recommandée |
|---|---|---|---|
| 6.1.1 | Documenter rate limiting, anti-automatisation, réponses adaptatives et prévention des blocages malveillants | README §8 ; `security/LoginRateLimiter.java` | Vérifier limites et durée ; s'assurer qu'un tiers ne peut pas désactiver durablement un compte |
| 6.2.1 | Mot de passe ≥ 8 caractères ; 15 fortement recommandés | `service/PasswordPolicy.java` | 7 caractères refusés ; 8 acceptés si non courants |
| 6.2.2 | L'utilisateur peut changer son mot de passe | `AuthController.changePassword`, formulaire React | Changement avec bonnes informations |
| 6.2.3 | Exiger l'ancien et le nouveau mot de passe | `dto/AuthDtos.ChangePassword`, `AuthController` | Ancien faux rejeté |
| 6.2.4 | Liste des 3 000 mots de passe les plus courants compatibles avec la politique | `prepare-password-list.ps1`, `PasswordPolicy.load`/`validate` | `password1` refusé ; liste vérifiée au démarrage |
| 6.2.5 | Aucune obligation de catégorie de caractères | `PasswordPolicy.validate` | Longue phrase sans symbole acceptée |
| 6.2.6 | Masquer le champ de mot de passe | `frontend/src/main.jsx` (`PasswordInput`) | Champ `type=password` par défaut |
| 6.2.7 | Autoriser coller et gestionnaires | `PasswordInput`, `autoComplete` | Coller fonctionne, aucune interdiction JavaScript |
| 6.2.8 | Vérifier exactement la valeur saisie (pas de troncature/transformation) | `AuthController`, PBKDF2, `PasswordPolicy` | Changement de casse/espaces produit un autre mot de passe |
| 6.3.1 | Mettre en œuvre protections contre brute force et credential stuffing | `LoginRateLimiter`, `AuthController.login` | HTTP 429 après la limite documentée |
| 6.3.2 | Pas de comptes par défaut actifs | Aucun mécanisme de seeding / compte par défaut | Base vierge au démarrage initial |
| 6.4.1 | Codes d'activation/mots de passe initiaux forts, temporaires et non persistants | **N/A** : aucune génération d'un code initial | Confirmer qu'aucune telle fonctionnalité n'existe |
| 6.4.2 | Aucune question secrète/indice de mot de passe | Absence dans formulaires/backend | Vérifier l'interface et les endpoints |

## Schéma fonctionnel

```text
Navigateur React (localhost:5173)
  | GET /api/auth/csrf (jeton anti-CSRF)
  | POST /api/auth/register -> validation + PBKDF2 -> H2
  | POST /api/auth/login -> contrôle anti-brute force + PBKDF2 -> session HttpOnly
  | GET /api/auth/me -> contexte Spring Security -> profil privé
  | POST /api/auth/change-password -> ancien mot de passe validé -> nouveau PBKDF2
  | POST /api/auth/logout -> invalidation session
  v
Vite Proxy -> Spring Security -> AuthController -> JPA -> H2
```

## Pour les captures d'écran du rapport

1. Formulaire d'inscription montrant la recommandation de longueur ;
2. Rejet du mot de passe `password1` (contrôle 6.2.4) ;
3. Tableau de bord après la connexion ;
4. Rejet de la modification avec mot de passe actuel incorrect ;
5. Réponse 429 dans les outils de développement du navigateur après 5 échecs ;
6. Accès au profil refusé après déconnexion.

Les captures doivent être réalisées **après tes propres tests**. Ne pas présenter une capture fictive comme résultat exécuté.
