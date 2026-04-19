# Fix pièces et champs dynamiques

### Branche: fix/1/piece-champ-dynamique

* Modifier PasseportApiController:
  * Créer un endpoint pour obtenir les références des pièces justificatives et les références des champs pour chaque type de visa lors de la séléction.
* Modifier nouvelle-demande.html pour appeler cette api ensuite pour charger les champs dynamiquement.