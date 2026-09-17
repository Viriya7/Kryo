package org.viriya.kryo.blueprint;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Blueprint {

    @SuppressWarnings("deprecation")
    public static ItemStack createBlueprint(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.BLUE_DYE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§9Blueprint");
            meta.setLore(List.of("§8(Right click to open)"));

            NamespacedKey key = new NamespacedKey(plugin, "blueprint");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BLUEPRINT");

            item.setItemMeta(meta);
        }
        return item;
    }
}