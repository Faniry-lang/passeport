package itu.passeport.repositories;

import itu.passeport.entities.StatutCarteResident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatutCarteResidentRepository extends JpaRepository<StatutCarteResident, Integer> {
    Optional<StatutCarteResident> findFirstByCarteResidentIdOrderByDateStatutDesc(Integer carteResidentId);
}
