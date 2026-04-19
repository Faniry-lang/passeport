package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "champ_type_visa_demande")
public class ChampTypeVisaDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_champ_type_visa_id", nullable = false)
    private ReferenceChampTypeVisa referenceChampTypeVisa;

    @Column(name = "valeur", length = Integer.MAX_VALUE)
    private String valeur;


}