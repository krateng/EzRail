package ch.krateng.minecraft.ezrail;

import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class UtilsAnnounce {

    public static String stationName(String name) {
        return ChatColor.GREEN + name + ChatColor.RESET;
    }


    public static void announceIncoming(RideableMinecart cart, String station, int platform, String[] nextStops, HashMap<Integer, String[]> otherConnections) {

        announce(cart,"You are arriving at " + stationName(station) + ".");
        //announce(cart,"Arriving at " + stationName(station), true);

        LinkedList<String> otherConnectionsStrings = new LinkedList<>();
        for (Map.Entry<Integer,String[]> entry : otherConnections.entrySet()) {
            if (entry.getValue().length > 0 && entry.getKey() != platform) {
                otherConnectionsStrings.add(String.join(", ", entry.getValue()) + " (Platform " + entry.getKey().toString() + ")");
            }

        }

        if (otherConnectionsStrings.size() > 0) {
            announce(cart, "Other connections are available:");
            announce(cart,String.join(", ",otherConnectionsStrings));
        }

        if (nextStops.length > 0) {
            announce(cart, "This train will continue to " + String.join(", ",nextStops) + ".");
            announce(cart, "Hold out a torch to continue without halt.");
        }
        else {
            announce(cart,"This train ends here.");
        }
    }


    public static void announce(RideableMinecart cart, String text, Boolean actionBar) {
        try {
            Player player = (Player) cart.getPassengers().get(0);
            TextComponent txt = new TextComponent(text);

            if (actionBar) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, txt);
            }
            else {
                player.spigot().sendMessage(ChatMessageType.SYSTEM, txt);
            }
        }
        catch (IndexOutOfBoundsException ignored) {

        }




    }
    public static void announce(RideableMinecart cart, String text) {
        announce(cart,text,false);
    }
}
