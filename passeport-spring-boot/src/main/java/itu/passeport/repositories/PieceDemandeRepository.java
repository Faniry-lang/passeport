package itu.passeport.repositories;

import itu.passeport.entities.PieceDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PieceDemandeRepository extends JpaRepository<PieceDemande, Integer> {
    Optional<PieceDemande> findByDemandeIdAndReferencePieceJustificativeId(Integer demandeId, Integer referencePieceId);

    List<PieceDemande> findByDemandeId(Integer demandeId);
}
