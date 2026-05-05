### BRANCHE: sprint/4/qr-code/generator

- QR code
    - ajouter librairie ZXing dans pom.xml (ou une autre alternative)
    - créer classe utilitaire QRCodeGenerator
    - créer une fonction qui recoit comme argument un lien (String) et le chemin du dossier pour stocker l'image du QR code (String)
- Affichage du QR code
    - créer une classe pour stocker la constante IP_ADRESS pour l'adresse IP de l'ordinateur
    - y mettre également l'endpoint de la page React qui va afficher la liste des demandes
        exemple:
            @Getter
            @Setter
            public class ReactConstants {
                private final String IP_ADRESS = "192.168.1.20";
                private final Integer port = 5173;
                private final String ENDPOINT = "/demandes/details";
            }

    - dans la page de détail d'une demande (demandes/detail.html)
        - ajouter un bouton obtenir QR code
        - le bouton va afficher un popup avec l'image du QR code et comme lien:
            -l'adresse IP de l'ordinateur + port + endpoint + "?id=" + id de la demande