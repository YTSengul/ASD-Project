package nl.han.asd.submarine.models;

public class UserData {
    private String alias;
    private String publicKey;
    private String privateKey;
    private String userName;

    public UserData() {
    }

    public UserData(String alias) {
        this.alias = alias;
    }

    public UserData(String alias, String publicKey, String privateKey){
        this.alias = alias;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
