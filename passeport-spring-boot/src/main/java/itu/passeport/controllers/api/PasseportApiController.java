package itu.passeport.controllers.api;

import itu.passeport.entities.Passeport;
import itu.passeport.entities.PieceObligatoireTypeVisa;
import itu.passeport.entities.ReferenceChampTypeVisa;
import itu.passeport.repositories.PasseportRepository;
import itu.passeport.repositories.PieceObligatoireTypeVisaRepository;
import itu.passeport.repositories.ReferenceChampTypeVisaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PasseportApiController {

    private final PasseportRepository passeportRepository;
    private final PieceObligatoireTypeVisaRepository pieceRepo;
    private final ReferenceChampTypeVisaRepository champRepo;

    public PasseportApiController(PasseportRepository passeportRepository,
            PieceObligatoireTypeVisaRepository pieceRepo,
            ReferenceChampTypeVisaRepository champRepo) {
        this.passeportRepository = passeportRepository;
        this.pieceRepo = pieceRepo;
        this.champRepo = champRepo;
    }

    @GetMapping("/passeports/{numero}")
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

    @GetMapping("/types-visa/{id}/dynamique")
    public ResponseEntity<?> getPiecesAndChamps(@PathVariable Integer id) {
        List<PieceObligatoireTypeVisa> pieces = pieceRepo.findByTypeVisaId(id);
        List<ReferenceChampTypeVisa> champs = champRepo.findByTypeVisaId(id);

        Map<String, Object> data = new HashMap<>();

        List<Map<String, Object>> piecesList = new ArrayList<>();
        for (PieceObligatoireTypeVisa p : pieces) {
            Map<String, Object> pm = new HashMap<>();
            pm.put("id", p.getReferencePiece().getId());
            pm.put("nom", p.getReferencePiece().getNom());
            pm.put("obligatoire", p.getObligatoire());
            piecesList.add(pm);
        }

        List<Map<String, Object>> champsList = new ArrayList<>();
        for (ReferenceChampTypeVisa c : champs) {
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.getId());
            cm.put("nom", c.getNom());
            cm.put("typeChamp", c.getTypeChamp());
            champsList.add(cm);
        }

        data.put("pieces", piecesList);
        data.put("champs", champsList);

        return ResponseEntity.ok(data);
    }
}
