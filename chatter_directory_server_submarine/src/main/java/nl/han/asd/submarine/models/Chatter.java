package nl.han.asd.submarine.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatter")
public class Chatter {

    @Id
    private String id;

    private String alias;
    private String publicKey;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String ipAddress;


    public Chatter() {
    }

    public Chatter(String alias, String publicKey, String username, String passwordHash, String passwordSalt, String ipAddress) {
        this.alias = alias;
        this.publicKey = publicKey;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.ipAddress = ipAddress;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isValid() {
        return alias != null &&
                publicKey != null &&
                username != null &&
                passwordHash != null &&
                ipAddress != null;
    }
}
