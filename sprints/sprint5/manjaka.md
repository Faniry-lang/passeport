MANJAKA (BACKEND)
====================================================

## Branche: sprint/5/webcam-signature/backend

* Objectif
    * ajouter le nouvel etat de demande
    * implementer le stockage des fichiers media (photo & signature) lier a la demande
    * appliquer les validations de passage d'etat bloquantes
    * gerer l'export carte resident incluant les medias (si genere cote server)

* Taches etat demande
    * ajouter `PHOTO_TERMINEE` (valeur numerique appropriee) dans `StatusConstante`
    * s'assurer que l'etat `PHOTO_TERMINEE` precede `SCAN_TERMINE` dans le workflow logique

* Taches modification entites et base de donnees
    * ajouter colonnes pour stocker les references/chemins de `photo_path` et `signature_path` dans l'entite `Demande`
    * OU creer une entite/table dediee (ex: `DemandeMedia`) si architecture privilegie une relation
    * **Note**: la structure doit correspondre aux bonnes pratiques du projet

* Taches controller et service
    * Endpoint de reception: creer un endpoint (ex: `POST /demandes/{id}/medias`) pour recevoir les images provenant du Front (Base64 ou Multipart)
    * Methode de validation d'etat : modifier le service de validation/changement d'etat vers `SCAN_TERMINE` 
        * Règle métier bloquante: Si Photo = null OR Signature = null => Exception metier (Ne peut pas passer a l'etape `SCAN_TERMINE`)
    * Service de stockage des donnees de la requete (sauvegarde sur le systeme de fichier + URL en bdd, et non BLOB pur)

* Qualite technique
    * Verifier le format d'image envoye (png, jpg, etc) pour securiser le telechargement
    * Messages d'erreur explicites depuis l'API si le passage a `SCAN_TERMINE` bloque par manque de mediass

* Export (Côté backend)
    * Lors de la generation de la carte resident exportable (Si implemente cote serveur avec Thymeleaf/PDF ou autre), joindre dynamiquement l'image de la signature et la photo du demandeur au fichier exporte.
