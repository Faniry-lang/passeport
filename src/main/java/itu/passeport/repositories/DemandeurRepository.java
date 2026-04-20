package itu.passeport.repositories;

import itu.passeport.entities.Demandeur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeurRepository extends JpaRepository<Demandeur, Integer> {
}

