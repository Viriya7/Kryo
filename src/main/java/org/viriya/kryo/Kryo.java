package org.viriya.kryo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.viriya.kryo.api.KryoRegistry;
import org.viriya.kryo.blueprint.BlueprintGroup;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.commands.KryoCommand;
import org.viriya.kryo.items.DustManager;
import org.viriya.kryo.items.DynamiteManager;
import org.viriya.kryo.items.SieveManager;
import org.viriya.kryo.listeners.BlueprintClickListener;
import org.viriya.kryo.listeners.BlueprintListener;
import org.viriya.kryo.listeners.PlayerJoinListener;
import org.viriya.kryo.workbench.WorkbenchManager;

public final class Kryo extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BlueprintListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueprintClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        registerGroups();

        SieveManager.init(this);
        getServer().getPluginManager().registerEvents(new SieveManager(), this);

        WorkbenchManager.init(this);
        getServer().getPluginManager().registerEvents(new WorkbenchManager(), this);

        DustManager.init(this);
        DynamiteManager.init(this);

        PluginCommand kryoCommand = getCommand("kryo");
        if (kryoCommand != null) {
            kryoCommand.setExecutor(new KryoCommand(this));
        }
    }

    private void registerGroups() {
        String machineTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNkYzBmZWI3MDAxZTJjMTBmZDUwNjZlNTAxYjg3ZTNkNjQ3OTMwOTJiODVhNTBjODU2ZDk2MmY4YmU5MmM3OCJ9fX0=";

        BlueprintGroup machineGroup = new BlueprintGroup(
                "MACHINE",
                machineTexture,
                Component.text("Machine", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
        );

        BlueprintGroup resourcesGroup = new BlueprintGroup("RESOURCES", Material.OAK_SAPLING, Component.text("Resources", NamedTextColor.GREEN));
        BlueprintGroup toolsGroup = new BlueprintGroup("TOOLS", Material.IRON_PICKAXE, Component.text("Tools", NamedTextColor.WHITE));

        KryoRegistry.registerGroup(machineGroup);
        KryoRegistry.registerGroup(toolsGroup);
        KryoRegistry.registerGroup(resourcesGroup);
    }

    @Override
    public void onDisable() {
    }
}