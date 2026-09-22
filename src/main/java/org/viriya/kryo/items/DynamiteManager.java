package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamiteManager implements Listener {

    private static NamespacedKey dynamiteKey;
    private static Plugin pluginInstance;
    private static final Map<String, DynamiteData> activeCountdowns = new HashMap<>();

    private static class DynamiteData {
        int ticksLeft;
        UUID placerUUID;
        UUID displayUUID;
        UUID hologramUUID;

        DynamiteData(int ticksLeft, UUID placerUUID, UUID displayUUID, UUID hologramUUID) {
            this.ticksLeft = ticksLeft;
            this.placerUUID = placerUUID;
            this.displayUUID = displayUUID;
            this.hologramUUID = hologramUUID;
        }
    }

    public static void init(Plugin plugin) {
        pluginInstance = plugin;
        if (dynamiteKey == null) {
            dynamiteKey = new NamespacedKey(plugin, "custom_dynamite");
        }

        ItemStack[] dynamiteRecipe = {
                null, new ItemStack(Material.STRING), null,
                new ItemStack(Material.GUNPOWDER), new ItemStack(Material.GUNPOWDER), new ItemStack(Material.GUNPOWDER),
                new ItemStack(Material.GUNPOWDER), new ItemStack(Material.GUNPOWDER), new ItemStack(Material.GUNPOWDER)
        };

        ItemStack result = getDynamiteItem();
        result.setAmount(4);

        WorkbenchManager.registerCustomRecipe(result, dynamiteRecipe);
        BlueprintItem.registerToGroup("TOOLS", getDynamiteItem(), WorkbenchManager.getWorkbenchItem(), dynamiteRecipe);
    }

    public static ItemStack getDynamiteItem() {
        ItemStack item = new ItemStack(Material.RED_CANDLE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Dynamite", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            if (dynamiteKey != null) {
                meta.getPersistentDataContainer().set(dynamiteKey, PersistentDataType.LONG, System.nanoTime());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomDynamite(ItemStack item) {
        if (item == null || !item.hasItemMeta() || dynamiteKey == null) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(dynamiteKey, PersistentDataType.LONG);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (!isCustomDynamite(item)) return;

        event.setCancelled(true);

        Block clickedBlock = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        if (clickedBlock == null) return;

        Location targetLoc = clickedBlock.getRelative(face).getLocation();
        String locKey = targetLoc.getWorld().getName() + "," + targetLoc.getBlockX() + "," + targetLoc.getBlockY() + "," + targetLoc.getBlockZ();
        if (activeCountdowns.containsKey(locKey)) return;

        Player player = event.getPlayer();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        Location displayLoc = targetLoc.clone().add(0.5, 0.0, 0.5);

        BlockDisplay display = targetLoc.getWorld().spawn(displayLoc, BlockDisplay.class, entity -> {
            entity.setBlock(Bukkit.createBlockData(Material.RED_CANDLE));
            entity.setTransformation(new Transformation(
                    new Vector3f(-0.25f, 0.0f, -0.25f),
                    new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f)
            ));
        });

        Location holoLoc = targetLoc.clone().add(0.5, 0.6, 0.5);
        TextDisplay hologram = targetLoc.getWorld().spawn(holoLoc, TextDisplay.class, entity -> {
            entity.text(Component.text("3.0s", NamedTextColor.GRAY, TextDecoration.BOLD));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setShadowed(true);
        });

        targetLoc.getWorld().playSound(targetLoc.clone().add(0.5, 0.5, 0.5), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

        activeCountdowns.put(locKey, new DynamiteData(60, player.getUniqueId(), display.getUniqueId(), hologram.getUniqueId()));
        startCountdown(targetLoc, locKey);
    }

    @EventHandler
    public void onPlayerPunch(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block clickedBlock = event.getClickedBlock();
        Location loc;

        if (clickedBlock != null) {
            loc = clickedBlock.getLocation();
        } else {
            Player p = event.getPlayer();
            Block target = p.getTargetBlockExact(5);
            if (target == null) return;
            loc = target.getLocation();
        }

        String locKey = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        if (activeCountdowns.containsKey(locKey)) {
            DynamiteData data = activeCountdowns.remove(locKey);
            if (data != null) {
                Entity displayEntity = Bukkit.getEntity(data.displayUUID);
                if (displayEntity != null) displayEntity.remove();

                Entity holoEntity = Bukkit.getEntity(data.hologramUUID);
                if (holoEntity != null) holoEntity.remove();
            }

            event.setCancelled(true);

            ItemStack dropItem = getDynamiteItem();
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), dropItem);
        }
    }

    private static void startCountdown(Location loc, String locKey) {
        if (pluginInstance == null || !activeCountdowns.containsKey(locKey)) return;

        DynamiteData data = activeCountdowns.get(locKey);

        if (data.ticksLeft > 0) {
            int seconds = data.ticksLeft / 20;
            int tenths = (data.ticksLeft % 20) * 10 / 20;

            NamedTextColor textColor = (data.ticksLeft <= 20) ? NamedTextColor.RED : NamedTextColor.GRAY;
            Component text = Component.text(seconds + "." + tenths + "s", textColor, TextDecoration.BOLD);

            Entity holoEntity = Bukkit.getEntity(data.hologramUUID);
            if (holoEntity instanceof TextDisplay textDisplay) {
                textDisplay.text(text);
            }

            data.ticksLeft -= 2;

            Bukkit.getScheduler().runTaskLater(pluginInstance, () -> startCountdown(loc, locKey), 2L);
        } else {
            DynamiteData finishedData = activeCountdowns.remove(locKey);
            if (finishedData != null) {
                Entity displayEntity = Bukkit.getEntity(finishedData.displayUUID);
                if (displayEntity != null) displayEntity.remove();

                Entity holoEntity = Bukkit.getEntity(finishedData.hologramUUID);
                if (holoEntity != null) holoEntity.remove();
            }

            Location centerLoc = loc.clone().add(0.5, 0.5, 0.5);
            centerLoc.getWorld().createExplosion(centerLoc, 2.5f, true, true);
        }
    }
}