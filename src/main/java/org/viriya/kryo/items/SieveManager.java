package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import javax.swing.*;
import java.util.Random;

public class SieveManager implements Listener {

    private static NamespacedKey sieveKey;
    private static final Random random = new Random();

    public static void init(Plugin plugin) {
        if (sieveKey == null) {
            sieveKey = new NamespacedKey(plugin, "custom_sieve");
        }
    }

    public static ItemStack getSieveItem() {
        ItemStack item = new ItemStack(Material.BOWL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Sieve", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            if (sieveKey != null) {
                meta.getPersistentDataContainer().set(sieveKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomSieve(ItemStack item) {
        if (item == null || !item.hasItemMeta() || sieveKey == null) return false;
        ItemMeta meta = item.getItemMeta();
        Byte value = meta.getPersistentDataContainer().get(sieveKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    private static ItemStack getRandomSieveDrop() {
        int roll = random.nextInt(100);

        if (roll < 20) {
            return DustManager.createDust("Iron Dust", Material.GUNPOWDER, "iron_dust");
        } else if (roll < 35) {
            return DustManager.createDust("Gold Dust", Material.GLOWSTONE_DUST, "gold_dust");
        } else if (roll < 50) {
            return DustManager.createDust("Copper Dust", Material.GLOWSTONE_DUST, "copper_dust");
        } else if (roll < 60) {
            return DustManager.createDust("Lead Dust", Material.SUGAR, "lead_dust");
        } else if (roll < 70) {
            return DustManager.createDust("Tin Dust", Material.SUGAR, "tin_dust");
        } else if (roll < 85) {
            return new ItemStack(Material.FLINT);
        } else {
            return new ItemStack(Material.IRON_NUGGET);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (isCustomSieve(handItem) && block.getType() == Material.GRAVEL) {
            event.setCancelled(true);

            Location loc = block.getLocation();
            block.setType(Material.AIR);

            loc.getWorld().playSound(loc.clone().add(0.5, 0.5, 0.5), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);

            ItemStack reward = getRandomSieveDrop();
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), reward);
        }
    }
}