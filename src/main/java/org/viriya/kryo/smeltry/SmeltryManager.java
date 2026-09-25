package org.viriya.kryo.smeltry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlastFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.items.DustManager;
import org.viriya.kryo.items.IngotManager;
import org.viriya.kryo.items.MercuryManager;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SmeltryManager implements Listener {

    private static NamespacedKey smeltryKey;
    private static NamespacedKey karatKey;
    private static NamespacedKey invDataKey;
    private static Plugin pluginInstance;

    private static final Map<Location, Inventory> smeltryInventories = new HashMap<>();
    private static final Map<Inventory, Integer> smeltingProgress = new HashMap<>();

    private static final int[] INPUT_SLOTS = {
            10, 11, 12, 13,
            19, 20, 21, 22
    };

    private static final int[] OUTPUT_SLOTS = {
            15, 16,
            24, 25,
            33, 34,
            42, 43
    };

    private static final int[] FUEL_SLOTS = {
            37, 38, 39, 40
    };

    private static final int INDICATOR_SLOT = 32;

    private static final int TOTAL_TICKS = 60;

    @SuppressWarnings("unused")
    public static void init(Plugin plugin) {
        pluginInstance = plugin;
        smeltryKey = new NamespacedKey(plugin, "custom_smeltry_block");
        karatKey = new NamespacedKey(plugin, "gold_karat_value");
        invDataKey = new NamespacedKey(plugin, "smeltry_inventory_data");
        registerSmeltryRecipe();
        startSmeltryTask();
    }

    public static ItemStack getSmeltryItem() {
        ItemStack item = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Smeltry", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(smeltryKey, PersistentDataType.STRING, "smeltry_block");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void registerSmeltryRecipe() {
        if (pluginInstance == null) return;

        ItemStack workbenchStation = WorkbenchManager.getWorkbenchItem();
        ItemStack ironIngot = new ItemStack(Material.IRON_INGOT);
        ItemStack cobblestone = new ItemStack(Material.COBBLESTONE);
        ItemStack result = getSmeltryItem();

        ItemStack[] recipe = {
                cobblestone, cobblestone, cobblestone,
                cobblestone, ironIngot,   cobblestone,
                cobblestone, cobblestone, cobblestone
        };

        WorkbenchManager.registerCustomRecipe(result, recipe);
        BlueprintItem.registerToGroup("MACHINE", result, workbenchStation, recipe);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) return;
        String id = item.getItemMeta().getPersistentDataContainer().get(smeltryKey, PersistentDataType.STRING);
        if (id != null && id.equals("smeltry_block")) {
            Block block = event.getBlockPlaced();
            if (block.getState() instanceof BlastFurnace blastFurnace) {
                blastFurnace.getPersistentDataContainer().set(smeltryKey, PersistentDataType.STRING, "smeltry_block");
                blastFurnace.update();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.BLAST_FURNACE) {
            if (block.getState() instanceof BlastFurnace blastFurnace) {
                if (blastFurnace.getPersistentDataContainer().has(smeltryKey, PersistentDataType.STRING)) {
                    Location loc = block.getLocation();

                    event.setDropItems(false);
                    block.getWorld().dropItemNaturally(loc, getSmeltryItem());

                    Inventory inv = smeltryInventories.remove(loc);
                    if (inv != null) {
                        for (int i = 0; i < inv.getSize(); i++) {
                            if (isFunctionalSlot(i) && i != INDICATOR_SLOT) {
                                ItemStack item = inv.getItem(i);
                                if (item != null && item.getType() != Material.AIR) {
                                    block.getWorld().dropItemNaturally(loc, item);
                                }
                            }
                        }
                        smeltingProgress.remove(inv);
                    } else {
                        String serializedInv = blastFurnace.getPersistentDataContainer().get(invDataKey, PersistentDataType.STRING);
                        if (serializedInv != null && !serializedInv.isEmpty()) {
                            Inventory tempInv = Bukkit.createInventory(null, 54);
                            deserializeInventory(tempInv, serializedInv);
                            for (int i = 0; i < tempInv.getSize(); i++) {
                                if (isFunctionalSlot(i) && i != INDICATOR_SLOT) {
                                    ItemStack item = tempInv.getItem(i);
                                    if (item != null && item.getType() != Material.AIR) {
                                        block.getWorld().dropItemNaturally(loc, item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.BLAST_FURNACE) {
                if (block.getState() instanceof BlastFurnace blastFurnace) {
                    if (blastFurnace.getPersistentDataContainer().has(smeltryKey, PersistentDataType.STRING)) {
                        event.setCancelled(true);
                        openSmeltryGUI(event.getPlayer(), block.getLocation());
                    }
                }
            }
        }
    }

    public static void openSmeltryGUI(Player player, Location loc) {
        Inventory inv = smeltryInventories.computeIfAbsent(loc, k -> {
            Inventory newInv = Bukkit.createInventory(new SmeltryHolder(), 54, Component.text("Smeltry", NamedTextColor.DARK_GRAY));

            Block block = loc.getBlock();
            if (block.getState() instanceof BlastFurnace blastFurnace) {
                PersistentDataContainer pdc = blastFurnace.getPersistentDataContainer();
                String serializedInv = pdc.get(invDataKey, PersistentDataType.STRING);
                deserializeInventory(newInv, serializedInv);
            } else {
                deserializeInventory(newInv, null);
            }

            smeltingProgress.put(newInv, 0);
            return newInv;
        });

        player.openInventory(inv);
    }

    private static boolean isFunctionalSlot(int slot) {
        for (int s : INPUT_SLOTS) if (s == slot) return true;
        for (int s : OUTPUT_SLOTS) if (s == slot) return true;
        for (int s : FUEL_SLOTS) if (s == slot) return true;
        return slot == INDICATOR_SLOT;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SmeltryHolder) {
            int slot = event.getRawSlot();

            if (slot == INDICATOR_SLOT) {
                event.setCancelled(true);
                return;
            }

            if (!isFunctionalSlot(slot) && slot < 54) {
                event.setCancelled(true);
            }

            saveInventoryToBlock(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SmeltryHolder) {
            saveInventoryToBlock(event.getInventory());
        }
    }

    private static void saveInventoryToBlock(Inventory inv) {
        for (Map.Entry<Location, Inventory> entry : smeltryInventories.entrySet()) {
            if (entry.getValue().equals(inv)) {
                Block block = entry.getKey().getBlock();
                if (block.getState() instanceof BlastFurnace blastFurnace) {
                    String serialized = serializeInventory(inv);
                    blastFurnace.getPersistentDataContainer().set(invDataKey, PersistentDataType.STRING, serialized);
                    blastFurnace.update();
                }
                break;
            }
        }
    }

    private static String serializeInventory(Inventory inv) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isFunctionalSlot(i) && i != INDICATOR_SLOT) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    sb.append(i).append(":AIR;");
                } else {
                    String encoded = Base64.getEncoder().encodeToString(item.serializeAsBytes());
                    sb.append(i).append(":").append(encoded).append(";");
                }
            }
        }
        return sb.toString();
    }

    private static void deserializeInventory(Inventory inv, String data) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            border.setItemMeta(meta);
        }
        for (int i = 0; i < 54; i++) {
            if (!isFunctionalSlot(i)) {
                inv.setItem(i, border);
            }
        }

        ItemStack indicator = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta indMeta = indicator.getItemMeta();
        if (indMeta != null) {
            indMeta.displayName(Component.text("Smeltry Status: Idle", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            indicator.setItemMeta(indMeta);
        }
        inv.setItem(INDICATOR_SLOT, indicator);

        for (int s : INPUT_SLOTS) inv.setItem(s, null);
        for (int s : OUTPUT_SLOTS) inv.setItem(s, null);
        for (int s : FUEL_SLOTS) inv.setItem(s, null);

        if (data == null || data.isEmpty()) return;

        String[] entries = data.split(";");
        for (String entry : entries) {
            if (entry.contains(":")) {
                String[] parts = entry.split(":", 2);
                try {
                    int slot = Integer.parseInt(parts[0]);
                    String itemData = parts[1];

                    if (!itemData.equals("AIR")) {
                        byte[] bytes = Base64.getDecoder().decode(itemData);
                        ItemStack item = ItemStack.deserializeBytes(bytes);
                        inv.setItem(slot, item);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void startSmeltryTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Inventory> entry : smeltryInventories.entrySet()) {
                    Inventory inv = entry.getValue();
                    processSmeltryTick(inv);
                    saveInventoryToBlock(inv);
                }
            }
        }.runTaskTimer(pluginInstance, 0L, 20L);
    }

    private static void processSmeltryTick(Inventory inv) {
        int fuelSlot = -1;
        for (int slot : FUEL_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && (item.getType() == Material.COAL || item.getType() == Material.COAL_BLOCK)) {
                fuelSlot = slot;
                break;
            }
        }

        ItemStack indicatorItem = inv.getItem(INDICATOR_SLOT);
        if (indicatorItem == null || indicatorItem.getType() != Material.FLINT_AND_STEEL) return;

        if (fuelSlot == -1) {
            setIndicatorProgress(indicatorItem, 0, "Status: No Fuel!", NamedTextColor.RED);
            smeltingProgress.put(inv, 0);
            return;
        }

        int targetInputSlot = -1;
        ItemStack sourceItem = null;
        for (int slot : INPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && (DustManager.isCustomDust(item) || isKaratGoldIngot(item) || MercuryManager.isCustomCinnabar(item))) {
                targetInputSlot = slot;
                sourceItem = item;
                break;
            }
        }

        if (sourceItem == null) {
            setIndicatorProgress(indicatorItem, 0, "Smeltry Status: Idle", NamedTextColor.YELLOW);
            smeltingProgress.put(inv, 0);
            return;
        }

        SmeltResult smeltResult = getSmeltResult(inv, targetInputSlot, sourceItem);
        if (smeltResult == null) {
            setIndicatorProgress(indicatorItem, 0, "Status: Invalid Recipe!", NamedTextColor.RED);
            smeltingProgress.put(inv, 0);
            return;
        }

        if (!canFitOutput(inv, smeltResult.result)) {
            setIndicatorProgress(indicatorItem, 0, "Status: Output Full!", NamedTextColor.RED);
            smeltingProgress.put(inv, 0);
            return;
        }

        int currentProgress = smeltingProgress.getOrDefault(inv, 0) + 1;
        int maxDurability = indicatorItem.getType().getMaxDurability();
        int currentDamage = maxDurability - (int) ((double) currentProgress / TOTAL_TICKS * maxDurability);

        setIndicatorProgress(indicatorItem, Math.max(0, Math.min(maxDurability, currentDamage)), "Smeltry Status: Smelting...", NamedTextColor.GREEN);

        if (currentProgress >= TOTAL_TICKS) {
            ItemStack fuelItem = inv.getItem(fuelSlot);
            if (fuelItem != null) {
                fuelItem.setAmount(fuelItem.getAmount() - 1);
                if (fuelItem.getAmount() <= 0) {
                    inv.setItem(fuelSlot, null);
                }
            }

            sourceItem.setAmount(sourceItem.getAmount() - 1);
            if (sourceItem.getAmount() <= 0) {
                inv.setItem(targetInputSlot, null);
            }

            if (smeltResult.secondarySlot != -1) {
                ItemStack secItem = inv.getItem(smeltResult.secondarySlot);
                if (secItem != null) {
                    secItem.setAmount(secItem.getAmount() - 1);
                    if (secItem.getAmount() <= 0) {
                        inv.setItem(smeltResult.secondarySlot, null);
                    }
                }
            }

            addToOutput(inv, smeltResult.result);
            smeltingProgress.put(inv, 0);
        } else {
            smeltingProgress.put(inv, currentProgress);
        }
    }

    private static SmeltResult getSmeltResult(Inventory inv, int primarySlot, ItemStack sourceItem) {
        // Cek jika inputnya adalah Cinnabar
        if (MercuryManager.isCustomCinnabar(sourceItem)) {
            return new SmeltResult(MercuryManager.getMercury(), -1);
        }

        if (isKaratGoldIngot(sourceItem)) {
            int currentKarat = getKaratValue(sourceItem);
            if (currentKarat < 24) {
                int nextKarat = currentKarat + 2;
                for (int slot : INPUT_SLOTS) {
                    if (slot == primarySlot) continue;
                    ItemStack item = inv.getItem(slot);
                    if (item != null && "gold_dust".equals(getDustId(item))) {
                        ItemStack nextGoldIngot = IngotManager.createGoldKaratIngot(nextKarat);
                        return new SmeltResult(nextGoldIngot, slot);
                    }
                }
            }
        }

        if (DustManager.isCustomDust(sourceItem) && "gold_dust".equals(getDustId(sourceItem))) {
            for (int slot : INPUT_SLOTS) {
                if (slot == primarySlot) continue;
                ItemStack item = inv.getItem(slot);
                if (isKaratGoldIngot(item)) {
                    int currentKarat = getKaratValue(item);
                    if (currentKarat < 24) {
                        int nextKarat = currentKarat + 2;
                        ItemStack nextGoldIngot = IngotManager.createGoldKaratIngot(nextKarat);
                        return new SmeltResult(nextGoldIngot, slot);
                    }
                }
            }
        }

        if (DustManager.isCustomDust(sourceItem)) {
            String dustId = getDustId(sourceItem);
            ItemStack result = null;
            switch (dustId) {
                case "iron_dust":
                    result = new ItemStack(Material.IRON_INGOT);
                    break;
                case "copper_dust":
                    result = IngotManager.getCopperIngot();
                    break;
                case "lead_dust":
                    result = IngotManager.getLeadIngot();
                    break;
                case "tin_dust":
                    result = IngotManager.getTinIngot();
                    break;
                case "gold_dust":
                    result = IngotManager.createGoldKaratIngot(4);
                    break;
                default:
                    break;
            }

            if (result != null) {
                return new SmeltResult(result, -1);
            }
        }

        return null;
    }

    private static class SmeltResult {
        ItemStack result;
        int secondarySlot;

        public SmeltResult(ItemStack result, int secondarySlot) {
            this.result = result;
            this.secondarySlot = secondarySlot;
        }
    }

    private static boolean isKaratGoldIngot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(karatKey, PersistentDataType.INTEGER);
    }

    private static int getKaratValue(ItemStack item) {
        if (!isKaratGoldIngot(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer value = meta.getPersistentDataContainer().get(karatKey, PersistentDataType.INTEGER);
        return value != null ? value : 0;
    }

    private static void setIndicatorProgress(ItemStack item, int damage, String statusText, NamedTextColor color) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
        }
        if (meta != null) {
            meta.displayName(Component.text(statusText, color).decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
    }

    private static String getDustId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "";
        String type = meta.getPersistentDataContainer().get(
                new NamespacedKey(pluginInstance, "custom_dust_type"),
                PersistentDataType.STRING
        );
        return type != null ? type : "";
    }

    private static boolean canFitOutput(Inventory inv, ItemStack result) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item == null || (item.isSimilar(result) && item.getAmount() + result.getAmount() <= item.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private static void addToOutput(Inventory inv, ItemStack result) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                inv.setItem(slot, result.clone());
                return;
            } else if (item.isSimilar(result) && item.getAmount() + result.getAmount() <= item.getMaxStackSize()) {
                item.setAmount(item.getAmount() + result.getAmount());
                return;
            }
        }
    }

    private static class SmeltryHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}