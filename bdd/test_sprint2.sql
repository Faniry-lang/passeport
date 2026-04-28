-- =========================================================================
-- JEU D'ESSAI SPRINT 2 (A exécuter APRÈS sprint1.sql)
-- =========================================================================

-- 1. Modification du passeport de Jean RAKOTO (id=1) 
-- On force l'un de ses passeports (MG1234567) à être EXPIRÉ.
-- Ceci est une condition MAJEURE pour tester le Transfert : l'ancien passeport doit être expiré.
update passeport
   set date_expiration = '2025-01-01',
       date_delivrance = '2015-01-01'
 where numero = 'MG1234567';

-- 2. Création de Visas Transformables
-- Ces visas sont nécessaires pour qu'ils puissent être transférés.

-- Visa 1: Pour Sophie RANDRIA (sur son passeport FR9876543, passeport_id=3 valide)
insert into visa_transformable (
   reference,
   passeport_id,
   date_delivrance,
   date_expiration
) values ( 'V-TRANS-SOPHIE',
           3,
           '2021-01-01',
           '2026-12-31' );

-- Visa 2: Pour Jean RAKOTO (sur son passeport MG1234567, passeport_id=1 EXPIRÉ)
-- Il est le candidat idéal pour tester un Transfert vers son autre passeport MG7654321 !
insert into visa_transformable (
   reference,
   passeport_id,
   date_delivrance,
   date_expiration
) values ( 'V-TRANS-JEAN',
           1,
           '2022-01-01',
           '2027-01-01' ); 

-- 3. Création de Demandes "Souches"
-- Toute carte ou transfert est rattaché à une demande. On crée une demande historique.
insert into demande (
   demandeur_id,
   passeport_id,
   visa_transformable_id,
   type_visa_id,
   date_demande
) values ( 2,
           3,
           1,
           1,
           '2021-02-01' );  -- Demande historique pour Sophie (id=1)

-- On approuve cette demande (ID statut 3 = APPROUVE)
insert into statut_demande (
   reference_statut_demande_id,
   demande_id,
   date_statut
) values ( 3,
           1,
           '2021-02-15' );

-- 4. Création d'une Carte de Résident VALIDE
-- Cela est OBLIGATOIRE pour pouvoir tester la fonctionnalité de Duplicata.
-- On l'attribue à Sophie RANDRIA, liée à la demande n°1.
insert into carte_resident (
   demande_id,
   passeport_id,
   date_debut,
   date_fin
) values ( 1,
           3,
           '2021-02-15',
           '2031-02-15' ); -- id=1

-- Ajout du statut VALIDE (qui correspond à l'ID 2 dans le sprint1.sql)
insert into statut_carte_resident (
   carte_resident_id,
   reference_statut_carte_resident_id,
   date_statut
) values ( 1,
           2,
           '2021-02-15' );

-- 5. Ajout du statut "ANNULE" / "INVALIDE" 
-- Le sprint1 n'avait que 'EN_ATTENTE', 'VALIDE', 'EXPIRE'. 
-- Pour qu'un duplicata désactive rigoureusement l'ancienne carte, on insère le statut ANNULE (ID=4).
insert into reference_statut_carte_resident ( nom ) values ( 'ANNULE' );


-- 1. On raccroche le visa V-TRANS-SOPHIE à son VRAI passeport (FR9876543)
update visa_transformable
   set
   passeport_id = (
      select id
        from passeport
       where numero = 'FR9876543'
   )
 where reference = 'V-TRANS-SOPHIE';

-- 2. On rend son VRAI passeport français EXPIRÉ dans le passé (Condition obligatoire pour réaliser un transfert)
update passeport
   set date_expiration = '2025-01-01',
       date_delivrance = '2015-01-01'
 where numero = 'FR9876543';

-- 3. On met à jour la "demande 1" pour qu'elle pointe aussi vers l'ID du bon passeport de Sophie
update demande
   set
   passeport_id = (
      select id
        from passeport
       where numero = 'FR9876543'
   )
 where id = 1;

-- 4. On met à jour la "carte_resident 1" pour qu'elle pointe vers l'ID du bon passeport de Sophie
update carte_resident
   set
   passeport_id = (
      select id
        from passeport
       where numero = 'FR9876543'
   )
 where id = 1;