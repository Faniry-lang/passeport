package itu.passeport.controllers;

import itu.passeport.dto.DemandeForm;
import itu.passeport.entities.Demande;
import itu.passeport.exceptions.DonneesIncoherentesException;
import itu.passeport.exceptions.PiecesObligatoiresManquantesException;
import itu.passeport.exceptions.RessourceIntrouvableException;
import itu.passeport.exceptions.VisaExpireException;
import itu.passeport.services.DemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

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
