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
import org.viriya.kryo.api.KryoRegistry;

import java.util.List;

public class BlueprintGUI {

    private static final int ITEMS_PER_PAGE = 28;

    public static void openMenu(Player player, int page) {
        Component title = Component.text("Blueprint - Page " + (page + 1), NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack settings = createGuiItem(Material.REPEATER, Component.text("Settings", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = y * 9 + x;
                if (y == 0 || y == 5) {
                    if (y == 0 && x == 0) {
                        gui.setItem(slot, settings);
                    } else {
                        gui.setItem(slot, border);
                    }
                }
            }
        }

        List<BlueprintGroup> groups = KryoRegistry.getGroups();
        int totalPages = (int) Math.ceil((double) groups.size() / ITEMS_PER_PAGE);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, groups.size());

        int slotIndex = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if ((slotIndex + 1) % 9 == 0) {
                slotIndex += 2;
            }
            gui.setItem(slotIndex++, groups.get(i).getIconItem());
        }

        if (page > 0) {
            ItemStack prevPage = createGuiItem(Material.ENCHANTED_BOOK, Component.text("Previous Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            gui.setItem(46, prevPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = createGuiItem(Material.ENCHANTED_BOOK, Component.text("Next Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            gui.setItem(52, nextPage);
        }

        player.openInventory(gui);
    }

    public static void openSettings(Player player) {
        Component title = Component.text("Settings", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, true);
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack back = createGuiItem(Material.BARRIER, Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        for (int x = 0; x < 9; x++) {
            gui.setItem(18 + x, border);
        }

        gui.setItem(26, back);
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