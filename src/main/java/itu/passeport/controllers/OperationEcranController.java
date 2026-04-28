package itu.passeport.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

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

    @GetMapping("/duplicata/recherche")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechercherDuplicata(@RequestParam(required = false) Integer idDemande) {
        // TODO backend: brancher la vraie recherche duplicata.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("success", false, "message", "Endpoint backend duplicata non implemente.", "data", Map.of()));
    }

    @GetMapping("/transfert/recherche")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechercherTransfert(@RequestParam(required = false) Integer idDemande) {
        // TODO backend: brancher la vraie recherche transfert.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("success", false, "message", "Endpoint backend transfert non implemente.", "data", Map.of()));
    }

    @PostMapping("/duplicata")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> soumettreDuplicata() {
        // TODO backend: brancher le traitement duplicata.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("success", false, "message", "Endpoint backend duplicata non implemente.", "data", Map.of()));
    }

    @PostMapping("/transfert")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> soumettreTransfert() {
        // TODO backend: brancher le traitement transfert.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("success", false, "message", "Endpoint backend transfert non implemente.", "data", Map.of()));
    }
}

