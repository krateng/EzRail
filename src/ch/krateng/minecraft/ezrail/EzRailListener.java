package ch.krateng.minecraft.ezrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class EzRailListener implements Listener {

    public static EzRail plugin_instance;
    public static HashMap<RideableMinecart,Long> cooldown = new HashMap<>();

    public static TrainAnnouncer announcer = new TrainAnnouncer();
    public static TrainController controller = new TrainController();

    public EzRailListener(EzRail instance)
    {
        plugin_instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMyVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof RideableMinecart) {
            RideableMinecart cart = (RideableMinecart) vehicle;

            Location cartLocation = cart.getLocation();
            Block indicatorBlock = cartLocation.subtract(0, 2, 0).getBlock();

            // plugin only ever handles events when there's a copper block
            if (indicatorBlock.getType() == Material.COPPER_BLOCK || indicatorBlock.getType() == Material.EXPOSED_COPPER) {

                // filter out repeating events
                Long now = System.currentTimeMillis();
                if (cooldown.containsKey(cart) && cooldown.get(cart) > (now - 3000)) {
                    return;
                }

                // save to cooldown
                cooldown.put(cart, now);

                // pick out relevant sign for handlers
                Location moveFrom = event.getFrom();
                Location moveTo = event.getTo();
                Vector train_direction = moveTo.toVector().subtract(moveFrom.toVector()).normalize();
                BlockFace signFace = null;
                if (train_direction.getX() > 0.6) {
                    signFace = BlockFace.WEST;
                } else if (train_direction.getX() < -0.6) {
                    signFace = BlockFace.EAST;
                } else if (train_direction.getZ() > 0.6) {
                    signFace = BlockFace.NORTH;
                } else if (train_direction.getZ() < -0.6) {
                    signFace = BlockFace.SOUTH;
                }
                Block infoBlock = indicatorBlock.getRelative(signFace);

                Sign sign = null;
                if (infoBlock.getState() instanceof Sign) {
                    sign = (Sign) infoBlock.getState();
                }


                // send to out handlers
                announcer.onCartOverCopper(event, cart, indicatorBlock, sign);

            }
        }
    }
}
