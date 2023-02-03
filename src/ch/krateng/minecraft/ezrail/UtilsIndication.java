package ch.krateng.minecraft.ezrail;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.inventory.ItemStack;

public class UtilsIndication {

    public static boolean isIndicatingNoStop(RideableMinecart cart) {
        try{
            Player player = (Player) cart.getPassengers().get(0);
            ItemStack heldItems = player.getInventory().getItemInMainHand();
            return (heldItems.getType() == Material.TORCH);
        }
        catch (IndexOutOfBoundsException ignored) {
            return false;
        }
    }
}
