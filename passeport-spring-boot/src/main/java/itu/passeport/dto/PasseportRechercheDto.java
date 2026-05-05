
package itu.passeport.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class PasseportRechercheDto {
    Integer passeportId;
    String passeportNumero;
    LocalDate passeportDateDelivrance;
    LocalDate passeportDateExpiration;

    Integer demandeurId;
    String nom;
    String prenom;
    String nomJeuneFille;
    LocalDate dtn;
    Integer situationFamilialeId;
    Integer nationaliteId;
    String adresse;
    String email;
    String telephone;
}

