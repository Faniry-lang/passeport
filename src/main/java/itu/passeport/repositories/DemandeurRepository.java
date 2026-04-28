package itu.passeport.repositories;

import itu.passeport.entities.Demandeur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DemandeurRepository extends JpaRepository<Demandeur, Integer> {

    Optional<Demandeur> findByNomAndPrenomAndDtn(String nom, String prenom, java.time.LocalDate dtn);
}
