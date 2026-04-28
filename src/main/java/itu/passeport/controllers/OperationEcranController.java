package itu.passeport.controllers;

import itu.passeport.repositories.NationaliteRepository;
import itu.passeport.repositories.SituationFamilialeRepository;
import itu.passeport.repositories.TypeVisaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class OperationEcranController {

    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;

    @GetMapping("/duplicata/nouvelle")
    public String afficherEcranDuplicata(Model model) {
        ajouterDonneesReferentielles(model);
        return "demandes/duplicata";
    }

    @GetMapping("/transfert/nouvelle")
    public String afficherEcranTransfert(Model model) {
        ajouterDonneesReferentielles(model);
        return "demandes/transfert";
    }

    private void ajouterDonneesReferentielles(Model model) {
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
    }
}
