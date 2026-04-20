package itu.passeport.repositories;

import itu.passeport.entities.Visa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisaRepository extends JpaRepository<Visa, Integer> {
}

