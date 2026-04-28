SPRINT 2 — DUPLICATA CARTE RESIDENT + TRANSFERT VISA

========================
FANIRY (BACKEND)
====================================================

## Branche: sprint/2/duplicata-transfert/backend

* Contraintes Globales :
    * AUCUNE modification de la structure de base de donnees (les tables `carte_resident`, `visa`, `demande`, `statut_demande` sont suffisantes telles quelles).
    * Pour realiser une operation (Duplicata ou Transfert), le systeme doit imperativenent associer une `demande`.
    * Principe de la demande: Soit l'agent saisit un `id_demande` existant pour recuperer l'identite du demandeur, soit il cree une nouvelle demande depuis l'ecran courant qui sera directement liee et approuvee (`statut_demande = APPROUVE`).

* Objectif

    * implementer la logique metier complete des 2 fonctionnalites
    * chaque fonctionnalite doit etre liee a une demande (soit existante passee en parametre, soit creee a la volee et directement approuvee)

* Taches repositories a creer

    * `CarteResidentRepository`
    * `StatutCarteResidentRepository`
    * `ReferenceStatutCarteResidentRepository`
    * `StatutVisaRepository`
    * `ReferenceStatutVisaRepository`

* Taches constantes

    * ajouter dans `StatusConstante`:

        * `DEMANDE_APPROUVE = 3`

* Taches DTO backend

    * creer `DuplicataForm`
    * creer `TransfertVisaForm`
    * ne pas modifier `DemandeForm`

* Taches service

    * creer un service dedie (exemple: `OperationTitreService`)
    * exposer ces methodes:

        * `traiterDuplicata(DuplicataForm form)`
        * `traiterTransfertVisa(TransfertVisaForm form)`

* Regles metier obligatoires — DUPLICATA

    1. verifier le choix ou la creation de la `demande`:
       - Soit on passe `id_demande` existante
       - Soit on cree une `demande` (avec statut a APPROUVE)
    2. lier les informations identite demandeur a cette `demande`
    3. verifier existence carte resident source et que la demande et titulaire correspondent
    4. verifier carte resident eligible (statut courant VALIDE)
    5. verifier motif duplicata dans {PERTE, DETERIORATION}
    6. creer la nouvelle carte resident liee a cette demande
    7. invalider l'ancienne carte

* Regles metier obligatoires — TRANSFERT

    1. verifier le choix ou la creation de la `demande` de transfert (comme duplicata)
    2. lier les informations identite demandeur a la `demande`
    3. verifier existence visa transformable par reference
    4. verifier visa non expire
    5. verifier ancien passeport lie est expire
    6. verifier nouveau passeport est valide
    7. verifier coherence demandeur entre ancien et nouveau passeport
    8. mettre a jour `visa_transformable.passeport_id` vers le nouveau passeport
    9. conserver la trace de transfert via la demande (creee/approuvee ou transferee)

* Taches controller/backend API

    * ajouter routes:

        * `GET /demandes/duplicata/nouvelle`
        * `POST /demandes/duplicata`
        * `GET /demandes/transfert/nouvelle`
        * `POST /demandes/transfert`
        * `GET /demandes/duplicata/recherche?passeportNumero=...`
        * `GET /demandes/transfert/recherche?visaReference=...`

* Contrat reponse API

    * succes: `success=true`, `message`, `data`
    * erreurs:

        * `400` donnees invalides
        * `404` ressource introuvable
        * `409` conflit metier

* Qualite technique

    * `@Transactional` sur chaque traitement complet
    * rollback sur toute erreur metier
    * supprimer tout `System.out.println`

========================
MANJAKA (FRONTEND)
====================================================

## Branche: sprint/2/duplicata-transfert/front

* Objectif

    * implementer les ecrans et le JS des 2 fonctionnalites en consommant les endpoints backend

* Taches templates

    * creer:

        * `templates/demandes/duplicata.html`
        * `templates/demandes/transfert.html`

    * ajouter les liens dans `templates/fragments/layout.html`

* Taches JavaScript

    * creer:

        * `static/js/demandes/duplicata.js`
        * `static/js/demandes/transfert.js`

    * pre-remplir les formulaires via:

        * `GET /demandes/duplicata/recherche`
        * `GET /demandes/transfert/recherche`

    * soumettre vers:

        * `POST /demandes/duplicata`
        * `POST /demandes/transfert`

* Champs minimaux ecran DUPLICATA

    * champ (optionnel) de saisie `id_demande` pour rechercher une demande (et pre-remplir l'identite)
    * identite demandeur (ou champs de saisie pour creer la demande directement)
    * passeport courant
    * carte resident source
    * motif duplicata (PERTE/DETERIORATION)

* Champs minimaux ecran TRANSFERT

    * champ (optionnel) de saisie `id_demande` pour rechercher une demande (et pre-remplir l'identite)
    * identite demandeur (ou champs de saisie pour creer la demande directement)
    * reference visa transformable
    * passeport actuel (expire)
    * nouveau passeport (numero, date delivrance, date expiration)

* UX obligatoire

    1. Option visible en premier : Saisir/Chercher `id_demande` (remplit l'identite et griser) ou Remplir formulaire creation de demande
    2. recherche de la source operee par le backend (carte/visa)
    3. affichage resultat clair
    4. blocage soumission si donnees manquantes
    5. affichage message succes/erreur depuis `message`
    6. redirection vers confirmation apres succes

========================
VALIDATION FINALE (COMMUNE)
====================================================

* Duplicata

    * la fonctionnalite cree une demande
    * la demande creee est directement APPROUVEE
    * la nouvelle carte resident est liee a cette demande approuvee

* Transfert

    * la fonctionnalite cree une demande
    * la demande creee est directement APPROUVEE
    * le transfert de visa est trace par cette demande approuvee

* Global

    * pas de data-init dans le sprint 2
    * pas de regression sur `/demandes/nouvelle`
