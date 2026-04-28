package itu.passeport.controllers;

import itu.passeport.dto.ChampTypeVisaDto;
import itu.passeport.dto.DemandeForm;
import itu.passeport.dto.PasseportRechercheDto;
import itu.passeport.dto.PieceTypeVisaDto;
import itu.passeport.entities.Demande;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                        piece.getObligatoire()
                ))
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
            RedirectAttributes redirectAttributes
    ) {
        try {
            Demande demande = demandeService.creerDemande(demandeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Demande creee avec succes.");
            redirectAttributes.addFlashAttribute("demandeId", demande.getId());
            return "redirect:/demandes/confirmation";
        } catch (
                VisaExpireException
                | PiecesObligatoiresManquantesException
                | DonneesIncoherentesException
                | RessourceIntrouvableException e
        ) {
            return redirigerAvecErreur(redirectAttributes, demandeForm, e.getMessage());
        }
    }

    private String redirigerAvecErreur(
            RedirectAttributes redirectAttributes,
            DemandeForm demandeForm,
            String message
    ) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        redirectAttributes.addFlashAttribute("demandeForm", demandeForm);
        return "redirect:/demandes/nouvelle";
    }
}
