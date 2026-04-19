package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "statut_carte_resident")
public class StatutCarteResident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carte_resident_id", nullable = false)
    private CarteResident carteResident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_statut_carte_resident_id", nullable = false)
    private ReferenceStatutCarteResident referenceStatutCarteResident;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_statut", nullable = false)
    private Instant dateStatut;


}