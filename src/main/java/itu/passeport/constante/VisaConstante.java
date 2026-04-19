package itu.passeport.constante;

import java.util.Set;

public final class VisaConstante {
    public static final String TYPE_INVESTISSEUR = "INVESTISSEUR";
    public static final String TYPE_TRAVAILLEUR = "TRAVAILLEUR";

    private static final Set<String> TYPES_SUPPORTES = Set.of(TYPE_INVESTISSEUR, TYPE_TRAVAILLEUR);

    private VisaConstante() {
    }

    public static boolean estTypeSupporte(String codeTypeVisa) {
        return codeTypeVisa != null && TYPES_SUPPORTES.contains(codeTypeVisa.toUpperCase());
    }
}
