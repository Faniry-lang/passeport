package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "statut_demande")
public class StatutDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_statut_demande_id", nullable = false)
    private ReferenceStatutDemande referenceStatutDemande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_statut", nullable = false)
    private Instant dateStatut;


}