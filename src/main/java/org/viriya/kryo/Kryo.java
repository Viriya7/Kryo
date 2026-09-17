package org.viriya.kryo;

import org.bukkit.plugin.java.JavaPlugin;
import org.viriya.kryo.listeners.BlueprintClickListener;
import org.viriya.kryo.listeners.BlueprintListener;
import org.viriya.kryo.listeners.PlayerJoinListener;

public final class Kryo extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueprintListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueprintClickListener(), this);

        getLogger().info("[Kryo] : Started");
    }

    @Override
    public void onDisable() {
        getLogger().info("[Kryo] : Ended");
    }
}