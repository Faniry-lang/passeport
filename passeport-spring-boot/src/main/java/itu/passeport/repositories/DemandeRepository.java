package itu.passeport.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import itu.passeport.entities.Demande;
import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    List<Demande> findByPasseportNumeroOrderByDateDemandeAsc(String numero);
    List<Demande> findByPasseportNumeroOrderByIdAsc(String numero);
    List<Demande> findByPasseportNumeroOrderByIdDesc(String numero);
}
