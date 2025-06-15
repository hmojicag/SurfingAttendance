package mx.ssaj.surfingattendance.surfingtime.dto;

public class RedeemTerminalOtp {
    private String Otp;
    private String DeviceSn;


    public String getOtp() {
        return Otp;
    }

    public void setOtp(String otp) {
        Otp = otp;
    }

    public String getDeviceSn() {
        return DeviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        DeviceSn = deviceSn;
    }
}
