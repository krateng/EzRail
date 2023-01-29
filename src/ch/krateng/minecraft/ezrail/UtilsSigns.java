package ch.krateng.minecraft.ezrail;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.Sign;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UtilsSigns {

    public static SignInfo extractSignInfo(Sign sign) {

        if (sign == null) {
            return null;
        }

        SignInfo result = new SignInfo();
        LinkedList<String> nextStops = new LinkedList<>();

        WallSign sign2 = (WallSign) sign.getBlockData();
        result.direction = sign2.getFacing().getOppositeFace();


        String[] lines = sign.getLines();
        for (String line : lines) {
            if (line == lines[0]) {
                String[] elements = lines[0].split("\\|");
                result.station = elements[0];
                result.platform = Integer.parseInt(elements[1]);
            }
            else {
                Collections.addAll(nextStops, line.split(","));

            }
        }

        nextStops.removeIf(item -> item == null || "".equals(item));
        result.nextStops = nextStops.toArray(result.nextStops);

        return result;
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
