package itu.passeport.controllers;

import itu.passeport.repositories.NationaliteRepository;
import itu.passeport.repositories.SituationFamilialeRepository;
import itu.passeport.repositories.TypeVisaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;

    public DemandeController(TypeVisaRepository typeVisaRepository,
            SituationFamilialeRepository situationFamilialeRepository,
            NationaliteRepository nationaliteRepository) {
        this.typeVisaRepository = typeVisaRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.nationaliteRepository = nationaliteRepository;
    }

    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());

        return "demande/nouvelle-demande";
    }
}
