package itu.passeport.repositories;

import itu.passeport.entities.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Integer> {
    Optional<StatutDemande> findFirstByDemandeIdOrderByDateStatutDesc(Integer demandeId);
}
