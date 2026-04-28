package itu.passeport.dto;

public class DuplicataForm {

    private Integer idDemande;

    private DemandeForm demande;

    private Integer carteResidentId;

    private String passeportNumero;

    private String carteResidentNumero;

    private String motif;

    public Integer getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Integer idDemande) {
        this.idDemande = idDemande;
    }

    public DemandeForm getDemande() {
        return demande;
    }

    public void setDemande(DemandeForm demande) {
        this.demande = demande;
    }

    public Integer getCarteResidentId() {
        return carteResidentId;
    }

    public void setCarteResidentId(Integer carteResidentId) {
        this.carteResidentId = carteResidentId;
    }

    public String getPasseportNumero() {
        return passeportNumero;
    }

    public void setPasseportNumero(String passeportNumero) {
        this.passeportNumero = passeportNumero;
    }

    public String getCarteResidentNumero() {
        return carteResidentNumero;
    }

    public void setCarteResidentNumero(String carteResidentNumero) {
        this.carteResidentNumero = carteResidentNumero;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
