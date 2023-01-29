package ch.krateng.minecraft.ezrail;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class UtilsSigns {

    public static SignInfo extractSignInfo(Sign sign) {

        String[] signElements = signRead(sign);

        SignInfo result = new SignInfo();

        if (signElements.length > 0) {
            result.station = signElements[0];
            result.platform = Integer.parseInt(signElements[1]);
            if (signElements.length > 2) {
                result.nextStops = signElements[2].split(",");
            }
            return result;
        }
        else {
            return null;
        }
    }


    public static Sign getSignInfo(Block block, BlockFace face) {

        Block infoBlock = block.getRelative(face);
        Sign sign = null;
        if (infoBlock.getState() instanceof Sign) {
            sign = (Sign) infoBlock.getState();
        }

        return sign;

    }

    public static String[] signRead(Sign sign) {
        if (sign == null) {
            return new String[0];
        }
        else {
            String signText = String.join("|", sign.getLines());
            return signText.split("\\|");
        }
    }

}
