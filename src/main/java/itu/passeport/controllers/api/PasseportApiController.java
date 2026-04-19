package itu.passeport.controllers.api;

import itu.passeport.entities.Passeport;
import itu.passeport.repositories.PasseportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/passeports")
public class PasseportApiController {

    private final PasseportRepository passeportRepository;

    public PasseportApiController(PasseportRepository passeportRepository) {
        this.passeportRepository = passeportRepository;
    }

    @GetMapping("/{numero}")
    public ResponseEntity<?> getPasseportDetails(@PathVariable String numero) {
        Optional<Passeport> passeportOpt = passeportRepository.findByNumero(numero);

        if (passeportOpt.isPresent()) {
            Passeport p = passeportOpt.get();
            Map<String, Object> data = new HashMap<>();

            // Infos Passeport
            data.put("numero", p.getNumero());
            data.put("dateDelivrance", p.getDateDelivrance());
            data.put("dateExpiration", p.getDateExpiration());

            // Infos Demandeur associées
            if (p.getDemandeur() != null) {
                Map<String, Object> demandeurMap = new HashMap<>();
                demandeurMap.put("id", p.getDemandeur().getId());
                demandeurMap.put("nom", p.getDemandeur().getNom());
                demandeurMap.put("prenom", p.getDemandeur().getPrenom());
                demandeurMap.put("nomJeuneFille", p.getDemandeur().getNomJeuneFille());
                demandeurMap.put("dateNaissance", p.getDemandeur().getDtn());
                demandeurMap.put("adresse", p.getDemandeur().getAdresse());
                demandeurMap.put("email", p.getDemandeur().getEmail());
                demandeurMap.put("telephone", p.getDemandeur().getTelephone());

                if (p.getDemandeur().getSituationFamiliale() != null) {
                    demandeurMap.put("situationFamilialeId", p.getDemandeur().getSituationFamiliale().getId());
                }
                if (p.getDemandeur().getNationalite() != null) {
                    demandeurMap.put("nationaliteId", p.getDemandeur().getNationalite().getId());
                }

                data.put("demandeur", demandeurMap);
            }

            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
