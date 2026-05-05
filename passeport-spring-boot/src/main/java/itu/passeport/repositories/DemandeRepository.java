package itu.passeport.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import itu.passeport.entities.Demande;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
}

