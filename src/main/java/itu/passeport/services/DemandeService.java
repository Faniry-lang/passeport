package itu.passeport.services;

import itu.passeport.constante.StatusConstante;
import itu.passeport.constante.VisaConstante;
import itu.passeport.dto.DemandeForm;
import itu.passeport.dto.PasseportRechercheDto;
import itu.passeport.entities.*;
import itu.passeport.exceptions.DonneesIncoherentesException;
import itu.passeport.exceptions.PiecesObligatoiresManquantesException;
import itu.passeport.exceptions.RessourceIntrouvableException;
import itu.passeport.exceptions.VisaExpireException;
import itu.passeport.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeService {
    private final DemandeRepository demandeRepository;
    private final DemandeurRepository demandeurRepository;
    private final PasseportRepository passeportRepository;
    private final VisaTransformableRepository visaTransformableRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final PieceObligatoireTypeVisaRepository pieceObligatoireTypeVisaRepository;
    private final ReferencePieceJustificativeRepository referencePieceJustificativeRepository;
    private final ReferenceChampTypeVisaRepository referenceChampTypeVisaRepository;
    private final ReferenceStatutDemandeRepository referenceStatutDemandeRepository;
    private final PieceDemandeRepository pieceDemandeRepository;
    private final ChampTypeVisaDemandeRepository champTypeVisaDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final VisaRepository visaRepository;

    @Transactional(readOnly = true)
    public Optional<PasseportRechercheDto> rechercherPasseportParNumero(String numero) {
        return passeportRepository.findByNumeroAvecDemandeur(numero).map(this::toRechercheDto);
    }

    @Transactional
    public Demande creerDemande(DemandeForm form) {
        return creerDemandeAvecStatut(form, StatusConstante.DEMANDE_CREE);
    }

    @Transactional
    public Demande creerDemandeApprouvee(DemandeForm form) {
        return creerDemandeAvecStatut(form, StatusConstante.DEMANDE_APPROUVE);
    }

    private Demande creerDemandeAvecStatut(DemandeForm form, int statutInitialId) {
        TypeVisa typeVisa = getTypeVisa(form.getTypeVisaId());
        validerTypeVisaSupporte(typeVisa);

        Passeport passeport = chargerOuCreerPasseport(form);
        Demandeur demandeur = passeport.getDemandeur();

        VisaTransformable visaTransformable = chargerOuCreerVisaTransformable(form, passeport);
        verifierVisaNonExpire(visaTransformable);

        Set<Integer> piecesSelectionnees = normaliserPieces(form.getPieceIds());
        validerPieces(typeVisa.getId(), piecesSelectionnees);

        Demande demande = creerEtSauvegarderDemande(demandeur, passeport, visaTransformable, typeVisa);

        insererPiecesDemande(demande, piecesSelectionnees);
        insererChampsDynamiques(demande, typeVisa.getId(), form.getChampsDynamiques());
        initialiserStatut(demande, statutInitialId);
        return demande;
    }

    private PasseportRechercheDto toRechercheDto(Passeport passeport) {
        Demandeur demandeur = passeport.getDemandeur();
        return PasseportRechercheDto.builder()
                .passeportId(passeport.getId())
                .passeportNumero(passeport.getNumero())
                .passeportDateDelivrance(passeport.getDateDelivrance())
                .passeportDateExpiration(passeport.getDateExpiration())
                .demandeurId(demandeur.getId())
                .nom(demandeur.getNom())
                .prenom(demandeur.getPrenom())
                .nomJeuneFille(demandeur.getNomJeuneFille())
                .dtn(demandeur.getDtn())
                .situationFamilialeId(demandeur.getSituationFamiliale().getId())
                .nationaliteId(demandeur.getNationalite().getId())
                .adresse(demandeur.getAdresse())
                .email(demandeur.getEmail())
                .telephone(demandeur.getTelephone())
                .build();
    }

    private void validerTypeVisaSupporte(TypeVisa typeVisa) {
        if (!VisaConstante.estTypeSupporte(typeVisa.getCode())) {
            throw new DonneesIncoherentesException("Type de visa non supporte: " + typeVisa.getCode());
        }
    }

    private TypeVisa getTypeVisa(Integer typeVisaId) {
        return typeVisaRepository.findById(typeVisaId)
                .orElseThrow(() -> new RessourceIntrouvableException("Type de visa introuvable."));
    }

    private Passeport chargerOuCreerPasseport(DemandeForm form) {
        return passeportRepository.findByNumeroAvecDemandeur(form.getPasseportNumero())
                .map(passeport -> {
                    verifierCoherenceDonneesSiPasseportExistant(form, passeport);
                    return passeport;
                })
                .orElseGet(() -> creerPasseportEtDemandeur(form));
    }

    private Passeport creerPasseportEtDemandeur(DemandeForm form) {
        Demandeur demandeur = new Demandeur();
        demandeur.setNom(form.getNom());
        demandeur.setPrenom(form.getPrenom());
        demandeur.setNomJeuneFille(form.getNomJeuneFille());
        demandeur.setDtn(form.getDtn());
        demandeur.setAdresse(form.getAdresse());
        demandeur.setEmail(form.getEmail());
        demandeur.setTelephone(form.getTelephone());
        demandeur.setSituationFamiliale(situationFamilialeRepository.findById(form.getSituationFamilialeId())
                .orElseThrow(() -> new RessourceIntrouvableException("Situation familiale introuvable.")));
        demandeur.setNationalite(nationaliteRepository.findById(form.getNationaliteId())
                .orElseThrow(() -> new RessourceIntrouvableException("Nationalite introuvable.")));

        Demandeur demandeurSauvegarde = demandeurRepository.save(demandeur);

        Passeport passeport = new Passeport();
        passeport.setNumero(form.getPasseportNumero());
        passeport.setDateDelivrance(form.getPasseportDateDelivrance());
        passeport.setDateExpiration(form.getPasseportDateExpiration());
        passeport.setDemandeur(demandeurSauvegarde);

        return passeportRepository.save(passeport);
    }

    private void verifierCoherenceDonneesSiPasseportExistant(DemandeForm form, Passeport passeport) {
        Demandeur demandeur = passeport.getDemandeur();

        boolean incoherent = false;
        incoherent |= estDifferent(form.getPasseportDateDelivrance(), passeport.getDateDelivrance());
        incoherent |= estDifferent(form.getPasseportDateExpiration(), passeport.getDateExpiration());
        incoherent |= estDifferent(form.getNom(), demandeur.getNom());
        incoherent |= estDifferent(form.getPrenom(), demandeur.getPrenom());
        incoherent |= estDifferent(form.getNomJeuneFille(), demandeur.getNomJeuneFille());
        incoherent |= estDifferent(form.getDtn(), demandeur.getDtn());
        incoherent |= estDifferent(form.getSituationFamilialeId(), demandeur.getSituationFamiliale().getId());
        incoherent |= estDifferent(form.getNationaliteId(), demandeur.getNationalite().getId());
        incoherent |= estDifferent(form.getAdresse(), demandeur.getAdresse());
        incoherent |= estDifferent(form.getEmail(), demandeur.getEmail());
        incoherent |= estDifferent(form.getTelephone(), demandeur.getTelephone());

        if (incoherent) {
            // Tolere les differences pour le flux actuel; controle metier renforcable plus tard.
        }
    }

    private VisaTransformable chargerOuCreerVisaTransformable(DemandeForm form, Passeport passeport) {
        return visaTransformableRepository.findByReference(form.getVisaReference())
                .map(visa -> {
                    if (!Objects.equals(visa.getPasseport().getId(), passeport.getId())) {
                        throw new DonneesIncoherentesException("Le visa transformable est lie a un autre passeport.");
                    }
                    return visa;
                })
                .orElseGet(() -> creerVisaTransformable(form, passeport));
    }

    private VisaTransformable creerVisaTransformable(DemandeForm form, Passeport passeport) {
        VisaTransformable visaTransformable = new VisaTransformable();
        visaTransformable.setReference(form.getVisaReference());
        visaTransformable.setPasseport(passeport);
        visaTransformable.setDateDelivrance(form.getVisaDateDelivrance());
        visaTransformable.setDateExpiration(form.getVisaDateExpiration());
        return visaTransformableRepository.save(visaTransformable);
    }

    private void verifierVisaNonExpire(VisaTransformable visaTransformable) {
        LocalDate expiration = visaTransformable.getDateExpiration();
        if (expiration != null && expiration.isBefore(LocalDate.now())) {
            throw new VisaExpireException("Le visa transformable est expire.");
        }
    }

    private Set<Integer> normaliserPieces(Set<Integer> pieceIds) {
        if (pieceIds == null) {
            return Collections.emptySet();
        }
        return pieceIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private void validerPieces(Integer typeVisaId, Set<Integer> piecesSelectionnees) {
        List<PieceObligatoireTypeVisa> configurations = pieceObligatoireTypeVisaRepository.findByTypeVisaId(typeVisaId);
        Set<Integer> piecesAutorisees = configurations.stream()
                .map(config -> config.getReferencePiece().getId())
                .collect(Collectors.toSet());
        Set<Integer> piecesObligatoires = configurations.stream()
                .filter(PieceObligatoireTypeVisa::getObligatoire)
                .map(config -> config.getReferencePiece().getId())
                .collect(Collectors.toSet());

        if (!piecesAutorisees.containsAll(piecesSelectionnees)) {
            throw new DonneesIncoherentesException("Une piece selectionnee n'est pas autorisee pour ce type de visa.");
        }

        Set<Integer> piecesManquantes = new HashSet<>(piecesObligatoires);
        piecesManquantes.removeAll(piecesSelectionnees);
        if (!piecesManquantes.isEmpty()) {
            throw new PiecesObligatoiresManquantesException("Des pieces obligatoires sont manquantes.");
        }
    }

    private Demande creerEtSauvegarderDemande(
            Demandeur demandeur,
            Passeport passeport,
            VisaTransformable visaTransformable,
            TypeVisa typeVisa
    ) {
        Demande demande = new Demande();
        demande.setDemandeur(demandeur);
        demande.setPasseport(passeport);
        demande.setVisaTransformable(visaTransformable);
        demande.setTypeVisa(typeVisa);
        demande.setDateDemande(LocalDate.now());
        return demandeRepository.save(demande);
    }

    private void insererPiecesDemande(Demande demande, Set<Integer> piecesSelectionnees) {
        if (piecesSelectionnees.isEmpty()) {
            return;
        }

        List<ReferencePieceJustificative> references = referencePieceJustificativeRepository.findAllById(piecesSelectionnees);
        Map<Integer, ReferencePieceJustificative> referenceParId = references.stream()
                .collect(Collectors.toMap(ReferencePieceJustificative::getId, piece -> piece));

        for (Integer pieceId : piecesSelectionnees) {
            ReferencePieceJustificative reference = referenceParId.get(pieceId);
            if (reference == null) {
                throw new RessourceIntrouvableException("Piece justificative introuvable: " + pieceId);
            }

            PieceDemande pieceDemande = new PieceDemande();
            pieceDemande.setDemande(demande);
            pieceDemande.setReferencePieceJustificative(reference);
            pieceDemande.setDateAjout(Instant.now());
            pieceDemandeRepository.save(pieceDemande);
        }
    }

    private void insererChampsDynamiques(Demande demande, Integer typeVisaId, Map<Integer, String> champsDynamiques) {
        List<ReferenceChampTypeVisa> references = referenceChampTypeVisaRepository.findByTypeVisaId(typeVisaId);
        Set<Integer> idsAutorises = references.stream()
                .map(ReferenceChampTypeVisa::getId)
                .collect(Collectors.toSet());
        Map<Integer, ReferenceChampTypeVisa> referencesParId = references.stream()
                .collect(Collectors.toMap(ReferenceChampTypeVisa::getId, reference -> reference));

        if (champsDynamiques == null || champsDynamiques.isEmpty()) {
            return;
        }

        if (!idsAutorises.containsAll(champsDynamiques.keySet())) {
            throw new DonneesIncoherentesException("Un champ dynamique ne correspond pas au type de visa selectionne.");
        }

        for (Map.Entry<Integer, String> entry : champsDynamiques.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }

            ChampTypeVisaDemande champ = new ChampTypeVisaDemande();
            champ.setDemande(demande);
            champ.setReferenceChampTypeVisa(referencesParId.get(entry.getKey()));
            champ.setValeur(entry.getValue().trim());
            champTypeVisaDemandeRepository.save(champ);
        }
    }

    private void initialiserStatut(Demande demande, int statutId) {
        ReferenceStatutDemande referenceStatut = referenceStatutDemandeRepository.findById(statutId)
                .orElseThrow(() -> new RessourceIntrouvableException("Statut initial introuvable."));

        StatutDemande statutDemande = new StatutDemande();
        statutDemande.setDemande(demande);
        statutDemande.setReferenceStatutDemande(referenceStatut);
        statutDemande.setDateStatut(Instant.now());
        statutDemandeRepository.save(statutDemande);
    }

    @SuppressWarnings("unused")
    private void creerVisa(
            Demande demande,
            TypeVisa typeVisa,
            DemandeForm form,
            VisaTransformable visaTransformable
    ) {
        Visa visa = new Visa();
        visa.setDemande(demande);
        visa.setTypeVisa(typeVisa);
        visa.setReference(visaTransformable.getReference());
        visa.setDateEntree(form.getDateEntree());
        visa.setLieuEntree(form.getLieuEntree());
        visa.setDateExpiration(visaTransformable.getDateExpiration());
        visaRepository.save(visa);
    }

    private boolean estDifferent(String valeurFormulaire, String valeurBase) {
        if (valeurFormulaire == null || valeurFormulaire.isBlank()) {
            return false;
        }
        return !valeurFormulaire.trim().equalsIgnoreCase(Objects.toString(valeurBase, "").trim());
    }

    private boolean estDifferent(LocalDate valeurFormulaire, LocalDate valeurBase) {
        return valeurFormulaire != null && !Objects.equals(valeurFormulaire, valeurBase);
    }

    private boolean estDifferent(Integer valeurFormulaire, Integer valeurBase) {
        return valeurFormulaire != null && !Objects.equals(valeurFormulaire, valeurBase);
    }
}
