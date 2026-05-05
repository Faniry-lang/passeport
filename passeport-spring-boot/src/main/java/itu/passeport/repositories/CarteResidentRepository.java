package itu.passeport.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import itu.passeport.entities.CarteResident;

public interface CarteResidentRepository extends JpaRepository<CarteResident, Integer> {

    Optional<CarteResident> findFirstByPasseportNumeroOrderByIdDesc(String passeportNumero);

}
