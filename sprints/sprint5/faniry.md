FANIRY (FRONTEND)
====================================================

## Branche: sprint/5/webcam-signature/front

* Objectif
    * implementer l'interface de capture Webcam pour la photo
    * implementer l'interface de capture de signature (canvas/pad)
    * lier ces captures a la fiche de la demande
    * envoyer les flux medias au backend lors de la validation
    * gerer l'affichage de la photo et signature sur la fiche demande `detail.html`

* Taches templates
    * creer/modifier `templates/demande/capture-media.html`
    * modifier `templates/demandes/detail.html` pour ajouter la vue des pieces capturees

* Taches JavaScript
    * utiliser les APIs Web natives (`navigator.mediaDevices.getUserMedia`) pour la webcam
    * utiliser une librairie ou un canvas manuel pour le pad de signature
    * serializer la photo et la signature (ex: Base64) pour les envoyer via formulaire POST ou AJAX vers le backend
    * s'assurer du clean-up des flux webcam apres la prise de la photo

* UX obligatoire
    * affichage du retour camera en direct (preview) avant capture
    * bouton "Prendre la photo" et "Reprendre"
    * bloc canvas clair pour la signature, avec option "Effacer/Recommencer"
    * empecher la validation locale si photo ou signature manquantes

* Export (Client-side optionnel)
    * Si l'export de la carte resident est gere en pure JS, inclure les images capturees dans le document (PDF) genere.
