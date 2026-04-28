package itu.passeport.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demandes")
public class OperationEcranController {

    @GetMapping("/duplicata/nouvelle")
    public String afficherEcranDuplicata() {
        return "demandes/duplicata";
    }

    @GetMapping("/transfert/nouvelle")
    public String afficherEcranTransfert() {
        return "demandes/transfert";
    }
}
