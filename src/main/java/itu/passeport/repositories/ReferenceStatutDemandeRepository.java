package itu.passeport.repositories;

import itu.passeport.entities.ReferenceStatutDemande;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceStatutDemandeRepository extends JpaRepository<ReferenceStatutDemande, Integer> {
	Optional<ReferenceStatutDemande> findByNomIgnoreCase(String nom);
}

