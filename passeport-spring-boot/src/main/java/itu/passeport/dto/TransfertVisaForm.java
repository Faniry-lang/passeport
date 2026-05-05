package itu.passeport.dto;

public class TransfertVisaForm {

    private Integer idDemande;

    private DemandeForm demande;

    private String visaReference;

    private PasseportDto nouveauPasseport;

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

    public String getVisaReference() {
        return visaReference;
    }

    public void setVisaReference(String visaReference) {
        this.visaReference = visaReference;
    }

    public PasseportDto getNouveauPasseport() {
        return nouveauPasseport;
    }

    public void setNouveauPasseport(PasseportDto nouveauPasseport) {
        this.nouveauPasseport = nouveauPasseport;
    }
}
