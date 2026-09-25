package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.smeltry.SmeltryManager;

import java.util.Random;

public class MercuryManager implements Listener {

    private static NamespacedKey cinnabarKey;
    private static NamespacedKey mercuryKey;
    private static final Random random = new Random();

    public static void init(Plugin plugin) {
        if (cinnabarKey == null) {
            cinnabarKey = new NamespacedKey(plugin, "custom_cinnabar");
        }
        if (mercuryKey == null) {
            mercuryKey = new NamespacedKey(plugin, "custom_mercury");
        }
        registerRecipes();
    }

    public static ItemStack getCinnabar() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Cinnabar", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
            if (cinnabarKey != null) {
                meta.getPersistentDataContainer().set(cinnabarKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getMercury() {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Mercury", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.setColor(Color.fromRGB(128, 128, 128));

            if (mercuryKey != null) {
                meta.getPersistentDataContainer().set(mercuryKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomCinnabar(ItemStack item) {
        if (item == null || !item.hasItemMeta() || cinnabarKey == null) return false;
        Byte value = item.getItemMeta().getPersistentDataContainer().get(cinnabarKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static boolean isCustomMercury(ItemStack item) {
        if (item == null || !item.hasItemMeta() || mercuryKey == null) return false;
        Byte value = item.getItemMeta().getPersistentDataContainer().get(mercuryKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.REDSTONE_ORE || block.getType() == Material.DEEPSLATE_REDSTONE_ORE) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();

            if (isIronOrBetter(tool.getType())) {
                if (random.nextDouble() < 0.40) { // 40% drop rate
                    event.setDropItems(false);
                    block.getWorld().dropItemNaturally(block.getLocation(), getCinnabar());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (isCustomMercury(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot drink Mercury!", NamedTextColor.RED));
        }
    }

    private boolean isIronOrBetter(Material mat) {
        return mat == Material.IRON_PICKAXE || mat == Material.DIAMOND_PICKAXE || mat == Material.NETHERITE_PICKAXE;
    }

    private static void registerRecipes() {
        ItemStack ironPickaxe = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta pickMeta = ironPickaxe.getItemMeta();
        if (pickMeta != null) {
            pickMeta.displayName(Component.text("Iron Pickaxe", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            ironPickaxe.setItemMeta(pickMeta);
        }

        ItemStack[] cinnabarGrid = new ItemStack[9];
        cinnabarGrid[4] = new ItemStack(Material.REDSTONE_ORE);

        BlueprintItem.registerToGroup("Resources", getCinnabar(), ironPickaxe, cinnabarGrid);

        ItemStack[] mercuryGrid = new ItemStack[9];
        mercuryGrid[4] = getCinnabar();

        BlueprintItem.registerToGroup("Resources", getMercury(), SmeltryManager.getSmeltryItem(), mercuryGrid);
    }
}