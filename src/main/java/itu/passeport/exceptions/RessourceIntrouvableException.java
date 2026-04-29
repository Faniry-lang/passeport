package itu.passeport.exceptions;

public class RessourceIntrouvableException extends DemandeBusinessException {
    public RessourceIntrouvableException(String message) {
        super(message);
    }

    public RessourceIntrouvableException(String message, Throwable cause) {
        super(message, cause);
    }
}
