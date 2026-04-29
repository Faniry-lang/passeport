- creer un boutton pour valider l'etat du dossier
- creer une focntion pour changer l'etat du dossier en valider lors se que l'on appuie sur le boutton
- verifier l'etat du dossier avant d'afficher le boutton valider et les botton de modification des informations

Details:

Objectif fonctionnel:
- Ajouter un workflow de validation finale de dossier (demande) depuis l'interface.
- Si le dossier est deja valide, ne plus afficher le bouton de validation ni les boutons de modification.

Tables concernees:
- `demande` (lecture de l'identifiant du dossier)
- `statut_demande` (insertion du nouveau statut horodate)
- `reference_statut_demande` (lecture de la reference `VALIDE`)

Classes/backend concernees:
- `DemandeController`
- `DemandeService`
- `StatutDemandeRepository`
- `ReferenceStatutDemandeRepository`
- `DemandeRepository`
- `StatusConstante` (ajouter constante du statut final si necessaire)

Fonctions a creer/adapter:
- Dans `DemandeService`:
	- `boolean estDemandeValidee(Integer demandeId)`
	- `void validerDossier(Integer demandeId)`
	- `Integer recupererDernierStatutDemandeId(Integer demandeId)` (option utilitaire)
- Dans `StatutDemandeRepository`:
	- `Optional<StatutDemande> findFirstByDemandeIdOrderByDateStatutDesc(Integer demandeId)`
- Dans `ReferenceStatutDemandeRepository`:
	- `Optional<ReferenceStatutDemande> findByNomIgnoreCase(String nom)`

Endpoints a ajouter:
- `POST /demandes/{id}/valider-dossier`
	- Action: ajoute une ligne dans `statut_demande` avec la reference `VALIDE`.
	- Reponse: 200 si ok, 409 si deja valide, 404 si dossier introuvable.
- `GET /demandes/{id}/etat`
	- Action: retourne l'etat courant pour pilotage UI.

UI/Front concerne:
- Template de detail/modification de dossier (page ou se trouvent les boutons de modification)
- JS associe a la page dossier:
	- afficher/masquer bouton "Valider dossier"
	- desactiver/masquer boutons de modification quand etat final atteint

Regles metier:
- Un dossier valide ne doit plus etre editable depuis l'IHM.
- La validation ne doit pas supprimer l'historique: on ajoute un nouveau statut (table d'historique).
- Le statut courant est le dernier `statut_demande` par `date_statut`.

Critere d'acceptation:
- Sur dossier non valide: bouton "Valider dossier" visible et clic fonctionnel.
- Apres validation: etat courant passe a `VALIDE`, bouton cache, modifications indisponibles.
- Sur dossier deja valide: endpoint retourne 409 et UI affiche message metier.
