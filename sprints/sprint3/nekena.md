- ajouter boutton "ajouter fichier"
- creer fonction upload et donwnload fichier avec optimisation de taille et de nom de fichier
- stocker les fichier dans un dossier storage/piece_justificative


Details:
- créer une page pour upload des fichiers pour chaque piece justificative d'une demande donnée
- on doit lister tous les labels des pièces justificatives et avoir un bouton upload sur chaque piece
- en haut on a un bouton valider qui va enregistrer les fichiers dans la base
    -table: piece_demande
- pas de bouton scan terminé dans la page upload
- le changement de statut SCAN_TERMINE se fait uniquement depuis la page liste des demandes
- si statut d'une demande est SCAN_TERMINE, on ne peut plus re-upload de fichier, mais tant que l'etat n'est pas SCAN_TERMINE, on peut toujours re-upload les fichiers des pieces justificatives

Implementation detaillee:

Objectif fonctionnel:
- Fournir un ecran de scan/upload des pieces justificatives pour une `demande` donnee.
- Permettre re-upload tant que la demande n'est pas `SCAN_TERMINE`.
- Verrouiller l'upload quand le statut courant devient `SCAN_TERMINE`.

Tables concernees:
- `piece_demande`
    - champs utilises: `id`, `demande_id`, `reference_piece_justificative_id`, `lien_fichier`, `date_ajout`
- `piece_obligatoire_type_visa`
    - pour savoir quelles pieces afficher selon le type de visa de la demande
- `reference_piece_justificative`
    - pour afficher les labels des pieces
- `statut_demande`
    - ajout du statut `SCAN_TERMINE`
- `reference_statut_demande`
    - lecture de la reference `SCAN_TERMINE`

Classes/backend concernees:
- `DemandeController` (nouveaux endpoints page upload + endpoint scan termine depuis la liste)
- `DemandeService` (orchestration metier)
- `PieceDemandeRepository`
- `PieceObligatoireTypeVisaRepository`
- `ReferencePieceJustificativeRepository`
- `StatutDemandeRepository`
- `ReferenceStatutDemandeRepository`
- DTO a creer:
    - `PieceUploadItemDto` (pieceId, pieceLabel, fichierExistant, uploadable)
    - `PieceUploadRequestDto` (pieceId, fichier)
    - `ScanStateDto` (demandeId, statutCourant, scanTermine)

Fonctions a creer/adapter:
- Dans `PieceDemandeRepository`:
    - `Optional<PieceDemande> findByDemandeIdAndReferencePieceJustificativeId(Integer demandeId, Integer referencePieceId)`
    - `List<PieceDemande> findByDemandeId(Integer demandeId)`
- Dans `StatutDemandeRepository`:
    - `Optional<StatutDemande> findFirstByDemandeIdOrderByDateStatutDesc(Integer demandeId)`
- Dans `ReferenceStatutDemandeRepository`:
    - `Optional<ReferenceStatutDemande> findByNomIgnoreCase(String nom)`
- Dans `DemandeService`:
    - `List<PieceUploadItemDto> listerPiecesPourUpload(Integer demandeId)`
    - `void enregistrerPieceUpload(Integer demandeId, Integer referencePieceId, MultipartFile fichier)`
    - `Resource telechargerPiece(Integer pieceDemandeId)`
    - `void marquerScanTermine(Integer demandeId)`
    - `boolean estScanTermine(Integer demandeId)`

Stockage fichiers:
- Dossier racine: `storage/piece_justificative`
- Convention de nommage:
    - `<demandeId>_<referencePieceId>_<timestamp>_<nomNettoye>.<ext>`
- Optimisation/minimisation:
    - nettoyer le nom (pas d'espace/char speciaux)
    - limiter taille max (ex: 5MB ou config application)
    - limiter types autorises (pdf, jpg, jpeg, png)

Endpoints proposes:
- `GET /demandes/{id}/pieces/upload`
    - retourne la page d'upload pour la demande
- `POST /demandes/{id}/pieces/upload`
    - upload d'un fichier pour une piece donnee (creer ou remplacer)
- `GET /demandes/pieces/{pieceDemandeId}/download`
    - download du fichier
- `POST /demandes/{id}/scan-termine`
    - ajoute statut `SCAN_TERMINE` (declenche depuis page liste des demandes, pas depuis page upload)

Comportement UI:
- Afficher la liste de toutes les pieces attendues (label + etat fichier present/absent).
- Sur chaque ligne:
    - bouton `Ajouter fichier` ou `Remplacer fichier` si scan non termine
    - bouton `Telecharger` si fichier existe
- En haut:
    - bouton `Valider` uniquement (sauvegarde des uploads en cours)
- Si `SCAN_TERMINE`:
    - desactiver inputs/boutons upload
    - garder telechargement autorise

Regles metier:
- Re-upload autorise tant que statut courant != `SCAN_TERMINE`.
- Apres `SCAN_TERMINE`, toute tentative upload doit retourner 409.
- L'action `Scan termine` ajoute un nouveau statut dans l'historique (ne pas ecraser les precedents).
- Upload interdit si la demande n'existe pas (404).

Critere d'acceptation:
- Les labels de pieces sont visibles pour la demande cible.
- Upload et remplacement fonctionnent avant scan termine.
- Download fonctionne pour piece existante.
- Clic `Scan termine` depuis la page liste verrouille les re-uploads.
- Les enregistrements `piece_demande` sont bien persistes avec `lien_fichier` et `date_ajout`.

ce n'est pas a toi de creer la page liste de demande avec le bouton scan terminé