package itu.passeport.repositories;

import itu.passeport.entities.StatutVisa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatutVisaRepository extends JpaRepository<StatutVisa, Integer> {
    Optional<StatutVisa> findFirstByVisaIdOrderByDateStatutDesc(Integer visaId);
}
