package itu.passeport.repositories;

import itu.passeport.entities.Passeport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
    Optional<Passeport> findByNumero(String numero);
}
