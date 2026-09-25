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
import org.viriya.kryo.items.*;
import org.viriya.kryo.listeners.BlueprintClickListener;
import org.viriya.kryo.listeners.BlueprintListener;
import org.viriya.kryo.listeners.PlayerJoinListener;
import org.viriya.kryo.workbench.WorkbenchManager;
import org.viriya.kryo.smeltry.SmeltryManager;

public final class Kryo extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BlueprintListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueprintClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        registerGroups();

        SmeltryManager.init(this);
        SieveManager.init(this);
        DustManager.init(this);
        DynamiteManager.init(this);
        IngotManager.init(this);

        GrapplingHookManager.init(this);
        MercuryManager.init(this);
        BulletManager.init(this);
        BulletAssemblyManager.init(this);

        WorkbenchManager.init(this);

        getServer().getPluginManager().registerEvents(new WorkbenchManager(), this);
        getServer().getPluginManager().registerEvents(new SmeltryManager(), this);
        getServer().getPluginManager().registerEvents(new SieveManager(), this);
        getServer().getPluginManager().registerEvents(new DynamiteManager(), this);
        getServer().getPluginManager().registerEvents(new IngotManager(), this);
        getServer().getPluginManager().registerEvents(new GrapplingHookManager(), this);
        getServer().getPluginManager().registerEvents(new MercuryManager(),this);
        getServer().getPluginManager().registerEvents(new BulletManager(),this);
        getServer().getPluginManager().registerEvents(new BulletAssemblyManager(),this);

        PluginCommand kryoCommand = getCommand("kryo");
        if (kryoCommand != null) {
            kryoCommand.setExecutor(new KryoCommand(this));
        }
    }

    private void registerGroups() {
        String machineTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNkYzBmZWI3MDAxZTJjMTBmZDUwNjZlNTAxYjg3ZTNkNjQ3OTMwOTJiODVhNTBjODU2ZDk2MmY4YmU5MmM3OCJ9fX0=";

        BlueprintGroup machineGroup = new BlueprintGroup("MACHINE", machineTexture, Component.text("Machine", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        BlueprintGroup resourcesGroup = new BlueprintGroup("RESOURCES", Material.OAK_SAPLING, Component.text("Resources", NamedTextColor.GREEN));
        BlueprintGroup toolsGroup = new BlueprintGroup("TOOLS", Material.IRON_PICKAXE, Component.text("Tools", NamedTextColor.WHITE));
        BlueprintGroup miscGroup = new BlueprintGroup("MISC", Material.BUCKET, Component.text("Misc", NamedTextColor.WHITE));
        BlueprintGroup FirearmGroup = new BlueprintGroup("FIREARM", Material.STONE_HOE, Component.text("Firearm", NamedTextColor.RED), "MISC");

        KryoRegistry.registerGroup(machineGroup);
        KryoRegistry.registerGroup(toolsGroup);
        KryoRegistry.registerGroup(resourcesGroup);
        KryoRegistry.registerGroup(miscGroup);
        KryoRegistry.registerGroup(FirearmGroup);
    }

    @Override
    public void onDisable() {
    }
}