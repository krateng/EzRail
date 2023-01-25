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




    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCartOverCopper(VehicleMoveEvent event, RideableMinecart cart, Block indicatorBlock, Sign sign) {

        Location cartLocation = cart.getLocation();

        String stationName = "";
        Integer platform = 0;
        String[] nextStops = {};
        HashMap<Integer, String[]> otherConnections = new HashMap<>();

        String[] sign_elements = signRead(sign);
        if (sign_elements.length > 0) {
            stationName = sign_elements[0];
            platform = Integer.parseInt(sign_elements[1]);
            if (sign_elements.length > 2) {
                nextStops = sign_elements[2].split(",");
            }

            fullAnnounce(cart, stationName, platform, nextStops, otherConnections);
        }




        //announce(cart,infoBlock.toString());

        //Block blockUnderCart = cartLocation.getBlock();
        //Rails railUnderCart = (Rails) blockUnderCart.getState().getData();


    }

    private static String[] signRead(Sign sign) {
        if (sign == null) {
            return new String[0];
        }
        else {
            String signtext = String.join("|", sign.getLines());
            return signtext.split("\\|");
        }
    }


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
