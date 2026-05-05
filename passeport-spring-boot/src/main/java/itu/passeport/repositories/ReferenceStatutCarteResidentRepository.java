package itu.passeport.repositories;

import itu.passeport.entities.ReferenceStatutCarteResident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenceStatutCarteResidentRepository extends JpaRepository<ReferenceStatutCarteResident, Integer> {
    Optional<ReferenceStatutCarteResident> findByNomIgnoreCase(String nom);
}
