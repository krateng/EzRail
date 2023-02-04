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
    LEAVING,
    THROUGH,
    ENDED
}

public class CartHoldingTask extends BukkitRunnable  {

    private final String station;
    private final RideableMinecart target_cart;
    private final BlockFace fromDirection;
    private CartStatus cartStatus;
    private final Block stationControlBlock;
    private final Block stationControlRail;
    private double defaultSpeed;
    private double secondsToDeparture;
    private boolean terminus;

    public static Set<RideableMinecart> handledCarts = new HashSet<>();


    public CartHoldingTask(String station, RideableMinecart target_cart, BlockFace fromDirection, Block stationControlBlock, boolean terminus) {
        this.station = station;
        this.target_cart = target_cart;
        this.fromDirection = fromDirection;
        this.stationControlBlock = stationControlBlock;
        this.stationControlRail = stationControlBlock.getRelative(0,2,0);
        this.terminus = terminus;
        //UtilsAnnounce.announce(target_cart,"Control Rail is " + stationControlRail.toString());

        handledCarts.add(target_cart);
        this.cartStatus = CartStatus.INCOMING;
    }

    public CartHoldingTask(String station, RideableMinecart target_cart, BlockFace fromDirection, Block stationControlBlock, boolean terminus, boolean startAtStation) {
        this(station,target_cart,fromDirection,stationControlBlock,terminus);
        if (startAtStation) {
            this.cartStatus = CartStatus.WAITING;
            this.secondsToDeparture = EzRailConfig.STATION_WAIT_TIME_NEW;
            this.defaultSpeed = EzRailConfig.DEFAULT_BASE_SPEED;
        }
    }


    @Override
    public void run() {
        if (target_cart == null || target_cart.isEmpty()) {
            this.cancel();
            handledCarts.remove(target_cart);
        }

        try {
            // cart has entered station zone, but not station
            if (cartStatus == CartStatus.INCOMING) {
                // just check if we're close enough for next status
                if (stationControlRail.getLocation().distance(target_cart.getLocation()) < EzRailConfig.MAX_DISTANCE_STATION_BEGIN) {
                    cartStatus = CartStatus.ARRIVING;

                    // if we're too slow, take the min speed as base from which to calculate our slowdown speeds
                    // we won't actually speed up to this if we're slower, it just causes the slowdown to start later
                    defaultSpeed = Math.max(EzRailConfig.DEFAULT_BASE_SPEED,target_cart.getVelocity().length());
                    //UtilsAnnounce.announce(target_cart,"Switch to ARRIVING, noted default speed is " + defaultSpeed);
                }
            }
            // cart is in station, before primary block
            else if (cartStatus == CartStatus.ARRIVING) {

                // no halt
                if (UtilsIndication.isIndicatingNoStop(target_cart) && !terminus) {
                    cartStatus = CartStatus.THROUGH;
                    return;
                }

                UtilsAnnounce.announce(target_cart,"Arriving at " + UtilsAnnounce.stationName(station),true);

                double distance = stationControlRail.getLocation().distance(target_cart.getLocation());

                // linear slowdown from entry to primary block
                double newSpeed = (distance / EzRailConfig.MAX_DISTANCE_STATION_BEGIN) * defaultSpeed;
                double relativeSpeed = newSpeed / target_cart.getVelocity().length();
                //UtilsAnnounce.announce(target_cart, "Slowing down to " + newSpeed + "(" + relativeSpeed + ")");
                if (relativeSpeed < 1) {
                    target_cart.setVelocity(target_cart.getVelocity().multiply(relativeSpeed));
                }


                if (target_cart.getLocation().getBlock().getX() == stationControlRail.getX() && target_cart.getLocation().getBlock().getZ() == stationControlRail.getZ()) {
                    // we have reached the central control block
                    target_cart.setVelocity(target_cart.getVelocity().multiply(0));
                    cartStatus = CartStatus.WAITING;
                    secondsToDeparture = (double) EzRailConfig.STATION_WAIT_TIME;
                }

            }
            // cart is waiting at station
            else if (cartStatus == CartStatus.WAITING) {

                if (terminus) {
                    cartStatus = CartStatus.ENDED;
                    return;
                }

                if (secondsToDeparture > EzRailConfig.STATION_DEPART_WARNING_TIME) {
                    UtilsAnnounce.announce(target_cart,"At " + UtilsAnnounce.stationName(station),true);
                }
                else if (secondsToDeparture > 0) {
                    UtilsAnnounce.announce(target_cart,"Departing " + UtilsAnnounce.stationName(station) + " in " + (int) Math.ceil(secondsToDeparture),true);
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
                    relativeSpeed = Math.min(relativeSpeed,1.2);
                    //UtilsAnnounce.announce(target_cart, "Speeding up to " + newSpeed + "(" + relativeSpeed + ")");
                    target_cart.setVelocity(target_cart.getVelocity().multiply(relativeSpeed));
                }

                if (distance > EzRailConfig.MAX_DISTANCE_STATION_BEGIN) {
                    //UtilsAnnounce.announce(target_cart, "Station " + station + " is releasing control of this minecart!");
                    this.cancel();
                    handledCarts.remove(target_cart);
                }

            }
            else if (cartStatus == CartStatus.THROUGH) {
                UtilsAnnounce.announce(target_cart,"Passing through " + UtilsAnnounce.stationName(station),true);

                double distance = stationControlRail.getLocation().distance(target_cart.getLocation());
                if (distance > EzRailConfig.MAX_DISTANCE_STATION_BEGIN) {
                    this.cancel();
                    handledCarts.remove(target_cart);
                }
            }

            else if (cartStatus == CartStatus.ENDED) {
                UtilsAnnounce.announce(target_cart,"At " + UtilsAnnounce.stationName(station) + ". End of line.",true);
            }

            else {
                this.cancel();
                handledCarts.remove(target_cart);
            }

        }
        catch (Exception e) {
            this.cancel();
            handledCarts.remove(target_cart);
        }


    }
}
