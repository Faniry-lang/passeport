package itu.passeport.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dtn;
    private Integer situationFamilialeId;
    private Integer nationaliteId;
    private String adresse;
    private String email;
    private String telephone;

    private String passeportNumero;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate passeportDateDelivrance;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate passeportDateExpiration;

    private String visaReference;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visaDateDelivrance;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visaDateExpiration;

    private Integer typeVisaId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEntree;
    private String lieuEntree;

    // HTML: name="pieceIds" (checkbox multiples)
    private Set<Integer> pieceIds = new HashSet<>();
    // HTML: name="champsDynamiques[<referenceChampTypeVisaId>]"
    private Map<Integer, String> champsDynamiques = new HashMap<>();

    // captures Base64 (remplies par le frontend)
    private String photoBase64;
    private String signatureBase64;
}
