# Sprint 2 : Duplicata & Transfert - Spécifications Techniques

Ce document détaille précisément le comportement du code métier (les contrôles, insertions et modifications en base de données) ainsi que le jeu d'essai SQL mis en place.

## 1. Jeu d'essai SQL (`test_sprint2.sql`) basé sur le Sprint 1

Le script `test_sprint2.sql` vient compléter les données du `sprint1.sql` pour permettre de valider les deux fonctionnalités. Il est conçu pour être lancé **après** `sprint1.sql`.

* **Modifications (UPDATE) :**
  * Le passeport `MG1234567` de **Jean RAKOTO (ID=1)** a été antidaté pour être rendu **expiré** (Date d'expiration passée à 2025). Cela est indispensable car la fonctionnalité de *Transfert de visa* exige que l'ancien passeport du demandeur soit expiré avant de faire le transfert sur son nouveau passeport.
* **Insertions (INSERT) :**
  * **2 Visas Transformables :** Un pour Sophie (lié à son passeport valide) et un pour Jean (lié à son passeport rendu expiré, avec la référence `V-TRANS-JEAN`). Jean est donc le client idéal pour tester le transfert vers son autre passeport.
  * **1 Demande approuvée :** Une demande historique pour Sophie, servant de souche pour justifier l'émission de sa carte.
  * **1 Carte de Résident :** Émise pour **Sophie RANDRIA**, avec le statut de référence `2` (`VALIDE`). Nécessaire pour pouvoir tester la fonctionnalité de *Duplicata*.
  * **Nouveau Statut de Carte de Résident :** Ajout du statut `ANNULE` (ID=4) dans `reference_statut_carte_resident` pour permettre au système d'invalider l'ancienne carte de Sophie une fois le duplicata validé.

---

## 2. Fonctionnalité : Transfert de Visa Transformable

Le transfert permet de lier un visa transformable (actuellement sur un passeport expiré) au nouveau passeport valide de la même personne.

### A. Contrôles Métier (Validations gérées par le Backend)
Lors de la requête `POST /demandes/transfert` le code vérifie ceci de façon séquentielle :
1. **Existence :** Le visa transformable ciblé par la référence doit exister en base.
2. **Validité du Visa :** La date d'expiration du visa lui-même ne doit pas être dépassée (date du jour <= date expiration).
3. **Passeport source (Ancien) :** L'ancien passeport attaché à ce visa **doit être expiré**.
4. **Passeport cible (Nouveau) :** Le nouveau passeport renseigné dans la requête **doit être valide** (date d'expiration dans le futur).
5. **Correspondance d'identité :** L'ancien passeport et le nouveau passeport doivent obligatoirement appartenir au **même demandeur** (même `demandeur_id`).

### B. Opérations en Base de Données (Transaction `@Transactional`)
Si tous les contrôles ci-dessus sont validés, l'opération s'exécute dans une transaction unique :
1. **INSERT (`demande`) :** Création d'une nouvelle demande au nom du profil lié, pour garder une trace comptable de l'action de transfert de visa.
2. **INSERT (`statut_demande`) :** Ajout d'une ligne pour asseoir le statut de cette nouvelle demande à `APPROUVE` (ID=3 constant).
3. **UPDATE (`visa_transformable`) :** La ligne visée subit la mise à jour de sa colonne `passeport_id` pour pointer directement sur le nouveau passeport renseigné et valide.

---

## 3. Fonctionnalité : Duplicata de Carte de Résident

Le duplicata permet de réémettre une carte de résident (suite à une perte ou détérioration) en neutralisant la carte précédente sans la supprimer.

### A. Contrôles Métier (Validations gérées par le Backend)
Lors de la soumission de `POST /demandes/duplicata`, le code contrôle :
1. **Correspondance demandeur :** Le demandeur qui souhaite refaire sa carte doit bien être le titulaire réel de la carte de résident source.
2. **Validité de la Carte source :** La carte de résident existante doit avoir le dernier statut équivalent à `VALIDE` (ID Statut = 2). On ne peut pas dupliquer une carte qui est déjà annulée, expirée ou en attente.
3. **Contrôle du motif :** L'objet du Json de la requête a pour obligation de présenter le motif restreint, soit "PERTE" soit "DETERIORATION".

### B. Opérations en Base de Données (Transaction `@Transactional`)
Si les contrôles sont corrects :
1. **INSERT (`demande`) :** Formation d'une demande "Duplicata", justifiant l'obligation au système d'émettre une carte.
2. **INSERT (`statut_demande`) :** Validation de cette demande par la création d'un statut lié `APPROUVE`.
3. **INSERT (`statut_carte_resident` *ancienne*) :** La carte de résident existante (l'ancienne) n'est pas "supprimée" ou mise à jour, on vient lui AJOUTER un nouveau log de statut qui sera pointé vers `ANNULE` (le statut d'ID=4 que l'on vient de créer). Elle est donc neutralisée.
4. **INSERT (`carte_resident`) :** Génération d'une nouvelle carte vierge, rattachée à la demande validée en étape 1 & 2. Ses dates de validités reprennent l'existant.
5. **INSERT (`statut_carte_resident` *nouvelle*) :** Exécution d'un dernier log de statut liant la nouvelle carte au statut `VALIDE` (ID=2), l'activant publiquement.