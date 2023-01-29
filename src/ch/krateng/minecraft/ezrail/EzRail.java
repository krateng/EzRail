package ch.krateng.minecraft.ezrail;

import org.bukkit.plugin.java.JavaPlugin;

public class EzRail extends JavaPlugin {

    public static EzRail instance;

    public final EzRailListener railListener = new EzRailListener(this);


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this.railListener, this);
        instance = this;
    }

    @Override
    public void onDisable() {

    }
}
