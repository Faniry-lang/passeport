package itu.passeport.controllers;

import itu.passeport.dto.ChampTypeVisaDto;
import itu.passeport.dto.DemandeForm;
import itu.passeport.dto.PasseportRechercheDto;
import itu.passeport.dto.PieceTypeVisaDto;
import itu.passeport.dto.PieceUploadItemDto;
import itu.passeport.constante.ReactConstants;
import itu.passeport.entities.Demande;
import itu.passeport.entities.Demandeur;
import itu.passeport.entities.ReferenceChampTypeVisa;
import itu.passeport.entities.TypeVisa;
import itu.passeport.exceptions.DonneesIncoherentesException;
import itu.passeport.exceptions.PiecesObligatoiresManquantesException;
import itu.passeport.exceptions.RessourceIntrouvableException;
import itu.passeport.exceptions.VisaExpireException;
import itu.passeport.repositories.NationaliteRepository;
import itu.passeport.repositories.PieceObligatoireTypeVisaRepository;
import itu.passeport.repositories.ReferenceChampTypeVisaRepository;
import itu.passeport.repositories.SituationFamilialeRepository;
import itu.passeport.repositories.TypeVisaRepository;
import itu.passeport.services.DemandeService;
import itu.passeport.utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;
    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final PieceObligatoireTypeVisaRepository pieceObligatoireTypeVisaRepository;
    private final ReferenceChampTypeVisaRepository referenceChampTypeVisaRepository;

    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        model.addAttribute("demandeForm", new DemandeForm());
        return "demande/nouvelle-demande";
    }

    @GetMapping("/confirmation")
    public String afficherConfirmation() {
        return "demandes/confirmation";
    }

    @GetMapping("")
    public String listerDemandes(Model model) {
        List<Demande> demandes = demandeService.getAllDemandes();
        List<java.util.Map<String, Object>> rows = demandes.stream().map(d -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", d.getId());
            Demandeur dem = d.getDemandeur();
            m.put("demandeur", dem == null ? "-" : dem.getNom() + " " + dem.getPrenom());
            m.put("date", d.getDateDemande());
            m.put("statut", demandeService.getCurrentStatutName(d.getId()));
            return m;
        }).toList();
        model.addAttribute("demandes", rows);
        return "demandes/list";
    }

    @GetMapping("/{id}")
    public String detailDemande(@PathVariable Integer id, Model model) {
        Demande demande = demandeService.getDemandeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable."));
        String statut = demandeService.getCurrentStatutName(id);
        model.addAttribute("demande", demande);
        model.addAttribute("statut", statut);
        model.addAttribute("reactIp", ReactConstants.IP_ADRESS);
        model.addAttribute("reactPort", ReactConstants.PORT);
        model.addAttribute("reactEndpoint", ReactConstants.ENDPOINT);
        return "demandes/detail";
    }

    @GetMapping("/{id}/qrcode")
    @ResponseBody
    public ResponseEntity<Resource> genererQrCode(@PathVariable Integer id) {
        demandeService.getDemandeById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Demande introuvable."));

        String link = String.format("http://%s:%d%s?id=%d",
                ReactConstants.IP_ADRESS,
                ReactConstants.PORT,
                ReactConstants.ENDPOINT,
                id);

        try {
            Path qrCodePath = QRCodeGenerator.generateQRCode(
                    link,
                    "storage/qrcode",
                    String.format("DMD-%d-QR-CODE.png", id));
            Resource resource = new UrlResource(qrCodePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                    .body(resource);
        } catch (IOException | WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/scan-termine")
    public String marquerScanTermine(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            demandeService.marquerScanTermine(id);
            redirectAttributes.addFlashAttribute("successMessage", "Scan marque comme termine.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/demandes/" + id;
    }

    @GetMapping("/recherche-passeport")
    @ResponseBody
    public ResponseEntity<PasseportRechercheDto> rechercherPasseport(@RequestParam("numero") String numero) {
        if (numero == null || numero.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return demandeService.rechercherPasseportParNumero(numero.trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/type-visa/{typeVisaId}/pieces")
    @ResponseBody
    public ResponseEntity<List<PieceTypeVisaDto>> rechercherPiecesParTypeVisa(@PathVariable Integer typeVisaId) {
        if (!typeVisaRepository.existsById(typeVisaId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<PieceTypeVisaDto> pieces = pieceObligatoireTypeVisaRepository.findByTypeVisaId(typeVisaId)
                .stream()
                .map(piece -> new PieceTypeVisaDto(
                        piece.getReferencePiece().getId(),
                        piece.getReferencePiece().getNom(),
                        piece.getObligatoire()))
                .toList();

        return ResponseEntity.ok(pieces);
    }

    @GetMapping("/type-visa/{typeVisaId}/champs")
    @ResponseBody
    public ResponseEntity<List<ChampTypeVisaDto>> rechercherChampsParTypeVisa(@PathVariable Integer typeVisaId) {
        TypeVisa typeVisa = typeVisaRepository.findById(typeVisaId).orElse(null);
        if (typeVisa == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ReferenceChampTypeVisa> references = referenceChampTypeVisaRepository.findByTypeVisaId(typeVisa.getId());
        List<ChampTypeVisaDto> champs = references.stream()
                .map(champ -> new ChampTypeVisaDto(champ.getId(), champ.getNom(), champ.getTypeChamp()))
                .toList();

        return ResponseEntity.ok(champs);
    }

    @PostMapping
    public String soumettreFormulaire(
            @ModelAttribute("demandeForm") DemandeForm demandeForm,
            RedirectAttributes redirectAttributes) {
        try {
            Demande demande = demandeService.creerDemande(demandeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Demande creee avec succes.");
            redirectAttributes.addFlashAttribute("demandeId", demande.getId());
            return "redirect:/demandes/confirmation";
        } catch (
                VisaExpireException
                | PiecesObligatoiresManquantesException
                | DonneesIncoherentesException
                | RessourceIntrouvableException e) {
            return redirigerAvecErreur(redirectAttributes, demandeForm, e.getMessage());
        }
    }

    private String redirigerAvecErreur(
            RedirectAttributes redirectAttributes,
            DemandeForm demandeForm,
            String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        redirectAttributes.addFlashAttribute("demandeForm", demandeForm);
        return "redirect:/demandes/nouvelle";
    }

    @GetMapping("/{id}/pieces/upload")
    public String afficherPageUpload(@PathVariable Integer id, Model model) {
        boolean scanTermine = demandeService.estScanTermine(id);
        List<PieceUploadItemDto> pieces = demandeService.listerPiecesPourUpload(id);
        model.addAttribute("demandeId", id);
        model.addAttribute("pieces", pieces);
        model.addAttribute("scanTermine", scanTermine);
        return "demande/upload-pieces";
    }

    @PostMapping("/{id}/pieces/upload")
    public String validerUpload(
            @PathVariable Integer id,
            @RequestParam("pieceIds") Integer[] pieceIds,
            @RequestParam("fichiers") MultipartFile[] fichiers,
            RedirectAttributes redirectAttributes) {

        if (demandeService.estScanTermine(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "Le scan est terminé, impossible d'ajouter de nouveaux fichiers.");
            return "redirect:/demandes/" + id + "/pieces/upload";
        }

        try {
            for (int i = 0; i < pieceIds.length; i++) {
                MultipartFile fichier = fichiers[i];
                if (!fichier.isEmpty()) {
                    demandeService.enregistrerPieceUpload(id, pieceIds[i], fichier);
                }
            }
            redirectAttributes.addFlashAttribute("success", "Fichiers enregistrés avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        return "redirect:/demandes/" + id + "/pieces/upload";
    }

    @GetMapping("/pieces/{pieceDemandeId}/download")
    public ResponseEntity<Resource> telechargerPiece(@PathVariable Integer pieceDemandeId) {
        try {
            Resource resource = demandeService.telechargerPiece(pieceDemandeId);
            String contentType = "application/octet-stream"; // A affiner selon l'extension si nécessaire

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

//    @PostMapping("/{id}/scan-termine")
//    public String marquerScanTermine(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
//        try {
//            demandeService.marquerScanTermine(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Scan de la demande marqué comme terminé.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
//        }
//        return "redirect:/demandes/liste"; // As pointed out, not handled by me.
//    }
}
