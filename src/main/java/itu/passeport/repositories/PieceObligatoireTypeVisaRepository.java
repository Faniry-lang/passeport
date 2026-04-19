package itu.passeport.repositories;

import itu.passeport.entities.PieceObligatoireTypeVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PieceObligatoireTypeVisaRepository extends JpaRepository<PieceObligatoireTypeVisa, Integer> {
    List<PieceObligatoireTypeVisa> findByTypeVisaId(Integer typeVisaId);
}