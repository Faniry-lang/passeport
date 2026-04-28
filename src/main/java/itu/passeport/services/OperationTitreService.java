package itu.passeport.services;

import itu.passeport.dto.DuplicataForm;
import itu.passeport.dto.OperationResult;
import itu.passeport.dto.TransfertVisaForm;

public interface OperationTitreService {

    OperationResult prechargerDuplicata(Integer idDemande);

    OperationResult prechargerTransfert(Integer idDemande);

    OperationResult rechercherDuplicata(String passeportNumero);

    OperationResult rechercherTransfert(String visaReference);

    OperationResult traiterDuplicata(DuplicataForm form);

    OperationResult traiterTransfertVisa(TransfertVisaForm form);
}
