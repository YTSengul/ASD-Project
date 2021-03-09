package nl.han.asd.submarine.models;

public class Chatter {

    private String username;
    private String password;
    private String ipAddress;
    private int port;
    private String alias;
    private String publicKey;

    //This constructor is used for testing purposes.
    public Chatter(String username, String alias) {
        this.username = username;
        this.alias = alias;
    }


    public Chatter(String username, String password, String alias) {
        this.username = username;
        this.password = password;
        this.alias = alias;
    }

    public Chatter() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey(){
        return publicKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
