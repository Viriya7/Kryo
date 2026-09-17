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
        BlueprintGroup machineGroup = new BlueprintGroup(
                "MACHINE",
                Material.CRAFTING_TABLE,
                Component.text("Machine", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
        );

        // Contoh: Kamu bisa menambahkan item ke dalam grup ini nantinya di sini
        // machineGroup.addItem(CustomItems.SOME_MACHINE);

        KryoRegistry.registerGroup(machineGroup);
    }

    @Override
    public void onDisable() {
    }
}