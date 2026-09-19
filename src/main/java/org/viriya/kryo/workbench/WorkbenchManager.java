package org.viriya.kryo.workbench;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkbenchManager implements Listener {

    private static NamespacedKey workbenchKey;
    private static final Map<Location, ItemDisplay> activeWorkbenches = new HashMap<>();
    private static final Component GUI_TITLE = Component.text("Workbench", NamedTextColor.DARK_GRAY);

    public static void init(Plugin plugin) {
        workbenchKey = new NamespacedKey(plugin, "custom_workbench");
        registerRecipe(plugin);
    }

    public static ItemStack getWorkbenchItem() {
        ItemStack item = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Workbench"));

            meta.lore(List.of(
                    Component.text("Where the magic begins", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true)
            ));

            meta.getPersistentDataContainer().set(workbenchKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomWorkbench(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        Byte value = meta.getPersistentDataContainer().get(workbenchKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    private static void registerRecipe(Plugin plugin) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "workbench_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getWorkbenchItem());

        recipe.shape(
                " A ",
                "PCS",
                " H "
        );

        recipe.setIngredient('A', Material.WOODEN_AXE);
        recipe.setIngredient('P', Material.WOODEN_PICKAXE);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        recipe.setIngredient('S', Material.WOODEN_SHOVEL);
        recipe.setIngredient('H', Material.WOODEN_HOE);

        Bukkit.addRecipe(recipe);
    }

    public static void openWorkbenchGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Workbench", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, true));

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.displayName(GUI_TITLE);
            border.setItemMeta(meta);
        }

        // Mapping layout 3 baris x 9 kolom (0 sampai 26)
        // Baris 0: [b][i][i][i][b][b][b][b][b] -> Slot 0, 4,5,6,7,8 border | 1,2,3 input
        // Baris 1: [b][i][i][i][b][b][o][b][b] -> Slot 9, 13,14,15,16,17 border | 10,11,12 input | 15 adalah 'o' (indeks ke-15)
        // Baris 2: [b][i][i][i][b][b][b][b][b] -> Slot 18, 22,23,24,25,26 border | 19,20,21 input

        for (int i = 0; i < 27; i++) {
            boolean isBorder = false;

            int row = i / 9;
            int col = i % 9;

            if (col == 0 || col >= 4) {
                if (!(row == 1 && col == 6)) {
                    isBorder = true;
                }
            }

            if (isBorder) {
                gui.setItem(i, border);
            }
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isCustomWorkbench(item)) {
            return;
        }

        Block block = event.getBlockPlaced();
        Location loc = block.getLocation();

        Location displayLoc = loc.clone().add(0.5, 0.5, 0.5);
        ItemDisplay display = loc.getWorld().spawn(displayLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(new ItemStack(Material.SMITHING_TABLE));
            entity.setBrightness(new Display.Brightness(15, 15));
        });

        activeWorkbenches.put(loc, display);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (activeWorkbenches.containsKey(loc)) {
            ItemDisplay display = activeWorkbenches.remove(loc);
            if (display != null && display.isValid()) {
                display.remove();
            }

            event.setDropItems(false);
            block.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), getWorkbenchItem());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        if (activeWorkbenches.containsKey(loc)) {
            event.setCancelled(true);
            openWorkbenchGUI(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("Workbench", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, true))) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
        }
    }
}