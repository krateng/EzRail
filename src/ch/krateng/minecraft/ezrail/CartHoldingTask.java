package ch.krateng.minecraft.ezrail;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

// This task is supposed to be active for any cart the moment it passes the secondary control block and is now
// inside the stations control zone
// it slows it down, stops it, waits, speeds it up again, then releases control

enum CartStatus {
    INCOMING,
    ARRIVING,
    WAITING,
    LEAVING
}

public class CartHoldingTask extends BukkitRunnable  {

    private String station;
    private RideableMinecart target_cart;
    private BlockFace fromDirection;
    private CartStatus cartStatus;
    private Block stationControlBlock;
    private Block stationControlRail;
    private double defaultSpeed;
    private double secondsToDeparture;

    public static Set<RideableMinecart> handledCarts = new HashSet<>();


    public CartHoldingTask(String station, RideableMinecart target_cart, BlockFace fromDirection, Block stationControlBlock) {
        this.station = station;
        this.target_cart = target_cart;
        this.fromDirection = fromDirection;
        this.stationControlBlock = stationControlBlock;
        this.stationControlRail = stationControlBlock.getRelative(0,2,0);
        //UtilsAnnounce.announce(target_cart,"Control Rail is " + stationControlRail.toString());

        handledCarts.add(target_cart);
        this.cartStatus = CartStatus.INCOMING;
    }


    @Override
    public void run() {
        try {
            // cart has entered station zone, but not station
            if (cartStatus == CartStatus.INCOMING) {
                // just check if we're close enough for next status
                if (stationControlRail.getLocation().distance(target_cart.getLocation()) < EzRailConfig.MAX_DISTANCE_STATION_BEGIN) {
                    cartStatus = CartStatus.ARRIVING;
                    defaultSpeed = target_cart.getVelocity().length();
                    //UtilsAnnounce.announce(target_cart,"Switch to ARRIVING, noted default speed is " + defaultSpeed);
                }
            }
            // cart is in station, before primary block
            else if (cartStatus == CartStatus.ARRIVING) {
                UtilsAnnounce.announce(target_cart,"Arriving at " + UtilsAnnounce.stationName(station),true);

                double distance = stationControlRail.getLocation().distance(target_cart.getLocation());

                // linear slowdown from entry to primary block
                double newSpeed = (distance / EzRailConfig.MAX_DISTANCE_STATION_BEGIN) * defaultSpeed;
                double relativeSpeed = newSpeed / target_cart.getVelocity().length();
                //UtilsAnnounce.announce(target_cart, "Slowing down to " + newSpeed + "(" + relativeSpeed + ")");
                target_cart.setVelocity(target_cart.getVelocity().multiply(relativeSpeed));

                if (target_cart.getLocation().getBlock().getX() == stationControlRail.getX() && target_cart.getLocation().getBlock().getZ() == stationControlRail.getZ()) {
                    // we have reached the central control block
                    target_cart.setVelocity(target_cart.getVelocity().multiply(0));
                    cartStatus = CartStatus.WAITING;
                    secondsToDeparture = (double) EzRailConfig.STATION_WAIT_TIME;
                }

            }
            // cart is waiting at station
            else if (cartStatus == CartStatus.WAITING) {

                if (secondsToDeparture > 4) {
                    UtilsAnnounce.announce(target_cart,"At " + UtilsAnnounce.stationName(station),true);
                }
                else if (secondsToDeparture > 0) {
                    UtilsAnnounce.announce(target_cart,"Departing " + UtilsAnnounce.stationName(station) + " in " + (int) secondsToDeparture,true);
                }
                else {
                    cartStatus = CartStatus.LEAVING;
                    // initial nudge just to get the direction right
                    target_cart.setVelocity(fromDirection.getDirection().multiply(-0.1));
                }
                secondsToDeparture = secondsToDeparture - ((double) EzRailConfig.TICKS_PER_CONTROL_TICK / 20.0);
            }
            // cart is leaving
            else if (cartStatus == CartStatus.LEAVING) {
                UtilsAnnounce.announce(target_cart,"Departing " + UtilsAnnounce.stationName(station),true);

                double distance = stationControlRail.getLocation().distance(target_cart.getLocation());
                double newSpeed = (distance / EzRailConfig.MAX_DISTANCE_STATION_BEGIN) * defaultSpeed;
                double relativeSpeed = newSpeed / target_cart.getVelocity().length();
                if (relativeSpeed > 1) {
                    //UtilsAnnounce.announce(target_cart, "Speeding up to " + newSpeed + "(" + relativeSpeed + ")");
                    target_cart.setVelocity(target_cart.getVelocity().multiply(relativeSpeed));
                }

                if (distance > EzRailConfig.MAX_DISTANCE_STATION_BEGIN) {
                    //UtilsAnnounce.announce(target_cart, "Station " + station + " is releasing control of this minecart!");
                    this.cancel();
                    handledCarts.remove(target_cart);
                }

            }

        }
        finally {

        }


    }
}
