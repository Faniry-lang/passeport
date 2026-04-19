CREATE TABLE situation_familiale (
                                     id SERIAL PRIMARY KEY,
                                     nom VARCHAR(255) NOT NULL
);

CREATE TABLE nationalite (
                             id SERIAL PRIMARY KEY,
                             nom VARCHAR(255) NOT NULL
);

CREATE TABLE demandeur (
                           id SERIAL PRIMARY KEY,
                           nom VARCHAR(255) NOT NULL,
                           prenom VARCHAR(255),
                           nom_jeune_fille VARCHAR(255),
                           dtn DATE NOT NULL,
                           situation_familiale_id INTEGER NOT NULL REFERENCES situation_familiale(id),
                           nationalite_id INTEGER NOT NULL REFERENCES nationalite(id),
                           adresse VARCHAR(255),
                           email VARCHAR(255),
                           telephone VARCHAR(20)
);

CREATE TABLE passeport (
                           id SERIAL PRIMARY KEY,
                           numero VARCHAR(255) NOT NULL UNIQUE,
                           date_delivrance DATE NOT NULL,
                           date_expiration DATE NOT NULL,
                           demandeur_id INTEGER NOT NULL REFERENCES demandeur(id),
                           CHECK (date_expiration > date_delivrance)
);

CREATE TABLE visa_transformable (
                                    id SERIAL PRIMARY KEY,
                                    reference VARCHAR(255) NOT NULL UNIQUE,
                                    passeport_id INTEGER NOT NULL REFERENCES passeport(id),
                                    date_delivrance DATE,
                                    date_expiration DATE
);

CREATE TABLE type_visa (
                           id SERIAL PRIMARY KEY,
                           code VARCHAR(10) NOT NULL UNIQUE,
                           nom VARCHAR(100) NOT NULL
);

CREATE TABLE reference_piece_justificative (
                                               id SERIAL PRIMARY KEY,
                                               nom VARCHAR(255) NOT NULL
);

CREATE TABLE piece_obligatoire_type_visa (
                                             id SERIAL PRIMARY KEY,
                                             type_visa_id INTEGER NOT NULL REFERENCES type_visa(id),
                                             reference_piece_id INTEGER NOT NULL REFERENCES reference_piece_justificative(id),
                                             obligatoire BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE reference_champ_type_visa (
                                           id SERIAL PRIMARY KEY,
                                           nom VARCHAR(255) NOT NULL,
                                           type_visa_id INTEGER NOT NULL REFERENCES type_visa(id),
                                           type_champ VARCHAR(50) NOT NULL
);

CREATE TABLE reference_statut_demande (
                                          id SERIAL PRIMARY KEY,
                                          nom VARCHAR(255) NOT NULL
);

CREATE TABLE demande (
                         id SERIAL PRIMARY KEY,
                         demandeur_id INTEGER NOT NULL REFERENCES demandeur(id),
                         passeport_id INTEGER NOT NULL REFERENCES passeport(id),
                         visa_transformable_id INTEGER NOT NULL REFERENCES visa_transformable(id),
                         type_visa_id INTEGER NOT NULL REFERENCES type_visa(id),
                         date_demande DATE NOT NULL
);

CREATE TABLE champ_type_visa_demande (
                                         id SERIAL PRIMARY KEY,
                                         demande_id INTEGER NOT NULL REFERENCES demande(id),
                                         reference_champ_type_visa_id INTEGER NOT NULL REFERENCES reference_champ_type_visa(id),
                                         valeur TEXT
);

CREATE TABLE piece_demande (
                               id SERIAL PRIMARY KEY,
                               reference_piece_justificative_id INTEGER NOT NULL REFERENCES reference_piece_justificative(id),
                               demande_id INTEGER NOT NULL REFERENCES demande(id),
                               lien_fichier VARCHAR(255),
                               date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE statut_demande (
                                id SERIAL PRIMARY KEY,
                                reference_statut_demande_id INTEGER NOT NULL REFERENCES reference_statut_demande(id),
                                demande_id INTEGER NOT NULL REFERENCES demande(id),
                                date_statut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE visa (
                      id SERIAL PRIMARY KEY,
                      demande_id INTEGER NOT NULL REFERENCES demande(id),
                      type_visa_id INTEGER NOT NULL REFERENCES type_visa(id),
                      reference VARCHAR(255) UNIQUE,
                      date_entree DATE NOT NULL,
                      lieu_entree VARCHAR(255),
                      date_expiration DATE NOT NULL
);

CREATE TABLE reference_statut_visa (
                                       id SERIAL PRIMARY KEY,
                                       nom VARCHAR(255) NOT NULL
);

CREATE TABLE statut_visa (
                             id SERIAL PRIMARY KEY,
                             visa_id INTEGER NOT NULL REFERENCES visa(id),
                             reference_statut_visa_id INTEGER NOT NULL REFERENCES reference_statut_visa(id),
                             date_statut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE carte_resident (
                                id SERIAL PRIMARY KEY,
                                demande_id INTEGER NOT NULL REFERENCES demande(id),
                                passeport_id INTEGER NOT NULL REFERENCES passeport(id),
                                date_debut DATE,
                                date_fin DATE
);

CREATE TABLE reference_statut_carte_resident (
                                                 id SERIAL PRIMARY KEY,
                                                 nom VARCHAR(255) NOT NULL
);

CREATE TABLE statut_carte_resident (
                                       id SERIAL PRIMARY KEY,
                                       carte_resident_id INTEGER NOT NULL REFERENCES carte_resident(id),
                                       reference_statut_carte_resident_id INTEGER NOT NULL REFERENCES reference_statut_carte_resident(id),
                                       date_statut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
