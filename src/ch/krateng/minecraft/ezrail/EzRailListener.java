package ch.krateng.minecraft.ezrail;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;


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
            if (UtilsRails.validCommandBlock(indicatorBlock)) {

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

                BlockFace fromDirection = UtilsRails.getOriginDirection(train_direction);
                Sign sign = UtilsSigns.getSignInfo(indicatorBlock, fromDirection);

                // PRIMARY CONTROL BLOCK
                if (sign != null) {
                }

                // SECONDARY CONTROL BLOCK
                else {

                    // Find associated sign
                    Sign primarySign = UtilsRails.getNextControlSign(cart, train_direction, EzRailConfig.MAX_DISTANCE_SECONDARY_CONTROL_BLOCK, false);

                    // this is indeed the beginning of a station zone
                    if (primarySign != null) {

                        // Take over cart control
                        if (CartHoldingTask.handledCarts.contains(cart)) {
                        }
                        else {
                            SignInfo info = UtilsSigns.extractSignInfo(primarySign);

                            // Find other destinations
                            HashMap<Integer,String[]> otherConnections = UtilsRails.getOtherPlatformDestinations(primarySign.getBlock(),info.station);
                            // Make announcement
                            UtilsAnnounce.announceIncoming(cart, info.station, info.platform, info.nextStops, otherConnections);
                            boolean terminus = (info.nextStops.length == 0);
                            BukkitTask task = new CartHoldingTask(info.station,cart,info.direction.getOppositeFace(),primarySign.getBlock(),terminus)
                                    .runTaskTimer(plugin_instance,2, EzRailConfig.TICKS_PER_CONTROL_TICK);
                        }
                    }



                }


            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMyVehicleEnter(VehicleEnterEvent event) {
        // CAREFUL: The cart has no passenger at the time of this event firing!
        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof RideableMinecart) {
            RideableMinecart cart = (RideableMinecart) vehicle;



            Location cartLocation = cart.getLocation();
            Rail rail = (Rail) cartLocation.getBlock().getBlockData();

            new BukkitRunnable() {
                @Override
                public void run() {
                    //plugin_instance.getServer().broadcastMessage("Your block is " + rail.toString());

                    for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH}) {
                        Sign sign = UtilsRails.getNextControlSign(cart, face.getDirection(), EzRailConfig.MAX_DISTANCE_STATION_BEGIN, false);
                        Sign sign_reverse = UtilsRails.getNextControlSign(cart, face.getDirection(), EzRailConfig.MAX_DISTANCE_STATION_BEGIN, true);
                        //UtilsAnnounce.announce(cart, "Next ctrl sign in direction " + face + " is " + sign);
                        if (sign != null) {
                            SignInfo info = UtilsSigns.extractSignInfo(sign);
                            boolean terminus = (info.nextStops.length == 0);
                            BukkitTask task = new CartHoldingTask(info.station,cart,face.getOppositeFace(),sign.getBlock(),terminus,true)
                                    .runTaskTimer(plugin_instance,2, EzRailConfig.TICKS_PER_CONTROL_TICK);

                        } else if (sign_reverse != null) {
                            SignInfo info = UtilsSigns.extractSignInfo(sign_reverse);
                            boolean terminus = (info.nextStops.length == 0);
                            BukkitTask task = new CartHoldingTask(info.station,cart,face,sign_reverse.getBlock(),terminus,true)
                                    .runTaskTimer(plugin_instance,2, EzRailConfig.TICKS_PER_CONTROL_TICK);
                        }


                    }
                }

            }.runTaskLater(plugin_instance, 5);



        }
    }


}
