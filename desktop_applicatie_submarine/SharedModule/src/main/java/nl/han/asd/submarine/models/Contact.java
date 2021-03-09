package nl.han.asd.submarine.models;

public class Contact {
    private final String publicKey;
    private final String alias;

    public Contact(String publicKey, String alias) {
        this.publicKey = publicKey;
        this.alias = alias;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAlias() {
        return alias;
    }
}
