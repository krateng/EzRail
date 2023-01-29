package ch.krateng.minecraft.ezrail;

public class SignInfo {
    String station;
    Integer platform;
    String[] nextStops = {};

    public SignInfo() {
        this.station = "";
        this.platform = 0;
        this.nextStops = new String[]{};
    }
}
