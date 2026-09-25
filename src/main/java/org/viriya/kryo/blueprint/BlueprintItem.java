package org.viriya.kryo.blueprint;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.viriya.kryo.api.KryoRegistry;

import java.util.HashMap;
import java.util.Map;

public class BlueprintItem {

    private static final Map<String, Map<ItemStack, ItemRecipe>> groupRecipes = new HashMap<>();

    public record ItemRecipe(ItemStack resultItem, ItemStack craftingStation, ItemStack[] recipeGrid) {
        public ItemRecipe {
            if (recipeGrid == null || recipeGrid.length != 9) {
                recipeGrid = new ItemStack[9];
            }
        }
    }

    public static void registerToGroup(String groupName, ItemStack resultItem, ItemStack craftingStation, ItemStack[] recipeGrid) {
        ItemRecipe recipe = new ItemRecipe(resultItem, craftingStation, recipeGrid);
        groupRecipes.computeIfAbsent(groupName.toUpperCase(), k -> new HashMap<>()).put(resultItem, recipe);

        BlueprintGroup group = KryoRegistry.getGroup(groupName);
        if (group != null) {
            group.addItem(resultItem);
        }
    }

    public static ItemRecipe getRecipe(String groupName, ItemStack resultItem) {
        Map<ItemStack, ItemRecipe> recipes = groupRecipes.get(groupName.toUpperCase());
        if (recipes != null) {
            return recipes.get(resultItem);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static void openBlueprintRecipeGUI(Player player, ItemRecipe itemRecipe) {
        Component title = Component.text("Blueprint", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, false);
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(" "));
            border.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(0, backButton);

        if (itemRecipe.craftingStation() != null && itemRecipe.craftingStation().getType() != Material.AIR) {
            gui.setItem(10, itemRecipe.craftingStation());
        }

        int[] recipeSlots = {3, 4, 5, 12, 13, 14, 21, 22, 23};
        ItemStack[] grid = itemRecipe.recipeGrid();

        for (int i = 0; i < 9; i++) {
            int targetSlot = recipeSlots[i];
            if (grid[i] != null && grid[i].getType() != Material.AIR) {
                gui.setItem(targetSlot, grid[i]);
            } else {
                gui.setItem(targetSlot, null);
            }
        }

        gui.setItem(16, itemRecipe.resultItem());

        player.openInventory(gui);
    }
}