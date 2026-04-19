package itu.passeport.repositories;

import itu.passeport.entities.Passeport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
    //Optional<Passeport> findByNumero(String numero);

    @Query("select p from Passeport p join fetch p.demandeur where p.numero = :numero")
    Optional<Passeport> findByNumeroAvecDemandeur(@Param("numero") String numero);
}
