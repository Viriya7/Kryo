package org.viriya.kryo.api;

import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public abstract class KryoAddon extends JavaPlugin {

    @Override
    public final void onEnable() {
        onAddonEnable();
    }

    @Override
    public final void onDisable() {
        onAddonDisable();
    }

    public abstract void onAddonEnable();

    public void onAddonDisable() {}

    public JavaPlugin getKryo() {
        return (JavaPlugin) getServer().getPluginManager().getPlugin("Kryo");
    }
}