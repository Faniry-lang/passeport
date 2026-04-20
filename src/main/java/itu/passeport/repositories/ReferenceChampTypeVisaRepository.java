package itu.passeport.repositories;

import itu.passeport.entities.ReferenceChampTypeVisa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferenceChampTypeVisaRepository extends JpaRepository<ReferenceChampTypeVisa, Integer> {
    List<ReferenceChampTypeVisa> findByTypeVisaId(Integer typeVisaId);
}
