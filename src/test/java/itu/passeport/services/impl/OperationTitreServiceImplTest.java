package itu.passeport.services.impl;

import itu.passeport.dto.DuplicataForm;
import itu.passeport.dto.OperationResult;
import itu.passeport.dto.PasseportDto;
import itu.passeport.dto.TransfertVisaForm;
import itu.passeport.entities.CarteResident;
import itu.passeport.entities.Demande;
import itu.passeport.entities.Demandeur;
import itu.passeport.entities.Passeport;
import itu.passeport.entities.ReferenceStatutCarteResident;
import itu.passeport.entities.StatutCarteResident;
import itu.passeport.entities.VisaTransformable;
import itu.passeport.exceptions.ConflictException;
import itu.passeport.repositories.CarteResidentRepository;
import itu.passeport.repositories.DemandeRepository;
import itu.passeport.repositories.PasseportRepository;
import itu.passeport.repositories.ReferenceStatutCarteResidentRepository;
import itu.passeport.repositories.StatutCarteResidentRepository;
import itu.passeport.repositories.VisaTransformableRepository;
import itu.passeport.services.DemandeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationTitreServiceImplTest {

    @Mock
    private DemandeRepository demandeRepository;
    @Mock
    private DemandeService demandeService;
    @Mock
    private CarteResidentRepository carteResidentRepository;
    @Mock
    private StatutCarteResidentRepository statutCarteResidentRepository;
    @Mock
    private ReferenceStatutCarteResidentRepository referenceStatutCarteResidentRepository;
    @Mock
    private VisaTransformableRepository visaTransformableRepository;
    @Mock
    private PasseportRepository passeportRepository;

    @InjectMocks
    private OperationTitreServiceImpl service;

    @Test
    void traiterDuplicata_shouldSucceed_whenEligible() {
        Demande demande = demandeAvecDemandeur(10, 1);
        Passeport passeport = passeport(20, "MG123", LocalDate.now().minusYears(5), LocalDate.now().plusYears(5), demande.getDemandeur());
        CarteResident source = carteResident(30, demande, passeport);

        DuplicataForm form = new DuplicataForm();
        form.setIdDemande(10);
        form.setCarteResidentId(30);
        form.setMotif("PERTE");

        ReferenceStatutCarteResident refValide = refStatut(2, "VALIDE");
        ReferenceStatutCarteResident refExpire = refStatut(3, "EXPIRE");
        StatutCarteResident statutActuel = statut(source, refValide);

        when(demandeRepository.findById(10)).thenReturn(Optional.of(demande));
        when(carteResidentRepository.findById(30)).thenReturn(Optional.of(source));
        when(statutCarteResidentRepository.findFirstByCarteResidentIdOrderByDateStatutDesc(30)).thenReturn(Optional.of(statutActuel));
        when(referenceStatutCarteResidentRepository.findByNomIgnoreCase("VALIDE")).thenReturn(Optional.of(refValide));
        when(referenceStatutCarteResidentRepository.findByNomIgnoreCase("EXPIRE")).thenReturn(Optional.of(refExpire));
        when(carteResidentRepository.save(any(CarteResident.class))).thenAnswer(inv -> {
            CarteResident c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(31);
            }
            return c;
        });

        OperationResult result = service.traiterDuplicata(form);

        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().get("demandeId"));
        assertEquals(31, result.getData().get("nouvelleCarteResidentId"));
        verify(statutCarteResidentRepository, atLeastOnce()).save(any(StatutCarteResident.class));
    }

    @Test
    void traiterDuplicata_shouldFail_whenSourceNotValide() {
        Demande demande = demandeAvecDemandeur(10, 1);
        Passeport passeport = passeport(20, "MG123", LocalDate.now().minusYears(5), LocalDate.now().plusYears(5), demande.getDemandeur());
        CarteResident source = carteResident(30, demande, passeport);

        DuplicataForm form = new DuplicataForm();
        form.setIdDemande(10);
        form.setCarteResidentId(30);
        form.setMotif("PERTE");

        ReferenceStatutCarteResident refEnAttente = refStatut(1, "EN_ATTENTE");
        StatutCarteResident statutActuel = statut(source, refEnAttente);

        when(demandeRepository.findById(10)).thenReturn(Optional.of(demande));
        when(carteResidentRepository.findById(30)).thenReturn(Optional.of(source));
        when(statutCarteResidentRepository.findFirstByCarteResidentIdOrderByDateStatutDesc(30)).thenReturn(Optional.of(statutActuel));

        assertThrows(ConflictException.class, () -> service.traiterDuplicata(form));
    }

    @Test
    void traiterTransfert_shouldSucceed_whenEligible() {
        Demande demande = demandeAvecDemandeur(10, 1);
        Demandeur demandeur = demande.getDemandeur();

        Passeport ancienPasseport = passeport(20, "OLD-1", LocalDate.now().minusYears(10), LocalDate.now().minusDays(2), demandeur);
        VisaTransformable visaTransformable = visaTransformable(40, "V-REF", LocalDate.now().plusMonths(6), ancienPasseport);

        PasseportDto nouveau = new PasseportDto();
        nouveau.setNumero("NEW-1");
        nouveau.setDateDelivrance(LocalDate.now().minusDays(1));
        nouveau.setDateExpiration(LocalDate.now().plusYears(10));

        TransfertVisaForm form = new TransfertVisaForm();
        form.setIdDemande(10);
        form.setVisaReference("V-REF");
        form.setNouveauPasseport(nouveau);

        when(demandeRepository.findById(10)).thenReturn(Optional.of(demande));
        when(visaTransformableRepository.findByReference("V-REF")).thenReturn(Optional.of(visaTransformable));
        when(passeportRepository.findByNumero("NEW-1")).thenReturn(Optional.empty());
        when(passeportRepository.save(any(Passeport.class))).thenAnswer(inv -> {
            Passeport p = inv.getArgument(0);
            p.setId(99);
            return p;
        });

        OperationResult result = service.traiterTransfertVisa(form);

        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().get("demandeId"));
        assertEquals(40, result.getData().get("visaTransformableId"));
        assertEquals(99, result.getData().get("nouveauPasseportId"));

        ArgumentCaptor<VisaTransformable> captor = ArgumentCaptor.forClass(VisaTransformable.class);
        verify(visaTransformableRepository).save(captor.capture());
        assertEquals(99, captor.getValue().getPasseport().getId());
    }

    @Test
    void traiterTransfert_shouldFail_whenOldPassportStillValid() {
        Demande demande = demandeAvecDemandeur(10, 1);
        Demandeur demandeur = demande.getDemandeur();

        Passeport ancienPasseport = passeport(20, "OLD-1", LocalDate.now().minusYears(10), LocalDate.now().plusDays(2), demandeur);
        VisaTransformable visaTransformable = visaTransformable(40, "V-REF", LocalDate.now().plusMonths(6), ancienPasseport);

        PasseportDto nouveau = new PasseportDto();
        nouveau.setNumero("NEW-1");
        nouveau.setDateDelivrance(LocalDate.now().minusDays(1));
        nouveau.setDateExpiration(LocalDate.now().plusYears(10));

        TransfertVisaForm form = new TransfertVisaForm();
        form.setIdDemande(10);
        form.setVisaReference("V-REF");
        form.setNouveauPasseport(nouveau);

        when(demandeRepository.findById(10)).thenReturn(Optional.of(demande));
        when(visaTransformableRepository.findByReference("V-REF")).thenReturn(Optional.of(visaTransformable));

        assertThrows(ConflictException.class, () -> service.traiterTransfertVisa(form));
    }

    private Demande demandeAvecDemandeur(Integer demandeId, Integer demandeurId) {
        Demandeur demandeur = new Demandeur();
        demandeur.setId(demandeurId);
        demandeur.setNom("RAKOTO");
        demandeur.setPrenom("Jean");

        Demande demande = new Demande();
        demande.setId(demandeId);
        demande.setDemandeur(demandeur);
        return demande;
    }

    private Passeport passeport(Integer id, String numero, LocalDate dateDelivrance, LocalDate dateExpiration, Demandeur demandeur) {
        Passeport p = new Passeport();
        p.setId(id);
        p.setNumero(numero);
        p.setDateDelivrance(dateDelivrance);
        p.setDateExpiration(dateExpiration);
        p.setDemandeur(demandeur);
        return p;
    }

    private CarteResident carteResident(Integer id, Demande demande, Passeport passeport) {
        CarteResident c = new CarteResident();
        c.setId(id);
        c.setDemande(demande);
        c.setPasseport(passeport);
        return c;
    }

    private ReferenceStatutCarteResident refStatut(Integer id, String nom) {
        ReferenceStatutCarteResident r = new ReferenceStatutCarteResident();
        r.setId(id);
        r.setNom(nom);
        return r;
    }

    private StatutCarteResident statut(CarteResident carte, ReferenceStatutCarteResident reference) {
        StatutCarteResident s = new StatutCarteResident();
        s.setCarteResident(carte);
        s.setReferenceStatutCarteResident(reference);
        s.setDateStatut(Instant.now());
        return s;
    }

    private VisaTransformable visaTransformable(Integer id, String reference, LocalDate expiration, Passeport passeport) {
        VisaTransformable v = new VisaTransformable();
        v.setId(id);
        v.setReference(reference);
        v.setDateExpiration(expiration);
        v.setPasseport(passeport);
        return v;
    }
}
