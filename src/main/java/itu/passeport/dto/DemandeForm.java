package itu.passeport.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DemandeForm {
    private String nom;
    private String prenom;
    private String nomJeuneFille;
    private LocalDate dtn;
    private Integer situationFamilialeId;
    private Integer nationaliteId;
    private String adresse;
    private String email;
    private String telephone;

    private String passeportNumero;
    private LocalDate passeportDateDelivrance;
    private LocalDate passeportDateExpiration;

    private String visaReference;
    private LocalDate visaDateDelivrance;
    private LocalDate visaDateExpiration;

    private Integer typeVisaId;
    private LocalDate dateEntree;
    private String lieuEntree;

    private Set<Integer> pieceIds = new HashSet<>();
    private Map<Integer, String> champsDynamiques = new HashMap<>();
}

