package ch.krateng.minecraft.ezrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;

public class EzRailListener implements Listener {

    public static EzRail plugin_instance;
    //public static HashMap<RideableMinecart,Long> cooldown = new HashMap<>();


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
            if (Utils.validCommandBlock(indicatorBlock)) {

                // filter out repeating events
                Long now = System.currentTimeMillis();
                //if (cooldown.containsKey(cart) && cooldown.get(cart) > (now - 3000)) {
                //    return;
                //}

                // save to cooldown
                //cooldown.put(cart, now);


                Location moveFrom = event.getFrom();
                Location moveTo = event.getTo();
                Vector train_direction = moveTo.toVector().subtract(moveFrom.toVector());

                BlockFace fromDirection = Utils.getOriginDirection(train_direction);
                Sign sign = Utils.getSignInfo(indicatorBlock, fromDirection);

                // PRIMARY CONTROL BLOCK
                if (sign != null) {
                }

                // SECONDARY CONTROL BLOCK
                else {

                    // Find associated sign
                    Sign primarySign = Utils.getNextControlSign(cart, train_direction);

                    // this is indeed the beginning of a station zone
                    if (primarySign != null) {

                        // Take over cart control
                        if (CartHoldingTask.handledCarts.contains(cart)) {
                        }
                        else {
                            SignInfo info = Utils.extractSignInfo(primarySign);

                            // Find other destinations
                            HashMap<Integer,String[]> otherConnections = Utils.getOtherPlatformDestinations(primarySign.getBlock(),info.station);
                            // Make announcement
                            UtilsAnnounce.announceIncoming(cart, info.station, info.platform, info.nextStops, otherConnections);
                            BukkitTask task = new CartHoldingTask(info.station,cart,fromDirection,primarySign.getBlock())
                                    .runTaskTimer(plugin_instance,2,CartHoldingTask.TICKS_PER_CONTROL_TICK);
                        }
                    }



                }


            }
        }
    }


}
