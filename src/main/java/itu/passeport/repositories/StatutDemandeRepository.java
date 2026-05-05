package itu.passeport.repositories;

import itu.passeport.entities.StatutDemande;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Integer> {
    Optional<StatutDemande> findFirstByDemandeIdOrderByDateStatutDesc(Integer demandeId);
}
