package ch.krateng.minecraft.ezrail;


import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.vehicle.VehicleMoveEvent;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;


import java.util.HashMap;

public class TrainAnnouncer implements Listener {

    public static EzRail plugin_instance;

    // make sure we only get one announcement, even tho the event fires multiple times while going over a block
    public static HashMap<RideableMinecart,Long> cooldown = new HashMap<>();


    private static void fullAnnounce(RideableMinecart cart,String station,Integer platform,String[] nextStops,HashMap<Integer, String[]> otherConnections) {

        announce(cart,"You are arriving at " + ChatColor.GREEN + station + ChatColor.RESET + ".");
        announce(cart,"Arriving at " + ChatColor.GREEN + station + ChatColor.RESET, true);
        if (otherConnections.size() > 0) {
            announce(cart, "Other connections are available:");
            // TODO
        }
        else {
        }
        if (nextStops.length > 0) {
            announce(cart, "This train will continue to " + String.join(", ",nextStops) + ".");
        }
        else {
            announce(cart,"This train ends here.");
        }
    }

    public TrainAnnouncer(EzRail instance)
    {
        plugin_instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMyVehicleMove(VehicleMoveEvent event) {

        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof RideableMinecart) {
            RideableMinecart cart = (RideableMinecart) vehicle;

            Location cartLocation = cart.getLocation();
            Block indicatorBlock = cartLocation.subtract(0,2,0).getBlock();

            if (indicatorBlock.getType() == Material.COPPER_BLOCK || indicatorBlock.getType() == Material.EXPOSED_COPPER) {

                Long now = System.currentTimeMillis();

                if (cooldown.containsKey(cart) && cooldown.get(cart) > (now - 3000)) {
                    return;
                }

                // save to cooldown map to avoid double messages
                cooldown.put(cart,now);



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
                if (infoBlock.getState() instanceof Sign) {
                    Sign sign = (Sign) infoBlock.getState();
                    String signtext = String.join("|",sign.getLines());
                    String[] signelements = signtext.split("\\|");

                    String stationName = "";
                    Integer platform = 0;
                    String[] nextStops = {};
                    HashMap<Integer, String[]> otherConnections = new HashMap<>();

                    stationName = signelements[0];
                    platform = Integer.parseInt(signelements[1]);
                    if (signelements.length > 2) {
                        nextStops = signelements[2].split(",");
                    }


                    fullAnnounce(cart, stationName, platform, nextStops, otherConnections);

                }
                else {
                    // REMOVE
                    //announce(cart,"This is not a sign!");
                    //announce(cart, infoBlock.toString());
                }

                //announce(cart,infoBlock.toString());

                //Block blockUnderCart = cartLocation.getBlock();
                //Rails railUnderCart = (Rails) blockUnderCart.getState().getData();


            }


        }


    }

    private static void announce(RideableMinecart cart, String text, Boolean actionBar) {
        Player player = (Player) cart.getPassengers().get(0);
        TextComponent txt = new TextComponent(text);

        if (actionBar) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, txt);
        }
        else {
            player.spigot().sendMessage(ChatMessageType.SYSTEM, txt);
        }


    }
    private static void announce(RideableMinecart cart, String text) {
        announce(cart,text,false);
    }
}
