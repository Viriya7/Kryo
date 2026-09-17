package org.viriya.kryo.blueprint;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Blueprint {

    public static ItemStack createBlueprint(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.BLUE_DYE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Blueprint", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("(Right click to open)", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
            ));

            NamespacedKey key = new NamespacedKey(plugin, "blueprint");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BLUEPRINT");

            item.setItemMeta(meta);
        }
        return item;
    }
}