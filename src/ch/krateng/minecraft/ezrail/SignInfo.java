package ch.krateng.minecraft.ezrail;

import org.bukkit.block.BlockFace;

public class SignInfo {
    String station;
    int platform;
    String[] nextStops = {};
    BlockFace direction;

    public SignInfo() {
        this.station = "";
        this.platform = 0;
        this.nextStops = new String[]{};
        this.direction = BlockFace.NORTH;
    }
}
