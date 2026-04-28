package itu.passeport.controllers.api;

import itu.passeport.entities.*;
import itu.passeport.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CarteResidentApiController {

    private final CarteResidentRepository carteResidentRepository;
    private final DemandeurRepository demandeurRepository;
    private final PasseportRepository passeportRepository;
    private final DemandeRepository demandeRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final VisaTransformableRepository visaTransformableRepository;

    public CarteResidentApiController(
            CarteResidentRepository carteResidentRepository,
            DemandeurRepository demandeurRepository,
            PasseportRepository passeportRepository,
            DemandeRepository demandeRepository,
            SituationFamilialeRepository situationFamilialeRepository,
            NationaliteRepository nationaliteRepository,
            TypeVisaRepository typeVisaRepository,
            VisaTransformableRepository visaTransformableRepository) {
        this.carteResidentRepository = carteResidentRepository;
        this.demandeurRepository = demandeurRepository;
        this.passeportRepository = passeportRepository;
        this.demandeRepository = demandeRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.visaTransformableRepository = visaTransformableRepository;
    }

    @PostMapping("/cartes-resident/create")
    public ResponseEntity<?> createCarteResident(@RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            String dateDebutStr = (String) request.get("dateDebut");
            String dateFinStr = (String) request.get("dateFin");
            String passeportNumero = (String) request.get("passeportNumero");

            @SuppressWarnings("unchecked")
            Map<String, Object> demandeurData = (Map<String, Object>) request.get("demandeur");

            // Validate required fields
            if (dateDebutStr == null || dateFinStr == null || passeportNumero == null || demandeurData == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Données manquantes pour la création de la carte résident"));
            }

            // Parse dates
            LocalDate dateDebut = LocalDate.parse(dateDebutStr);
            LocalDate dateFin = LocalDate.parse(dateFinStr);

            // Validate date logic
            if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La date de fin doit être postérieure à la date de début"));
            }

            // Find or create demandeur
            String nom = (String) demandeurData.get("nom");
            String prenom = (String) demandeurData.get("prenom");
            String nomJeuneFille = (String) demandeurData.get("nomJeuneFille");
            String dtnStr = (String) demandeurData.get("dtn");
            String adresse = (String) demandeurData.get("adresse");
            String email = (String) demandeurData.get("email");
            String telephone = (String) demandeurData.get("telephone");

            Integer situationFamilialeId = (Integer) demandeurData.get("situationFamilialeId");
            Integer nationaliteId = (Integer) demandeurData.get("nationaliteId");

            if (nom == null || prenom == null || dtnStr == null ||
                    situationFamilialeId == null || nationaliteId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Informations du demandeur incomplètes"));
            }

            LocalDate dtn = LocalDate.parse(dtnStr);

            Optional<SituationFamiliale> situationOpt = situationFamilialeRepository.findById(situationFamilialeId);
            Optional<Nationalite> nationaliteOpt = nationaliteRepository.findById(nationaliteId);

            if (situationOpt.isEmpty() || nationaliteOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Situation familiale ou nationalité invalide"));
            }

            // Find existing demandeur or create new one
            Demandeur demandeur;
            Optional<Demandeur> existingDemandeur = demandeurRepository
                    .findByNomAndPrenomAndDtn(nom, prenom, dtn);

            if (existingDemandeur.isPresent()) {
                demandeur = existingDemandeur.get();
                // Update existing demandeur info
                demandeur.setNomJeuneFille(nomJeuneFille);
                demandeur.setSituationFamiliale(situationOpt.get());
                demandeur.setNationalite(nationaliteOpt.get());
                demandeur.setAdresse(adresse);
                demandeur.setEmail(email);
                demandeur.setTelephone(telephone);
                demandeur = demandeurRepository.save(demandeur);
            } else {
                // Create new demandeur
                demandeur = new Demandeur();
                demandeur.setNom(nom);
                demandeur.setPrenom(prenom);
                demandeur.setNomJeuneFille(nomJeuneFille);
                demandeur.setDtn(dtn);
                demandeur.setSituationFamiliale(situationOpt.get());
                demandeur.setNationalite(nationaliteOpt.get());
                demandeur.setAdresse(adresse);
                demandeur.setEmail(email);
                demandeur.setTelephone(telephone);
                demandeur = demandeurRepository.save(demandeur);
            }

            // Find or create passport
            Optional<Passeport> passeportOpt = passeportRepository.findByNumero(passeportNumero);
            Passeport passeport;

            if (passeportOpt.isPresent()) {
                passeport = passeportOpt.get();
                // Assurer que le passeport est lié au demandeur
                if (passeport.getDemandeur() == null || !passeport.getDemandeur().getId().equals(demandeur.getId())) {
                    passeport.setDemandeur(demandeur);
                    passeport = passeportRepository.save(passeport);
                }
            } else {
                // Create new passport if not found
                passeport = new Passeport();
                passeport.setNumero(passeportNumero);
                passeport.setDateDelivrance(LocalDate.now());
                passeport.setDateExpiration(LocalDate.now().plusYears(10)); // Default 10 years
                passeport.setDemandeur(demandeur); // Associate with demandeur
                passeport = passeportRepository.save(passeport);
            }

            // Create a dummy demande for the carte resident (required by DB schema)
            Demande demande = new Demande();
            demande.setDemandeur(demandeur);
            demande.setPasseport(passeport);

            // Get default type visa and visa transformable (you may need to adjust this)
            Optional<TypeVisa> typeVisaOpt = typeVisaRepository.findAll().stream().findFirst();
            Optional<VisaTransformable> visaTransformableOpt = visaTransformableRepository.findAll().stream()
                    .findFirst();

            if (typeVisaOpt.isEmpty() || visaTransformableOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Configuration système incomplète (TypeVisa ou VisaTransformable manquant)"));
            }

            demande.setTypeVisa(typeVisaOpt.get());
            demande.setVisaTransformable(visaTransformableOpt.get());
            demande.setDateDemande(LocalDate.now());
            demande = demandeRepository.save(demande);

            // Create carte resident
            CarteResident carteResident = new CarteResident();
            carteResident.setDemande(demande);
            carteResident.setPasseport(passeport);
            carteResident.setDateDebut(dateDebut);
            carteResident.setDateFin(dateFin);

            carteResident = carteResidentRepository.save(carteResident);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Carte résident créée avec succès");

            Map<String, Object> data = new HashMap<>();
            data.put("id", carteResident.getId());
            data.put("dateDebut", carteResident.getDateDebut());
            data.put("dateFin", carteResident.getDateFin());

            Map<String, Object> demandeurResponse = new HashMap<>();
            demandeurResponse.put("id", demandeur.getId());
            demandeurResponse.put("nom", demandeur.getNom());
            demandeurResponse.put("prenom", demandeur.getPrenom());
            demandeurResponse.put("nomJeuneFille", demandeur.getNomJeuneFille());
            demandeurResponse.put("dtn", demandeur.getDtn());
            demandeurResponse.put("adresse", demandeur.getAdresse());
            demandeurResponse.put("email", demandeur.getEmail());
            demandeurResponse.put("telephone", demandeur.getTelephone());
            demandeurResponse.put("situationFamilialeId", demandeur.getSituationFamiliale().getId());
            demandeurResponse.put("nationaliteId", demandeur.getNationalite().getId());

            data.put("demandeur", demandeurResponse);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création de la carte résident: " + e.getMessage()));
        }
    }
}
