package itu.passeport.constante;

/**
 * Constantes utilisées pour construire l'URL côté React (IP, port, endpoint).
 *
 * NOTE: ces valeurs peuvent être modifiées selon l'environnement réseau.
 */
public final class ReactConstants {

    private ReactConstants() {
        // utilitaire
    }

    public static final String IP_ADRESS = "192.168.1.20";
    public static final Integer PORT = 5173;
    public static final String ENDPOINT = "/demandes/details";
}

