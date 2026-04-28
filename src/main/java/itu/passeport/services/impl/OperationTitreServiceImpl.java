package itu.passeport.services.impl;

import itu.passeport.dto.DemandeForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import itu.passeport.exceptions.BusinessException;
import itu.passeport.exceptions.ConflictException;
import itu.passeport.exceptions.ResourceNotFoundException;
import itu.passeport.repositories.CarteResidentRepository;
import itu.passeport.repositories.DemandeRepository;
import itu.passeport.repositories.PasseportRepository;
import itu.passeport.repositories.ReferenceStatutCarteResidentRepository;
import itu.passeport.repositories.StatutCarteResidentRepository;
import itu.passeport.repositories.VisaTransformableRepository;
import itu.passeport.services.DemandeService;
import itu.passeport.services.OperationTitreService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OperationTitreServiceImpl implements OperationTitreService {

    private static final String STATUT_CARTE_VALIDE = "VALIDE";
    private static final String STATUT_CARTE_EXPIRE = "EXPIRE";
    private static final Set<String> MOTIFS_DUPLICATA = Set.of("PERTE", "DETERIORATION");

    private final DemandeRepository demandeRepository;
    private final DemandeService demandeService;
    private final CarteResidentRepository carteResidentRepository;
    private final StatutCarteResidentRepository statutCarteResidentRepository;
    private final ReferenceStatutCarteResidentRepository referenceStatutCarteResidentRepository;
    private final VisaTransformableRepository visaTransformableRepository;
    private final PasseportRepository passeportRepository;

    @Override
    @Transactional(readOnly = true)
    public OperationResult prechargerDuplicata(Integer idDemande) {
        if (idDemande == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "idDemande est obligatoire.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));

        Map<String, Object> data = construireDataDemande(demande);
        return new OperationResult(true, "Demande chargee pour duplicata.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationResult prechargerTransfert(Integer idDemande) {
        if (idDemande == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "idDemande est obligatoire.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));

        Map<String, Object> data = construireDataDemande(demande);
        return new OperationResult(true, "Demande chargee pour transfert.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationResult rechercherDuplicata(String passeportNumero) {
        if (estVide(passeportNumero)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Le numero de passeport est obligatoire.");
        }

        CarteResident carteResident = carteResidentRepository
                .findFirstByPasseportNumeroOrderByIdDesc(passeportNumero.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune carte resident trouvee pour ce passeport."));

        StatutCarteResident statut = statutCarteResidentRepository
                .findFirstByCarteResidentIdOrderByDateStatutDesc(carteResident.getId())
                .orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("carteResidentId", carteResident.getId());
        data.put("passeportId", carteResident.getPasseport().getId());
        data.put("passeportNumero", carteResident.getPasseport().getNumero());
        data.put("demandeurId", carteResident.getPasseport().getDemandeur().getId());
        data.put("statutCarte", statut == null ? null : statut.getReferenceStatutCarteResident().getNom());

        return new OperationResult(true, "Source duplicata trouvee.", data);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationResult rechercherTransfert(String visaReference) {
        if (estVide(visaReference)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La reference de visa est obligatoire.");
        }

        VisaTransformable visa = visaTransformableRepository.findByReference(visaReference.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Visa transformable introuvable."));

        Map<String, Object> data = new HashMap<>();
        data.put("visaTransformableId", visa.getId());
        data.put("visaReference", visa.getReference());
        data.put("visaDateExpiration", visa.getDateExpiration());
        data.put("ancienPasseportId", visa.getPasseport().getId());
        data.put("ancienPasseportNumero", visa.getPasseport().getNumero());
        data.put("demandeurId", visa.getPasseport().getDemandeur().getId());

        return new OperationResult(true, "Source transfert trouvee.", data);
    }

    @Override
    @Transactional
    public OperationResult traiterDuplicata(DuplicataForm form) {
        if (form == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Le formulaire duplicata est obligatoire.");
        }

        Demande demande = resoudreOuCreerDemande(form.getIdDemande(), form.getDemande());
        CarteResident carteSource = resoudreCarteSource(form);

        if (!memeDemandeur(demande.getDemandeur(), carteSource.getPasseport().getDemandeur())) {
            throw new ConflictException(
                    "Incoherence: le titulaire de la demande ne correspond pas a la carte resident source.");
        }

        StatutCarteResident statutActuel = statutCarteResidentRepository
                .findFirstByCarteResidentIdOrderByDateStatutDesc(carteSource.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Statut de la carte resident source introuvable."));

        if (!STATUT_CARTE_VALIDE.equalsIgnoreCase(statutActuel.getReferenceStatutCarteResident().getNom())) {
            throw new ConflictException("La carte resident source n'est pas eligible (statut non VALIDE).");
        }

        String motif = normaliserMotif(form.getMotif());
        if (!MOTIFS_DUPLICATA.contains(motif)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Le motif doit etre PERTE ou DETERIORATION.");
        }

        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDemande(demande);
        nouvelleCarte.setPasseport(carteSource.getPasseport());
        nouvelleCarte.setDateDebut(LocalDate.now());
        nouvelleCarte.setDateFin(carteSource.getDateFin());
        nouvelleCarte = carteResidentRepository.save(nouvelleCarte);

        ReferenceStatutCarteResident refValide = referenceStatutCarteResidentRepository
                .findByNomIgnoreCase(STATUT_CARTE_VALIDE)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reference statut carte resident VALIDE introuvable."));
        ReferenceStatutCarteResident refExpire = referenceStatutCarteResidentRepository
                .findByNomIgnoreCase(STATUT_CARTE_EXPIRE)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reference statut carte resident EXPIRE introuvable."));

        StatutCarteResident statutNouvelle = new StatutCarteResident();
        statutNouvelle.setCarteResident(nouvelleCarte);
        statutNouvelle.setReferenceStatutCarteResident(refValide);
        statutNouvelle.setDateStatut(Instant.now());
        statutCarteResidentRepository.save(statutNouvelle);

        StatutCarteResident invalidationAncienne = new StatutCarteResident();
        invalidationAncienne.setCarteResident(carteSource);
        invalidationAncienne.setReferenceStatutCarteResident(refExpire);
        invalidationAncienne.setDateStatut(Instant.now());
        statutCarteResidentRepository.save(invalidationAncienne);

        carteSource.setDateFin(LocalDate.now());
        carteResidentRepository.save(carteSource);

        Map<String, Object> data = new HashMap<>();
        data.put("demandeId", demande.getId());
        data.put("nouvelleCarteResidentId", nouvelleCarte.getId());
        data.put("ancienneCarteResidentId", carteSource.getId());
        data.put("motif", motif);

        return new OperationResult(true, "Duplicata traite avec succes.", data);
    }

    @Override
    @Transactional
    public OperationResult traiterTransfertVisa(TransfertVisaForm form) {
        if (form == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Le formulaire transfert est obligatoire.");
        }

        Demande demande = resoudreOuCreerDemande(form.getIdDemande(), form.getDemande());

        if (estVide(form.getVisaReference())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La reference de visa est obligatoire.");
        }

        VisaTransformable visaTransformable = visaTransformableRepository
                .findByReference(form.getVisaReference().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Visa transformable introuvable."));

        LocalDate maintenant = LocalDate.now();
        if (visaTransformable.getDateExpiration() != null
                && visaTransformable.getDateExpiration().isBefore(maintenant)) {
            throw new ConflictException("Le visa transformable est expire.");
        }

        Passeport ancienPasseport = visaTransformable.getPasseport();
        if (ancienPasseport.getDateExpiration() == null || ancienPasseport.getDateExpiration().isAfter(maintenant)) {
            throw new ConflictException("L'ancien passeport lie au visa doit etre expire.");
        }

        Passeport nouveauPasseport = resoudreOuCreerNouveauPasseport(form.getNouveauPasseport(),
                ancienPasseport.getDemandeur());

        if (nouveauPasseport.getDateExpiration() == null || !nouveauPasseport.getDateExpiration().isAfter(maintenant)) {
            throw new ConflictException("Le nouveau passeport doit etre valide.");
        }

        if (!memeDemandeur(ancienPasseport.getDemandeur(), nouveauPasseport.getDemandeur())) {
            throw new ConflictException("Incoherence: le nouveau passeport n'appartient pas au meme demandeur.");
        }

        if (!memeDemandeur(demande.getDemandeur(), ancienPasseport.getDemandeur())) {
            throw new ConflictException("Incoherence: la demande ne correspond pas au titulaire du visa source.");
        }

        visaTransformable.setPasseport(nouveauPasseport);
        visaTransformableRepository.save(visaTransformable);

        Map<String, Object> data = new HashMap<>();
        data.put("demandeId", demande.getId());
        data.put("visaTransformableId", visaTransformable.getId());
        data.put("ancienPasseportId", ancienPasseport.getId());
        data.put("nouveauPasseportId", nouveauPasseport.getId());

        return new OperationResult(true, "Transfert de visa traite avec succes.", data);
    }

    private Demande resoudreOuCreerDemande(Integer idDemande, DemandeForm formDemande) {
        if (idDemande != null) {
            return demandeRepository.findById(idDemande)
                    .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));
        }

        if (formDemande == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "idDemande ou demande de creation est obligatoire.");
        }

        return demandeService.creerDemandeApprouvee(formDemande);
    }

    private CarteResident resoudreCarteSource(DuplicataForm form) {
        if (form.getCarteResidentId() != null) {
            return carteResidentRepository.findById(form.getCarteResidentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carte resident source introuvable."));
        }

        String passeportNumero = premierNonVide(form.getPasseportNumero(), form.getCarteResidentNumero());
        if (estVide(passeportNumero)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "carteResidentId ou passeportNumero est obligatoire pour le duplicata.");
        }

        return carteResidentRepository.findFirstByPasseportNumeroOrderByIdDesc(passeportNumero.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Carte resident source introuvable."));
    }

    private Passeport resoudreOuCreerNouveauPasseport(PasseportDto dto, Demandeur demandeur) {
        if (dto == null || estVide(dto.getNumero())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Le nouveau passeport est obligatoire.");
        }

        String numero = dto.getNumero().trim();
        Passeport passeportExistant = passeportRepository.findByNumero(numero).orElse(null);
        if (passeportExistant != null) {
            return passeportExistant;
        }

        if (dto.getDateDelivrance() == null || dto.getDateExpiration() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Les dates de delivrance et expiration du nouveau passeport sont obligatoires.");
        }

        Passeport nouveauPasseport = new Passeport();
        nouveauPasseport.setNumero(numero);
        nouveauPasseport.setDateDelivrance(dto.getDateDelivrance());
        nouveauPasseport.setDateExpiration(dto.getDateExpiration());
        nouveauPasseport.setDemandeur(demandeur);
        return passeportRepository.save(nouveauPasseport);
    }

    private boolean memeDemandeur(Demandeur a, Demandeur b) {
        return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
    }

    private String normaliserMotif(String motif) {
        return motif == null ? "" : motif.trim().toUpperCase();
    }

    private String premierNonVide(String a, String b) {
        if (!estVide(a)) {
            return a;
        }
        return b;
    }

    private boolean estVide(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, Object> construireDataDemande(Demande demande) {
        Map<String, Object> data = new HashMap<>();
        data.put("demandeId", demande.getId());
        data.put("dateDemande", demande.getDateDemande());

        if (demande.getDemandeur() != null) {
            Demandeur d = demande.getDemandeur();
            data.put("demandeurId", d.getId());
            data.put("nom", d.getNom());
            data.put("prenom", d.getPrenom());
            data.put("nomJeuneFille", d.getNomJeuneFille());
            data.put("dtn", d.getDtn());
            data.put("adresse", d.getAdresse());
            data.put("email", d.getEmail());
            data.put("telephone", d.getTelephone());
            if (d.getSituationFamiliale() != null) {
                data.put("situationFamilialeId", d.getSituationFamiliale().getId());
            }
            if (d.getNationalite() != null) {
                data.put("nationaliteId", d.getNationalite().getId());
            }
        }

        if (demande.getPasseport() != null) {
            Passeport p = demande.getPasseport();
            data.put("passeportId", p.getId());
            data.put("passeportNumero", p.getNumero());
            data.put("passeportDateDelivrance", p.getDateDelivrance());
            data.put("passeportDateExpiration", p.getDateExpiration());
        }

        if (demande.getVisaTransformable() != null) {
            VisaTransformable vt = demande.getVisaTransformable();
            data.put("visaTransformableId", vt.getId());
            data.put("visaReference", vt.getReference());
            data.put("visaDateDelivrance", vt.getDateDelivrance());
            data.put("visaDateExpiration", vt.getDateExpiration());
        }

        if (demande.getTypeVisa() != null) {
            data.put("typeVisaId", demande.getTypeVisa().getId());
        }

        return data;
    }
}
