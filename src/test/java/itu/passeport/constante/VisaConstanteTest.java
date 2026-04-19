package itu.passeport.constante;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisaConstanteTest {

    @Test
    void estTypeSupporte_retourneTruePourTypesSupportes() {
        assertTrue(VisaConstante.estTypeSupporte("INVESTISSEUR"));
        assertTrue(VisaConstante.estTypeSupporte("travailleur"));
    }

    @Test
    void estTypeSupporte_retourneFalsePourTypeInvalide() {
        assertFalse(VisaConstante.estTypeSupporte("ETUDIANT"));
        assertFalse(VisaConstante.estTypeSupporte(null));
    }
}

