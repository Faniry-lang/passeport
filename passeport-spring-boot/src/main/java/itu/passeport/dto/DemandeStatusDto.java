package itu.passeport.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class DemandeStatusDto {
    private Integer id;
    private String passeportNumero;
    private String demandeurNom;
    private LocalDate dateDemande;
    private List<StatutDetailDto> statuts;

    @Data
    public static class StatutDetailDto {
        private String nomStatut;
        private String dateStatut;
    }
}
