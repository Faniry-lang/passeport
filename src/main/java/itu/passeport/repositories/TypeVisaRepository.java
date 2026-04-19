package itu.passeport.repositories;

import itu.passeport.entities.TypeVisa;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.util.Optional;

public interface TypeVisaRepository extends JpaRepository<TypeVisa, Integer> {
    //Optional<TypeVisa> findByCode(String code);
}

