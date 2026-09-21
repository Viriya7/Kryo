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
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.util.HashMap;
import java.util.Map;

public class DynamiteManager implements Listener {

    private static NamespacedKey dynamiteKey;
    private static Plugin pluginInstance;

    private static final Map<String, ArmorStand> activeCountdowns = new HashMap<>();

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
        ItemStack item = new ItemStack(Material.RED_CANDLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Dynamite", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            if (dynamiteKey != null) {
                meta.getPersistentDataContainer().set(dynamiteKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomDynamite(ItemStack item) {
        if (item == null || !item.hasItemMeta() || dynamiteKey == null) return false;
        ItemMeta meta = item.getItemMeta();
        Byte value = meta.getPersistentDataContainer().get(dynamiteKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isCustomDynamite(item)) return;

        Block block = event.getBlockPlaced();
        if (block.getType() != Material.RED_CANDLE) return;

        String locKey = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
        if (activeCountdowns.containsKey(locKey)) return;

        Bukkit.getScheduler().runTaskLater(pluginInstance, () -> {
            if (block.getType() == Material.RED_CANDLE) {
                if (block.getBlockData() instanceof Candle candle) {
                    candle.setLit(true);
                    block.setBlockData(candle);
                }

                Location centerLoc = block.getLocation().add(0.5, 0.0, 0.5);
                block.getWorld().playSound(centerLoc, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

                ArmorStand hologram = (ArmorStand) block.getWorld().spawnEntity(centerLoc.clone().add(0, 0.3, 0), EntityType.ARMOR_STAND);
                hologram.setVisible(false);
                hologram.setGravity(false);
                hologram.setMarker(true);
                hologram.setCustomNameVisible(true);

                activeCountdowns.put(locKey, hologram);

                startSmoothCountdown(block, hologram, locKey, 60);
            }
        }, 1L);
    }

    private static void startSmoothCountdown(Block block, ArmorStand hologram, String locKey, int ticksLeft) {
        if (pluginInstance == null) return;

        if (ticksLeft > 0) {
            if (block.getType() == Material.RED_CANDLE) {
                int seconds = ticksLeft / 20;
                int tenths = (ticksLeft % 20) * 10 / 20;
                String timeText = "§c[ " + seconds + "." + tenths + "s ]";

                hologram.setCustomName(timeText);

                Bukkit.getScheduler().runTaskLater(pluginInstance, () -> {
                    startSmoothCountdown(block, hologram, locKey, ticksLeft - 2);
                }, 2L);
            } else {
                hologram.remove();
                activeCountdowns.remove(locKey);
            }
        } else {
            hologram.remove();
            activeCountdowns.remove(locKey);

            Location loc = block.getLocation().add(0.5, 0.5, 0.5);

            int candleCount = 1;
            if (block.getBlockData() instanceof Candle candleData) {
                candleCount = candleData.getCandles();
            }

            block.setType(Material.AIR);

            float explosionPower = 1.0f + (candleCount * 0.6f);
            loc.getWorld().createExplosion(loc, explosionPower, true, true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.RED_CANDLE) {
            String locKey = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
            if (activeCountdowns.containsKey(locKey)) {
                activeCountdowns.get(locKey).remove();
                activeCountdowns.remove(locKey);
            }

            int count = 1;
            if (block.getBlockData() instanceof Candle candle) {
                count = candle.getCandles();
            }

            event.setDropItems(false);
            ItemStack dropItem = getDynamiteItem();
            dropItem.setAmount(count);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), dropItem);
        }
    }
}