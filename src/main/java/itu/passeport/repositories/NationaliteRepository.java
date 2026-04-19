package itu.passeport.repositories;

import itu.passeport.entities.Nationalite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NationaliteRepository extends JpaRepository<Nationalite, Integer> {
}

