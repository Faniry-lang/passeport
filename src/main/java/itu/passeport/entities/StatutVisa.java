package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "statut_visa")
public class StatutVisa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visa_id", nullable = false)
    private Visa visa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_statut_visa_id", nullable = false)
    private ReferenceStatutVisa referenceStatutVisa;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_statut", nullable = false)
    private Instant dateStatut;


}