package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "carte_resident")
public class CarteResident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passeport_id", nullable = false)
    private Passeport passeport;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;


}