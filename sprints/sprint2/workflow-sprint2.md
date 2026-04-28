# WORKFLOW SPRINT 2 - DUPLICATA ET TRANSFERT

## REPONSE A LA QUESTION : EST-CE QU'ON TOUCHE A LA STRUCTURE DE LA BASE DE DONNEES ?
**NON.** La structure actuelle de la base de données est suffisante. 
- Les tables `carte_resident` et `visa` possèdent déjà une clé étrangère `demande_id`.
- La table `demande` contient déjà toutes les liaisons nécessaires (`demandeur_id`, `passeport_id`, `visa_transformable_id`, `type_visa_id`).
- L'audit et la traçabilité peuvent être assurés par l'insertion de nouvelles lignes dans la table `demande` ou la réutilisation d'anciennes demandes, avec le statut `APPROUVE` (inséré dans `statut_demande`).

---

## WORKFLOW DETAILLE SANS APPROXIMATION

### 1. Saisie de la demande métier (Duplicata ou Transfert)
A l'ouverture de la page de Duplicata ou de Transfert :
- **OPTION 1 : Choisir une demande existante.**
  - L'agent recherche une demande existante via son identifiant (`id_demande`).
  - Le système récupère et verrouille les champs d'identité du demandeur associés à cette `demande`.
  
- **OPTION 2 : Créer une nouvelle demande intégrée.**
  - Si aucune demande n'est associée ou disponible, l'agent utilise directement le formulaire de demande classique affiché dans l'interface de l'opération.
  - Cette nouvelle demande sera créée spécifiquement pour l'opération.
  - La particularité absolue : cette demande sera enregistrée et **directement approuvée** (statut `APPROUVE` inséré dans `statut_demande`) sans passer par les étapes de validation intermédiaires habituelles.

### 2. Renseignement des informations spécifiques à l'opération
Une fois la `demande` (existante ou nouvelle) verrouillée, l'agent remplit les données de la fonctionnalité :

#### 🔹 Pour le Duplicata Carte Résident
- Renseigner la carte source.
- Spécifier le motif (PERTE ou DETERIORATION).

#### 🔹 Pour le Transfert Visa
- Renseigner la référence du visa source.
- Renseigner le nouveau passeport cible.

### 3. Validation et Exécution Transactionnelle (Backend)
À la soumission :

- **Validation Stricte :**
  - L'identité du demandeur (récupérée de l'`id_demande`) doit strictement correspondre au titulaire de la carte résident (pour le duplicata) ou de l'ancien passeport/visa (pour le transfert).

- **Exécution :**
  - *Si OPTION 2 a été choisie* : Insertion de la table `demande` avec les données d'identité, puis insertion immédiate d'une ligne dans `statut_demande` pointant vers `APPROUVE`.
  - *Opération Duplicata* : Invalidation de l'ancienne carte résident, insertion d'une nouvelle ligne `carte_resident` liée au `demande_id` (existant ou nouvellement créé).
  - *Opération Transfert* : Modification du `passeport_id` sur la table `visa_transformable` (ou entité liée) et traçabilité via le `demande_id`.

### 4. Clôture de l'Opération
- Succès : Redirection vers une page de confirmation récapitulant les données de l'opération et l'ID de la demande associée/créée.
- Échec : Annulation complète (rollback) et renvoi à l'écran de saisie avec le motif de rejet exact.
