package org.viriya.kryo.blueprint;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlueprintGUI {

    public static void openGUI(Player player) {
        Component title = Component.text("Blueprint", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(""));
        ItemStack prevPage = createGuiItem(Material.ENCHANTED_BOOK, Component.text("Previous Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        ItemStack nextPage = createGuiItem(Material.ENCHANTED_BOOK, Component.text("Next Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = y * 9 + x;

                if (y == 0 || y == 5) {
                    if (y == 5 && x == 1) {
                        gui.setItem(slot, prevPage);
                    } else if (y == 5 && x == 7) {
                        gui.setItem(slot, nextPage);
                    } else {
                        gui.setItem(slot, border);
                    }
                }
            }
        }

        player.openInventory(gui);
    }

    private static ItemStack createGuiItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}