package ch.krateng.minecraft.ezrail;

import org.bukkit.plugin.java.JavaPlugin;

public class EzRail extends JavaPlugin {

    public final TrainAnnouncer trainAnnouncer = new TrainAnnouncer(this);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this.trainAnnouncer, this);
    }

    @Override
    public void onDisable() {

    }
}
