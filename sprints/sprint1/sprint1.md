SPRINT 1 — INSERTION D’UNE NOUVELLE DEMANDE

========================
NEKENA 
==============================================

## Branche: sprint/1/nouvelle-demande/formulaire

* Création du layout de l'application en template Thymeleaf

    * créer un fichier sidebar réutilisable
    * inclure le sidebar dans toutes les pages
    * ajouter le lien vers le formulaire de demande

* Création du formulaire de demande de nouveau titre

    * afficher TOUJOURS les champs du demandeur :

        * nom
        * prénom
        * nom de jeune fille
        * date de naissance
        * situation familiale
        * nationalité
        * adresse
        * email
        * téléphone

    * afficher les champs passeport :

        * numéro
        * date délivrance
        * date expiration

    * afficher les champs visa transformable :

        * référence
        * date délivrance (optionnel)
        * date expiration

    * afficher :

        * type de visa
        * date d’entrée
        * lieu d’entrée

    * afficher les pièces justificatives :

        * sous forme de checkbox dynamiques selon le type de visa

    * afficher les champs dynamiques liés au type de visa

* Fonctionnalité de recherche par numéro de passeport

    * champ de recherche (numéro passeport)

    * bouton ou déclenchement automatique (AJAX)

    * si passeport trouvé :

        * auto-remplir :

            * champs demandeur
            * champs passeport
        * rendre ces champs modifiables ou non selon besoin

    * si passeport non trouvé :

        * laisser les champs vides pour saisie manuelle

* Controller (GET)

    * route pour afficher le formulaire de création

        * charger :

            * liste des types de visa
            * liste des situations familiales
            * liste des nationalités
        * initialiser le formulaire

    * route de recherche par passeport (API interne)

        * entrée : numéro de passeport
        * sortie :

            * informations demandeur + passeport si trouvés
            * sinon réponse vide

========================
MANJAKA 
====================================================

## Branche: sprint/1/nouvelle-demande/metier

* Création du service DemandeService

    * méthode : créer une demande à partir des données du formulaire
    * méthode : rechercher un passeport par numéro (avec demandeur associé)

* Logique métier : création d’une demande

    1. Recherche du passeport

        * chercher par numéro
        * si trouvé :

            * récupérer passeport + demandeur
        * sinon :

            * créer un nouveau demandeur avec les informations du formulaire
            * créer un nouveau passeport lié à ce demandeur

    2. Gestion du visa transformable (externe au système)

        * chercher par référence
        * si non trouvé :

            * créer un visa transformable avec :

                * référence
                * passeport associé
                * dates fournies

    3. Vérifications métier

        * vérifier que le visa transformable n’est pas expiré
        * vérifier cohérence :

            * le visa transformable est bien lié au passeport utilisé
        * sinon → erreur

    4. Création de la demande

        * insertion avec :

            * demandeur
            * passeport
            * visa transformable
            * type de visa
            * date et lieu d’entrée

    5. Validation des pièces justificatives

        * récupérer les pièces obligatoires selon le type de visa
        * vérifier que toutes sont cochées
        * sinon → erreur

    6. Insertion des pièces justificatives

        * pour chaque pièce cochée :

            * créer une entrée (sans fichier pour l’instant)

    7. Insertion des champs dynamiques

        * enregistrer les valeurs associées au type de visa

    8. Initialisation du statut de la demande

        * créer un statut initial : CREE

    9. Transaction

        * rollback automatique en cas d’erreur

* Controller (POST)

    * route de soumission du formulaire

        * recevoir les données du formulaire
        * appeler le service de création de demande
        * gérer les erreurs :

            * visa expiré
            * pièces obligatoires manquantes
            * incohérence des données
        * redirection :

            * succès → page de confirmation
            * erreur → retour formulaire avec messages
