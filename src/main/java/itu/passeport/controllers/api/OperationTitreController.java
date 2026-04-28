package itu.passeport.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import itu.passeport.dto.DuplicataForm;
import itu.passeport.dto.OperationResult;
import itu.passeport.dto.TransfertVisaForm;
import itu.passeport.exceptions.BusinessException;
import itu.passeport.services.OperationTitreService;

@RestController
@RequestMapping("/demandes")
public class OperationTitreController {

    private final OperationTitreService operationTitreService;

    public OperationTitreController(OperationTitreService operationTitreService) {
        this.operationTitreService = operationTitreService;
    }

    @GetMapping("/duplicata/nouvelle")
    public ResponseEntity<?> nouvelleDuplicata(@RequestParam(value = "idDemande", required = false) Integer idDemande) {
        try {
            if (idDemande == null) {
                return ResponseEntity.ok().body(new ApiResponse(true, "Ecran duplicata pret.", null));
            }
            OperationResult r = operationTitreService.prechargerDuplicata(idDemande);
            return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        }
    }

    @GetMapping("/duplicata/recherche")
    public ResponseEntity<?> rechercherDuplicata(@RequestParam("passeportNumero") String passeportNumero) {
        try {
            OperationResult r = operationTitreService.rechercherDuplicata(passeportNumero);
            return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        }
    }

    @PostMapping("/duplicata")
    public ResponseEntity<?> postDuplicata(@RequestBody DuplicataForm form) {
        try {
            OperationResult r = operationTitreService.traiterDuplicata(form);
            return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        } catch (UnsupportedOperationException ue) {
            return ResponseEntity.status(501).body(new ApiResponse(false, ue.getMessage(), null));
        }
    }

    @GetMapping("/transfert/nouvelle")
    public ResponseEntity<?> nouvelleTransfert(
            @RequestParam(value = "idDemande", required = false) Integer idDemande,
            @RequestParam(value = "visaReference", required = false) String visaReference
    ) {
        try {
            if (idDemande != null) {
                OperationResult r = operationTitreService.prechargerTransfert(idDemande);
                return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
            }

            if (visaReference != null && !visaReference.isBlank()) {
                OperationResult r = operationTitreService.rechercherTransfert(visaReference);
                return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
            }

            return ResponseEntity.ok().body(new ApiResponse(true, "Ecran transfert pret.", null));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        }
    }

    @GetMapping("/transfert/recherche")
    public ResponseEntity<?> rechercherTransfert(@RequestParam("visaReference") String visaReference) {
        try {
            OperationResult r = operationTitreService.rechercherTransfert(visaReference);
            return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        }
    }

    @PostMapping("/transfert")
    public ResponseEntity<?> postTransfert(@RequestBody TransfertVisaForm form) {
        try {
            OperationResult r = operationTitreService.traiterTransfertVisa(form);
            return ResponseEntity.ok(new ApiResponse(true, r.getMessage(), r.getData()));
        } catch (BusinessException be) {
            return ResponseEntity.status(be.getStatus()).body(new ApiResponse(false, be.getMessage(), null));
        } catch (UnsupportedOperationException ue) {
            return ResponseEntity.status(501).body(new ApiResponse(false, ue.getMessage(), null));
        }
    }

    public static class ApiResponse {
        public boolean success;
        public String message;
        public Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }
}
