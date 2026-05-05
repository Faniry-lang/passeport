package itu.passeport.repositories;

import itu.passeport.entities.VisaTransformable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Integer> {
    Optional<VisaTransformable> findByReference(String reference);
}

