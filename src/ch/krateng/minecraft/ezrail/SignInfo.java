package ch.krateng.minecraft.ezrail;

public class SignInfo {
    String station;
    int platform;
    String[] nextStops = {};

    public SignInfo() {
        this.station = "";
        this.platform = 0;
        this.nextStops = new String[]{};
    }
}
