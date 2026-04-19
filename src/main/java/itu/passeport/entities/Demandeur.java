package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "demandeur")
public class Demandeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "nom_jeune_fille")
    private String nomJeuneFille;

    @Column(name = "dtn", nullable = false)
    private LocalDate dtn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "situation_familiale_id", nullable = false)
    private SituationFamiliale situationFamiliale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nationalite_id", nullable = false)
    private Nationalite nationalite;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;


}