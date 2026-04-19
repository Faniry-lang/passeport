package itu.passeport.repositories;

import itu.passeport.entities.ReferenceStatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceStatutDemandeRepository extends JpaRepository<ReferenceStatutDemande, Integer> {
}

