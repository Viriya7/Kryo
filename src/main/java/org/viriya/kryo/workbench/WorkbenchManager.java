package org.viriya.kryo.workbench;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.items.SieveManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WorkbenchManager implements Listener {

    private static NamespacedKey workbenchKey;
    private static Plugin pluginInstance;
    private static final Map<Location, Inventory> activeWorkbenchInventories = new HashMap<>();

    private static final Map<String, Supplier<ItemStack>> registeredRecipeSuppliers = new HashMap<>();
    private static final Map<String, Material[]> recipeGridMap = new HashMap<>();

    public static void init(Plugin plugin) {
        pluginInstance = plugin;
        if (workbenchKey == null) {
            workbenchKey = new NamespacedKey(plugin, "custom_workbench");
        }
        registerRecipe(plugin);

        ItemStack[] workbenchRecipe = {
                null, new ItemStack(Material.WOODEN_AXE), null,
                new ItemStack(Material.WOODEN_PICKAXE), new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.WOODEN_SHOVEL),
                null, new ItemStack(Material.WOODEN_HOE), null
        };
        registerCustomRecipe(WorkbenchManager::getWorkbenchItem, workbenchRecipe);
        BlueprintItem.registerToGroup("MACHINE", getWorkbenchItem(), new ItemStack(Material.CRAFTING_TABLE), workbenchRecipe);

        ItemStack[] sieveRecipe = {
                null, null, null,
                new ItemStack(Material.OAK_PLANKS), null, new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS)
        };
        registerCustomRecipe(SieveManager::getSieveItem, sieveRecipe);
        BlueprintItem.registerToGroup("TOOLS", SieveManager.getSieveItem(), getWorkbenchItem(), sieveRecipe);
    }

    public static void registerCustomRecipe(Supplier<ItemStack> resultSupplier, ItemStack[] grid) {
        if (grid.length != 9) return;
        Material[] mats = new Material[9];
        for (int i = 0; i < 9; i++) {
            mats[i] = (grid[i] == null) ? Material.AIR : grid[i].getType();
        }
        String key = "recipe_" + recipeGridMap.size();
        recipeGridMap.put(key, mats);
        registeredRecipeSuppliers.put(key, resultSupplier);
    }

    public static void registerCustomRecipe(ItemStack result, ItemStack[] grid) {
        registerCustomRecipe(() -> result, grid);
    }

    public static ItemStack getWorkbenchItem() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Workbench", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Where the magic begins", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            if (workbenchKey != null) {
                meta.getPersistentDataContainer().set(workbenchKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomWorkbench(ItemStack item) {
        if (item == null || !item.hasItemMeta() || workbenchKey == null) return false;
        ItemMeta meta = item.getItemMeta();
        Byte value = meta.getPersistentDataContainer().get(workbenchKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static boolean isCustomWorkbenchBlock(Block block) {
        if (block == null || block.getType() != Material.BARREL || workbenchKey == null) return false;
        if (block.getState() instanceof TileState tileState) {
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            Byte value = pdc.get(workbenchKey, PersistentDataType.BYTE);
            return value != null && value == 1;
        }
        return false;
    }

    private static void registerRecipe(Plugin plugin) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "workbench_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, getWorkbenchItem());
        recipe.shape(" A ", "PCS", " H ");
        recipe.setIngredient('A', Material.WOODEN_AXE);
        recipe.setIngredient('P', Material.WOODEN_PICKAXE);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        recipe.setIngredient('S', Material.WOODEN_SHOVEL);
        recipe.setIngredient('H', Material.WOODEN_HOE);
        Bukkit.addRecipe(recipe);
    }

    public static void openWorkbenchGUI(Player player, Location loc) {
        Inventory gui = activeWorkbenchInventories.computeIfAbsent(loc, k -> {
            Inventory newGui = Bukkit.createInventory(null, 27, Component.text("Workbench", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false));

            ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = border.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(" "));
                border.setItemMeta(meta);
            }

            for (int i = 0; i < 27; i++) {
                if (isBorderSlot(i)) {
                    newGui.setItem(i, border);
                }
            }
            return newGui;
        });

        player.openInventory(gui);
    }

    private static boolean isBorderSlot(int i) {
        int row = i / 9;
        int col = i % 9;

        if (col >= 1 && col <= 3) {
            return false;
        }

        if (row == 1 && col == 6) {
            return false;
        }

        return true;
    }

    private static void updateCraftingOutput(Inventory inv) {
        ItemStack[] grid = getGridItems(inv);
        Supplier<ItemStack> matchedSupplier = null;

        for (Map.Entry<String, Material[]> entry : recipeGridMap.entrySet()) {
            Material[] recipeMats = entry.getValue();
            boolean match = true;
            for (int i = 0; i < 9; i++) {
                Material slotMat = (grid[i] == null || grid[i].getType() == Material.AIR) ? Material.AIR : grid[i].getType();
                if (slotMat != recipeMats[i]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matchedSupplier = registeredRecipeSuppliers.get(entry.getKey());
                break;
            }
        }

        if (matchedSupplier != null) {
            inv.setItem(15, matchedSupplier.get());
        } else {
            inv.setItem(15, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("Workbench", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        Inventory inv = event.getInventory();
        int rawSlot = event.getRawSlot();

        if (rawSlot == 15) {
            event.setCancelled(true);
            if (clicked != null && clicked.getType() != Material.AIR) {
                ItemStack[] grid = getGridItems(inv);
                Supplier<ItemStack> matchedSupplier = null;

                for (Map.Entry<String, Material[]> entry : recipeGridMap.entrySet()) {
                    Material[] recipeMats = entry.getValue();
                    boolean match = true;
                    for (int i = 0; i < 9; i++) {
                        Material slotMat = (grid[i] == null || grid[i].getType() == Material.AIR) ? Material.AIR : grid[i].getType();
                        if (slotMat != recipeMats[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        matchedSupplier = registeredRecipeSuppliers.get(entry.getKey());
                        break;
                    }
                }

                if (matchedSupplier == null) return;
                ItemStack actualResult = matchedSupplier.get();

                for (int r = 0; r < 3; r++) {
                    for (int c = 1; c <= 3; c++) {
                        int slot = r * 9 + c;
                        ItemStack ingredient = inv.getItem(slot);
                        if (ingredient != null && ingredient.getType() != Material.AIR) {
                            ingredient.setAmount(ingredient.getAmount() - 1);
                            if (ingredient.getAmount() <= 0) {
                                inv.setItem(slot, null);
                            }
                        }
                    }
                }

                HashMap<Integer, ItemStack> leftover = event.getWhoClicked().getInventory().addItem(actualResult);
                for (ItemStack drop : leftover.values()) {
                    event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), drop);
                }

                inv.setItem(15, null);

                if (pluginInstance != null) {
                    Bukkit.getScheduler().runTaskLater(pluginInstance, () -> updateCraftingOutput(inv), 1L);
                }
            }
            return;
        }

        if (clicked != null && clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        if (pluginInstance != null) {
            Bukkit.getScheduler().runTaskLater(pluginInstance, () -> updateCraftingOutput(inv), 1L);
        }
    }

    private static ItemStack[] getGridItems(Inventory inv) {
        ItemStack[] grid = new ItemStack[9];
        int idx = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 1; c <= 3; c++) {
                int slot = r * 9 + c;
                grid[idx++] = inv.getItem(slot);
            }
        }
        return grid;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isCustomWorkbench(item)) return;

        Block block = event.getBlockPlaced();
        if (block.getState() instanceof TileState tileState && workbenchKey != null) {
            tileState.getPersistentDataContainer().set(workbenchKey, PersistentDataType.BYTE, (byte) 1);
            tileState.update();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isCustomWorkbenchBlock(block)) return;

        Location loc = block.getLocation();
        event.setDropItems(false);

        Inventory gui = activeWorkbenchInventories.remove(loc);
        if (gui != null) {
            for (int i = 0; i < gui.getSize(); i++) {
                ItemStack item = gui.getItem(i);
                if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    block.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), item);
                }
            }
        }

        block.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), getWorkbenchItem());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (isCustomWorkbenchBlock(block)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

            openWorkbenchGUI(event.getPlayer(), block.getLocation());
        }
    }
}