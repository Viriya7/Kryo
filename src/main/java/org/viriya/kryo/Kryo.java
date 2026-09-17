package org.viriya.kryo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.viriya.kryo.api.KryoRegistry;
import org.viriya.kryo.blueprint.BlueprintGroup;
import org.viriya.kryo.commands.KryoCommand;
import org.viriya.kryo.listeners.BlueprintClickListener;
import org.viriya.kryo.listeners.BlueprintListener;
import org.viriya.kryo.listeners.PlayerJoinListener;

public final class Kryo extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BlueprintListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueprintClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        PluginCommand kryoCommand = getCommand("kryo");
        if (kryoCommand != null) {
            kryoCommand.setExecutor(new KryoCommand(this));
        }

        registerGroups();
    }

    private void registerGroups() {
        String machineTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNkYzBmZWI3MDAxZTJjMTBmZDUwNjZlNTAxYjg3ZTNkNjQ3OTMwOTJiODVhNTBjODU2ZDk2MmY4YmU5MmM3OCJ9fX0=";

        BlueprintGroup machineGroup = new BlueprintGroup(
                "MACHINE",
                machineTexture,
                Component.text("Machine", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
        );

        KryoRegistry.registerGroup(machineGroup);
    }

    @Override
    public void onDisable() {
    }
}