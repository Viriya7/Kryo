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

    public static void openMenu(Player player) {
        Component title = Component.text("Blueprint", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack settings = createGuiItem(Material.REPEATER, Component.text("Settings", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = y * 9 + x;
                if (y == 0 || y == 5) {
                    if (y == 0 && x == 1) {
                        gui.setItem(slot, settings);
                    } else {
                        gui.setItem(slot, border);
                    }
                }
            }
        }

        List<BlueprintGroup> groups = KryoRegistry.getGroups();
        int slotIndex = 9; // Mulai dari baris ke-2, kolom ke-0 (x=0, y=1)

        for (int i = 0; i < groups.size() && slotIndex < 45; i++) {
            gui.setItem(slotIndex++, groups.get(i).getIconItem());
        }

        player.openInventory(gui);
    }

    public static void openGroupMenu(Player player, BlueprintGroup group) {
        Component title = group.getName().decoration(TextDecoration.BOLD, true);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack backButton = createGuiItem(Material.BARRIER, Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                int slot = y * 9 + x;
                if (y == 0 || y == 5) {
                    gui.setItem(slot, border);
                }
            }
        }

        // Tombol Back di y = 5, x = 7 -> Slot 52
        gui.setItem(52, backButton);

        List<ItemStack> items = group.getItems();
        int slotIndex = 9; // Mulai dari x = 0, y = 1

        for (int i = 0; i < items.size() && slotIndex < 45; i++) {
            gui.setItem(slotIndex++, items.get(i));
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