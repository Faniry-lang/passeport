package itu.passeport.services;

import itu.passeport.constante.StatusConstante;
import itu.passeport.constante.VisaConstante;
import itu.passeport.dto.DemandeForm;
import itu.passeport.dto.PasseportRechercheDto;
import itu.passeport.entities.*;
import itu.passeport.dto.PieceUploadItemDto;
import itu.passeport.exceptions.DonneesIncoherentesException;
import itu.passeport.exceptions.PiecesObligatoiresManquantesException;
import itu.passeport.exceptions.RessourceIntrouvableException;
import itu.passeport.exceptions.VisaExpireException;
import itu.passeport.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final PieceDemandeRepository pieceDemandeRepository;
    private final ChampTypeVisaDemandeRepository champTypeVisaDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final VisaRepository visaRepository;
    private final ReferenceStatutDemandeRepository referenceStatutDemandeRepository;

    @Transactional(readOnly = true)
    public Optional<PasseportRechercheDto> rechercherPasseportParNumero(String numero) {
        return passeportRepository.findByNumeroAvecDemandeur(numero).map(this::toRechercheDto);
    }

    public List<Demande> getAllDemandes() {
        return this.demandeRepository.findAll();
    }

    public Optional<Demande> getDemandeById(Integer id) {
        return this.demandeRepository.findById(id);
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
        if (statutInitialId != StatusConstante.DEMANDE_APPROUVE) {
            validerPieces(typeVisa.getId(), piecesSelectionnees);
        }

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
            // Tolere les differences pour le flux actuel; controle metier renforcable plus
            // tard.
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
            TypeVisa typeVisa) {
        Demande demande = new Demande();
        demande.setDemandeur(demandeur);
        demande.setPasseport(passeport);
        demande.setVisaTransformable(visaTransformable);
        demande.setTypeVisa(typeVisa);
        demande.setDateDemande(LocalDate.now());
        return demandeRepository.save(demande);
    }

    @Transactional(readOnly = true)
    public String getCurrentStatutName(Integer demandeId) {
        return statutDemandeRepository.findFirstByDemandeIdOrderByDateStatutDesc(demandeId)
                .map(s -> s.getReferenceStatutDemande().getNom())
                .orElse("CREE");
    }

    private void insererPiecesDemande(Demande demande, Set<Integer> piecesSelectionnees) {
        if (piecesSelectionnees.isEmpty()) {
            return;
        }

        List<ReferencePieceJustificative> references = referencePieceJustificativeRepository
                .findAllById(piecesSelectionnees);
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
            VisaTransformable visaTransformable) {
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

    @Transactional(readOnly = true)
    public List<PieceUploadItemDto> listerPiecesPourUpload(Integer demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Demande introuvable."));

        List<PieceObligatoireTypeVisa> configurations = pieceObligatoireTypeVisaRepository
                .findByTypeVisaId(demande.getTypeVisa().getId());
        List<PieceDemande> piecesExistantes = pieceDemandeRepository.findByDemandeId(demandeId);

        boolean scanTermine = estScanTermine(demandeId);

        return configurations.stream().map(config -> {
            ReferencePieceJustificative refPiece = config.getReferencePiece();
            Optional<PieceDemande> pieceMatch = piecesExistantes.stream()
                    .filter(pd -> pd.getReferencePieceJustificative().getId().equals(refPiece.getId()))
                    .findFirst();

            PieceUploadItemDto dto = new PieceUploadItemDto();
            dto.setPieceId(refPiece.getId());
            dto.setPieceLabel(refPiece.getNom());
            dto.setFichierExistant(pieceMatch.isPresent() && pieceMatch.get().getLienFichier() != null);
            dto.setUploadable(!scanTermine);
            pieceMatch.ifPresent(pd -> dto.setPieceDemandeId(pd.getId()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void enregistrerPieceUpload(Integer demandeId, Integer referencePieceId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new DonneesIncoherentesException("Le fichier est vide.");
        }

        if (estScanTermine(demandeId)) {
            throw new IllegalStateException("Le scan est termine, upload interdit.");
        }

        // valider taille (ex. 5MB)
        if (fichier.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Le fichier depasse la taille maximale de 5MB.");
        }

        // valider extension (jpg, png, pdf)
        String originalFilename = fichier.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nom de fichier invalide.");
        }

        String ext = StringUtils.getFilenameExtension(originalFilename).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png", "pdf").contains(ext)) {
            throw new IllegalArgumentException(
                    "Type de fichier non autorise. Seuls .jpg, .jpeg, .png, .pdf sont acceptes.");
        }

        // nettoyer nom
        String nomNettoye = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String finalName = String.format("%d_%d_%d_%s", demandeId, referencePieceId, System.currentTimeMillis(),
                nomNettoye);

        Path storageDir = Paths.get("storage/piece_justificative").toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDir);
            Path targetLocation = storageDir.resolve(finalName);
            Files.copy(fichier.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible d'enregistrer le fichier. " + ex.getMessage(), ex);
        }

        PieceDemande pieceDemande = pieceDemandeRepository
                .findByDemandeIdAndReferencePieceJustificativeId(demandeId, referencePieceId)
                .orElseGet(() -> {
                    Demande demande = demandeRepository.findById(demandeId)
                            .orElseThrow(() -> new RessourceIntrouvableException("Demande introuvable."));
                    ReferencePieceJustificative refPiece = referencePieceJustificativeRepository
                            .findById(referencePieceId)
                            .orElseThrow(() -> new RessourceIntrouvableException("Piece introuvable."));
                    PieceDemande pd = new PieceDemande();
                    pd.setDemande(demande);
                    pd.setReferencePieceJustificative(refPiece);
                    return pd;
                });

        pieceDemande.setLienFichier(finalName);
        pieceDemande.setDateAjout(Instant.now());
        pieceDemandeRepository.save(pieceDemande);
    }

    @Transactional(readOnly = true)
    public Resource telechargerPiece(Integer pieceDemandeId) {
        PieceDemande pieceDemande = pieceDemandeRepository.findById(pieceDemandeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Piece justificative introuvable."));

        if (pieceDemande.getLienFichier() == null) {
            throw new RessourceIntrouvableException("Aucun fichier associe a cette piece.");
        }

        try {
            Path filePath = Paths.get("storage/piece_justificative").resolve(pieceDemande.getLienFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RessourceIntrouvableException("Le fichier est introuvable sur le disque.");
            }
        } catch (MalformedURLException ex) {
            throw new RessourceIntrouvableException("Erreur lors de la resolution du fichier.", ex);
        }
    }

    @Transactional
    public void marquerScanTermine(Integer demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new RessourceIntrouvableException("Demande introuvable.");
        }
        initialiserStatut(demandeRepository.findById(demandeId).get(),
                referenceStatutDemandeRepository.findByNomIgnoreCase("SCAN_TERMINE")
                        .orElseThrow(
                                () -> new RessourceIntrouvableException("Reference Statut SCAN_TERMINE introuvable."))
                        .getId());
    }

    @Transactional(readOnly = true)
    public boolean estScanTermine(Integer demandeId) {
        return statutDemandeRepository.findFirstByDemandeIdOrderByDateStatutDesc(demandeId)
                .map(statut -> statut.getReferenceStatutDemande().getNom().equalsIgnoreCase("SCAN_TERMINE"))
                .orElse(false);
    }
}
