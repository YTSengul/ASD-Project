package nl.han.asd.submarine.controllers.dto;

public class ChatterAddressDTO {

    private String ipAddress;

    public ChatterAddressDTO(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ChatterAddressDTO() {
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
