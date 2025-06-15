package mx.ssaj.surfingattendance.surfingtime.dto;

public class WebApiClients {
    private int Emisor;
    private String ClientId;
    private String ClientSecret;
    private String Name;
    private String Description;
    private String DeviceSn;

    public int getEmisor() {
        return Emisor;
    }

    public void setEmisor(int emisor) {
        Emisor = emisor;
    }

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    public String getClientSecret() {
        return ClientSecret;
    }

    public void setClientSecret(String clientSecret) {
        ClientSecret = clientSecret;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDeviceSn() {
        return DeviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        DeviceSn = deviceSn;
    }
}
