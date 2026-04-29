package itu.passeport.repositories;

import itu.passeport.entities.ReferenceStatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReferenceStatutDemandeRepository extends JpaRepository<ReferenceStatutDemande, Integer> {
    Optional<ReferenceStatutDemande> findByNomIgnoreCase(String nom);
}
