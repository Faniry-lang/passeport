package itu.passeport.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "piece_demande")
public class PieceDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_piece_justificative_id", nullable = false)
    private ReferencePieceJustificative referencePieceJustificative;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @Column(name = "lien_fichier")
    private String lienFichier;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_ajout", nullable = false)
    private Instant dateAjout;


}