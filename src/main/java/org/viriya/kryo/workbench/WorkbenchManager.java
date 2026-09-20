package org.viriya.kryo.workbench;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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
import org.viriya.kryo.blueprint.BlueprintItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkbenchManager implements Listener {

    private static NamespacedKey workbenchKey;
    private static final Set<Location> activeWorkbenches = new HashSet<>();
    private static final Component GUI_TITLE = Component.text("Workbench", NamedTextColor.DARK_GRAY);

    public static void init(Plugin plugin) {
        workbenchKey = new NamespacedKey(plugin, "custom_workbench");
        registerRecipe(plugin);

        ItemStack[] workbenchRecipe = {
                null, new ItemStack(Material.WOODEN_AXE), null,
                new ItemStack(Material.WOODEN_PICKAXE), new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.WOODEN_SHOVEL),
                null, new ItemStack(Material.WOODEN_HOE), null
        };

        BlueprintItem.registerToGroup("MACHINE", getWorkbenchItem(), new ItemStack(Material.CRAFTING_TABLE), workbenchRecipe);
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

        activeWorkbenches.add(event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (activeWorkbenches.contains(loc)) {
            activeWorkbenches.remove(loc);

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
        if (activeWorkbenches.contains(loc)) {
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