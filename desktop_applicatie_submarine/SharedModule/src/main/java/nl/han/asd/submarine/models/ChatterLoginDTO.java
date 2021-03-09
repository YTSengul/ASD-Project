package nl.han.asd.submarine.models;

public class ChatterLoginDTO {

    private String username;
    private String password;
    private String ipAddress;

    public ChatterLoginDTO(String username, String password, String ipAddress) {
        this.username = username;
        this.password = password;
        this.ipAddress = ipAddress;
    }

    public ChatterLoginDTO() {

    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
