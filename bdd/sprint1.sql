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
                                                    ('Photo d’identité'),
                                                    ('Curriculum Vitae'),
                                                    ('Contrat de travail'),
                                                    ('Lettre de motivation'),
                                                    ('Plan d’investissement'),
                                                    ('Attestation bancaire'),
                                                    ('Casier judiciaire');

-- =========================
-- PIÈCES OBLIGATOIRES PAR TYPE VISA
-- =========================

-- Travailleur (id = 1)
INSERT INTO piece_obligatoire_type_visa (type_visa_id, reference_piece_id, obligatoire) VALUES
                                                                                            (1, 1, TRUE), -- Passeport
                                                                                            (1, 2, TRUE), -- Photo
                                                                                            (1, 3, TRUE), -- CV
                                                                                            (1, 4, TRUE), -- Contrat
                                                                                            (1, 8, TRUE); -- Casier

-- Investisseur (id = 2)
INSERT INTO piece_obligatoire_type_visa (type_visa_id, reference_piece_id, obligatoire) VALUES
                                                                                            (2, 1, TRUE), -- Passeport
                                                                                            (2, 2, TRUE), -- Photo
                                                                                            (2, 6, TRUE), -- Plan investissement
                                                                                            (2, 7, TRUE); -- Attestation bancaire

-- =========================
-- CHAMPS DYNAMIQUES PAR TYPE VISA
-- =========================

-- Travailleur
INSERT INTO reference_champ_type_visa (nom, type_visa_id, type_champ) VALUES
                                                                          ('Nom de l’entreprise', 1, 'TEXT'),
                                                                          ('Poste occupé', 1, 'TEXT'),
                                                                          ('Salaire mensuel', 1, 'NUMBER');

-- Investisseur
INSERT INTO reference_champ_type_visa (nom, type_visa_id, type_champ) VALUES
                                                                          ('Nom de l’entreprise', 2, 'TEXT'),
                                                                          ('Montant de l’investissement', 2, 'NUMBER'),
                                                                          ('Secteur d’activité', 2, 'TEXT');

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
