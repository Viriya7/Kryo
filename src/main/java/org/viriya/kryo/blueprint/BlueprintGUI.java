package org.viriya.kryo.blueprint;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.viriya.kryo.api.KryoRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlueprintGUI implements Listener {

    private static final Component MAIN_TITLE = Component.text("Blueprint", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, false);
    private static final Component SETTINGS_TITLE = Component.text("Settings", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false);

    private static final Map<UUID, BlueprintGroup> openSubMenus = new HashMap<>();

    public static void openMenu(Player player) {
        openSubMenus.remove(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 54, MAIN_TITLE);

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
        int slotIndex = 9;

        for (int i = 0; i < groups.size() && slotIndex < 45; i++) {
            gui.setItem(slotIndex++, groups.get(i).getIconItem());
        }

        player.openInventory(gui);
    }

    public static void openGroupMenu(Player player, BlueprintGroup group) {
        openSubMenus.put(player.getUniqueId(), group);
        Component title = group.getName().decoration(TextDecoration.BOLD, false);
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

        gui.setItem(1, backButton);

        List<ItemStack> items = group.getItems();
        int slotIndex = 9;

        for (int i = 0; i < items.size() && slotIndex < 45; i++) {
            gui.setItem(slotIndex++, items.get(i));
        }

        player.openInventory(gui);
    }

    public static void openSettings(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, SETTINGS_TITLE);

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Component viewTitle = event.getView().title();
        boolean isMain = viewTitle.equals(MAIN_TITLE);
        boolean isSettings = viewTitle.equals(SETTINGS_TITLE);
        boolean isSub = openSubMenus.containsKey(player.getUniqueId());

        if (!isMain && !isSettings && !isSub) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (isMain) {
            if (clickedItem.getType() == Material.REPEATER) {
                openSettings(player);
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            for (BlueprintGroup group : KryoRegistry.getGroups()) {
                Component groupName = group.getName();
                Component itemDisplayName = meta.displayName();

                if (group.getIconItem().isSimilar(clickedItem) ||
                        (itemDisplayName != groupName && itemDisplayName != null && itemDisplayName.equals(groupName))) {
                    openGroupMenu(player, group);
                    break;
                }
            }
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            openMenu(player);
        }
    }
}