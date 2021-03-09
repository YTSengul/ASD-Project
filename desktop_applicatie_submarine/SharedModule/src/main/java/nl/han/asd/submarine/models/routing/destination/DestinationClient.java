package nl.han.asd.submarine.models.routing.destination;

public class DestinationClient extends Destination {
    private final String alias;
    private final String encryptedSymmetricKey;

    public DestinationClient(String alias, String encryptedSymmetricKey) {
        this.alias = alias;
        this.encryptedSymmetricKey = encryptedSymmetricKey;
    }

    public String getAlias() {
        return alias;
    }

    public String getEncryptedSymmetricKey() {
        return encryptedSymmetricKey;
    }
}
