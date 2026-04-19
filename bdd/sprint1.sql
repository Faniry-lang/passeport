-- =========================
-- RESET COMPLET
-- =========================
TRUNCATE TABLE
    statut_carte_resident,
reference_statut_carte_resident,
carte_resident,
statut_visa,
reference_statut_visa,
visa,
statut_demande,
piece_demande,
champ_type_visa_demande,
demande,
reference_statut_demande,
reference_champ_type_visa,
piece_obligatoire_type_visa,
reference_piece_justificative,
type_visa,
visa_transformable,
passeport,
demandeur,
nationalite,
situation_familiale
RESTART IDENTITY CASCADE;

-- =========================
-- SITUATION FAMILIALE
-- =========================
INSERT INTO situation_familiale (nom) VALUES
                                          ('Célibataire'),
                                          ('Marié(e)'),
                                          ('Divorcé(e)'),
                                          ('Veuf(ve)');

-- =========================
-- NATIONALITÉ
-- =========================
INSERT INTO nationalite (nom) VALUES
                                  ('Malagasy'),
                                  ('Française'),
                                  ('Canadienne'),
                                  ('Chinoise'),
                                  ('Indienne'),
                                  ('Sud-africaine');

-- =========================
-- TYPE VISA
-- =========================
INSERT INTO type_visa (code, nom) VALUES
                                      ('TRAV', 'Travailleur'),
                                      ('INV', 'Investisseur');

-- =========================
-- PIÈCES JUSTIFICATIVES
-- =========================
INSERT INTO reference_piece_justificative (nom) VALUES
                                                    ('Passeport (copie)'),
                                                    ('Photo d''identité'),
                                                    ('Curriculum Vitae'),
                                                    ('Contrat de travail'),
                                                    ('Lettre de motivation'),
                                                    ('Plan d''investissement'),
                                                    ('Attestation bancaire'),
                                                    ('Casier judiciaire');

-- =========================
-- PIÈCES OBLIGATOIRES PAR TYPE VISA
-- =========================

-- Travailleur (id = 1)
INSERT INTO piece_obligatoire_type_visa (type_visa_id, reference_piece_id, obligatoire) VALUES
                                                                                            (1, 1, TRUE),
                                                                                            (1, 2, TRUE),
                                                                                            (1, 3, TRUE),
                                                                                            (1, 4, TRUE),
                                                                                            (1, 8, TRUE);

-- Investisseur (id = 2)
INSERT INTO piece_obligatoire_type_visa (type_visa_id, reference_piece_id, obligatoire) VALUES
                                                                                            (2, 1, TRUE),
                                                                                            (2, 2, TRUE),
                                                                                            (2, 6, TRUE),
                                                                                            (2, 7, TRUE);

-- =========================
-- CHAMPS DYNAMIQUES PAR TYPE VISA
-- =========================

-- Travailleur
INSERT INTO reference_champ_type_visa (nom, type_visa_id, type_champ) VALUES
                                                                          ('Nom de l''entreprise', 1, 'TEXT'),
                                                                          ('Poste occupé', 1, 'TEXT'),
                                                                          ('Salaire mensuel', 1, 'NUMBER');

-- Investisseur
INSERT INTO reference_champ_type_visa (nom, type_visa_id, type_champ) VALUES
                                                                          ('Nom de l''entreprise', 2, 'TEXT'),
                                                                          ('Montant de l''investissement', 2, 'NUMBER'),
                                                                          ('Secteur d''activité', 2, 'TEXT');

-- =========================
-- STATUT DEMANDE
-- =========================
INSERT INTO reference_statut_demande (nom) VALUES
                                               ('CREE'),
                                               ('SCAN_TERMINE'),
                                               ('APPROUVE');

-- =========================
-- STATUT VISA
-- =========================
INSERT INTO reference_statut_visa (nom) VALUES
                                            ('EN_COURS'),
                                            ('ACTIF'),
                                            ('EXPIRE');

-- =========================
-- STATUT CARTE RÉSIDENT
-- =========================
INSERT INTO reference_statut_carte_resident (nom) VALUES
                                                      ('EN_ATTENTE'),
                                                      ('VALIDE'),
                                                      ('EXPIRE');

-- =========================
-- DEMANDEURS
-- =========================
INSERT INTO demandeur (nom, prenom, nom_jeune_fille, dtn, situation_familiale_id, nationalite_id, adresse, email, telephone) VALUES
                                                                                                                                 ('RAKOTO', 'Jean', NULL, '1990-05-12', 1, 1, 'Antananarivo', '[jean.rakoto@mail.com](mailto:jean.rakoto@mail.com)', '0340000001'),
                                                                                                                                 ('RANDRIA', 'Sophie', 'RAZAFY', '1985-08-22', 2, 2, 'Fianarantsoa', '[sophie.randria@mail.com](mailto:sophie.randria@mail.com)', '0340000002'),
                                                                                                                                 ('SMITH', 'John', NULL, '1992-03-10', 1, 3, 'Toronto', '[john.smith@mail.com](mailto:john.smith@mail.com)', '0340000003'),
                                                                                                                                 ('LI', 'Wei', NULL, '1988-11-05', 2, 4, 'Beijing', '[li.wei@mail.com](mailto:li.wei@mail.com)', '0340000004'),
                                                                                                                                 ('PATEL', 'Anita', NULL, '1995-01-17', 1, 5, 'Mumbai', '[anita.patel@mail.com](mailto:anita.patel@mail.com)', '0340000005');

-- =========================
-- PASSEPORTS
-- =========================

-- Jean RAKOTO (id = 1) → 2 passeports (test multi-passeport)
INSERT INTO passeport (numero, date_delivrance, date_expiration, demandeur_id) VALUES
                                                                                   ('MG1234567', '2018-01-01', '2028-01-01', 1),
                                                                                   ('MG7654321', '2023-02-01', '2033-02-01', 1);

-- Sophie RANDRIA (id = 2)
INSERT INTO passeport (numero, date_delivrance, date_expiration, demandeur_id) VALUES
    ('FR9876543', '2020-06-15', '2030-06-15', 2);

-- John SMITH (id = 3)
INSERT INTO passeport (numero, date_delivrance, date_expiration, demandeur_id) VALUES
    ('CA4567891', '2019-09-10', '2029-09-10', 3);

-- Li WEI (id = 4)
INSERT INTO passeport (numero, date_delivrance, date_expiration, demandeur_id) VALUES
    ('CN1122334', '2021-04-20', '2031-04-20', 4);

-- Anita PATEL (id = 5)
INSERT INTO passeport (numero, date_delivrance, date_expiration, demandeur_id) VALUES
    ('IN9988776', '2022-07-01', '2032-07-01', 5);
