package nl.han.asd.submarine.controllers.dto;

import nl.han.asd.submarine.models.ChatterDTO;

public class ChatterRegistrationDTO extends ChatterDTO {

    private String alias;
    private String publicKey;

    public ChatterRegistrationDTO() {
    }

    public ChatterRegistrationDTO(String alias, String publicKey, String username, String password, String ipAddress) {
        super(username, password, ipAddress);
        this.alias = alias;
        this.publicKey = publicKey;

    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
