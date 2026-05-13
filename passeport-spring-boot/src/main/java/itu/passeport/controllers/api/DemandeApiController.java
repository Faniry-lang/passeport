package itu.passeport.controllers.api;

import itu.passeport.entities.Demande;
import itu.passeport.entities.StatutDemande;
import itu.passeport.repositories.StatutDemandeRepository;
import itu.passeport.services.DemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows React to consume
public class DemandeApiController {

    private final DemandeService demandeService;
    private final StatutDemandeRepository statutDemandeRepository;

    @GetMapping("/by-passeport")
    public ResponseEntity<List<Map<String, Object>>> getDemandesByPasseport(
            @RequestParam("numero") String numero,
            @RequestParam(value = "ordre", defaultValue = "desc") String ordre) {
        List<Demande> demandes = demandeService.getDemandesByPasseport(numero, ordre);
        return ResponseEntity.ok(demandes.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/by-demande")
    public ResponseEntity<List<Map<String, Object>>> getDemandesByDemande(
            @RequestParam("id") Integer id,
            @RequestParam(value = "ordre", defaultValue = "desc") String ordre) {
        List<Demande> demandes = demandeService.getDemandesByDemandeId(id, ordre);
        return ResponseEntity.ok(demandes.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/medias")
    public ResponseEntity<?> enregistrerMedias(
            @org.springframework.web.bind.annotation.PathVariable("id") Integer id,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("signature") MultipartFile signature) {
        try {
            demandeService.enregistrerMedias(id, photo, signature);
            return ResponseEntity.ok().body(Map.of("message", "Médias enregistrés avec succès."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> mapToDto(Demande demande) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", demande.getId());
        map.put("dateDemande", demande.getDateDemande());

        if (demande.getPasseport() != null) {
            map.put("passeportNumero", demande.getPasseport().getNumero());
        }

        StatutDemande dernierStatut = statutDemandeRepository.findFirstByDemandeIdOrderByDateStatutDesc(demande.getId())
                .orElse(null);
        if (dernierStatut != null && dernierStatut.getReferenceStatutDemande() != null) {
            map.put("statutActuel", dernierStatut.getReferenceStatutDemande().getNom());
            map.put("dateStatut", dernierStatut.getDateStatut());
        }

        if (demande.getTypeVisa() != null) {
            map.put("typeVisa", demande.getTypeVisa().getNom());
        }

        return map;
    }
}
