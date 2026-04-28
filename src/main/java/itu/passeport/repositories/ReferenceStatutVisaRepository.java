package itu.passeport.repositories;

import itu.passeport.entities.ReferenceStatutVisa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenceStatutVisaRepository extends JpaRepository<ReferenceStatutVisa, Integer> {
    Optional<ReferenceStatutVisa> findByNomIgnoreCase(String nom);
}
