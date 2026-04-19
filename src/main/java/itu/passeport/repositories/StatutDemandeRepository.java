package itu.passeport.repositories;

import itu.passeport.entities.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Integer> {
}

