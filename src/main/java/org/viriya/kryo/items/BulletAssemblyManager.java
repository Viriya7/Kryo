package org.viriya.kryo.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class BulletAssemblyManager implements Listener {

    private static NamespacedKey bulletTypeKey;
    private static NamespacedKey assemblyStationKey;
    private static NamespacedKey inventoryDataKey;
    private static final String GUI_TITLE = "Bullet Assembly Station";

    public static void init(Plugin plugin) {
        if (bulletTypeKey == null) {
            bulletTypeKey = new NamespacedKey(plugin, "custom_bullet_type");
        }
        if (assemblyStationKey == null) {
            assemblyStationKey = new NamespacedKey(plugin, "bullet_assembly_station");
        }
        if (inventoryDataKey == null) {
            inventoryDataKey = new NamespacedKey(plugin, "station_inventory");
        }
        plugin.getServer().getPluginManager().registerEvents(new BulletAssemblyManager(), plugin);
        registerRecipes();
    }

    public static ItemStack getAssemblyStation() {
        ItemStack item = new ItemStack(Material.DAYLIGHT_DETECTOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Bullet Assembly Station", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Used to assemble bullets with tips.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(assemblyStationKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getAssembledBullet(BulletManager.TipType tipType, int amount) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Bullet", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            switch (tipType) {
                case GOLD:
                    lore.add(Component.text("It's expensive", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    break;
                case COPPER:
                    lore.add(Component.text("Get oxides", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                    break;
                case TIN:
                    lore.add(Component.text("Stun for 3 seconds", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                    break;
                case IRON:
                    lore.add(Component.text("So sharp", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                    break;
                case MERCURY:
                    lore.add(Component.text("It's toxic to me", NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false));
                    break;
                case LEAD:
                default:
                    break;
            }
            if (!lore.isEmpty()) {
                meta.lore(lore);
            }

            meta.getPersistentDataContainer().set(bulletTypeKey, PersistentDataType.STRING, tipType.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static BulletManager.TipType getAssembledBulletType(ItemStack item) {
        if (item == null || !item.hasItemMeta() || bulletTypeKey == null) return null;
        String val = item.getItemMeta().getPersistentDataContainer().get(bulletTypeKey, PersistentDataType.STRING);
        if (val == null) return null;
        try {
            return BulletManager.TipType.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static ItemStack getCustomSkull(String base64Texture, String displayName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", base64Texture));
            meta.setPlayerProfile(profile);

            head.setItemMeta(meta);
        }
        return head;
    }

    public static void openAssemblyGUI(Player player, Block block) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(GUI_TITLE));

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text(""));
            border.setItemMeta(bMeta);
        }

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        if (block.getState() instanceof TileState state) {
            String serializedInv = state.getPersistentDataContainer().get(inventoryDataKey, PersistentDataType.STRING);
            if (serializedInv != null && !serializedInv.isEmpty()) {
                ItemStack[] items = itemStackArrayFromBase64(serializedInv);
                if (items != null) {
                    if (items.length > 10) gui.setItem(10, items[10]);
                    if (items.length > 12) gui.setItem(12, items[12]);
                    if (items.length > 14) gui.setItem(14, items[14]);
                    if (items.length > 16) gui.setItem(16, items[16]);
                }
            } else {
                gui.setItem(10, null);
                gui.setItem(12, null);
                gui.setItem(14, null);
                gui.setItem(16, null);
            }
        }

        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";
        gui.setItem(15, getCustomSkull(texture, "Assemble Bullet"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(assemblyStationKey, PersistentDataType.BYTE)) {
            Block block = event.getBlockPlaced();
            if (block.getState() instanceof TileState state) {
                state.getPersistentDataContainer().set(assemblyStationKey, PersistentDataType.BYTE, (byte) 1);
                state.update();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof TileState state) {
            if (state.getPersistentDataContainer().has(assemblyStationKey, PersistentDataType.BYTE)) {
                event.setDropItems(false);

                String serializedInv = state.getPersistentDataContainer().get(inventoryDataKey, PersistentDataType.STRING);
                if (serializedInv != null && !serializedInv.isEmpty()) {
                    ItemStack[] items = itemStackArrayFromBase64(serializedInv);
                    if (items != null) {
                        if (items.length > 10 && items[10] != null && !items[10].getType().isAir()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), items[10]);
                        }
                        if (items.length > 12 && items[12] != null && !items[12].getType().isAir()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), items[12]);
                        }
                        if (items.length > 14 && items[14] != null && !items[14].getType().isAir()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), items[14]);
                        }
                        if (items.length > 16 && items[16] != null && !items[16].getType().isAir()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), items[16]);
                        }
                    }
                }

                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), getAssemblyStation());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getState() instanceof TileState state) {
            if (state.getPersistentDataContainer().has(assemblyStationKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                openAssemblyGUI(event.getPlayer(), block);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(GUI_TITLE))) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        if (slot == 10 || slot == 12 || slot == 14 || slot == 16) {
            return;
        }

        event.setCancelled(true);

        if (slot == 15 && event.getWhoClicked() instanceof Player player) {
            Inventory inv = event.getInventory();
            ItemStack emptyBullet = inv.getItem(10);
            ItemStack gunpowder = inv.getItem(12);
            ItemStack tipItem = inv.getItem(14);

            if (emptyBullet != null && gunpowder != null && tipItem != null) {
                BulletManager.TipType tipType = BulletManager.getTipType(tipItem);
                if (tipType != null) {
                    int craftAmount = Math.min(emptyBullet.getAmount(), Math.min(gunpowder.getAmount(), tipItem.getAmount()));

                    if (craftAmount > 0) {
                        emptyBullet.setAmount(emptyBullet.getAmount() - craftAmount);
                        gunpowder.setAmount(gunpowder.getAmount() - craftAmount);
                        tipItem.setAmount(tipItem.getAmount() - craftAmount);

                        ItemStack result = getAssembledBullet(tipType, craftAmount);
                        ItemStack currentOutput = inv.getItem(16);

                        if (currentOutput == null || currentOutput.getType().isAir()) {
                            inv.setItem(16, result);
                        } else if (currentOutput.isSimilar(result)) {
                            int newAmount = currentOutput.getAmount() + result.getAmount();
                            if (newAmount <= result.getMaxStackSize()) {
                                currentOutput.setAmount(newAmount);
                            } else {
                                result.setAmount(newAmount - result.getMaxStackSize());
                                currentOutput.setAmount(result.getMaxStackSize());
                                player.getInventory().addItem(result);
                            }
                        } else {
                            player.getInventory().addItem(result);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().title().equals(Component.text(GUI_TITLE))) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock != null && targetBlock.getState() instanceof TileState state) {
            if (state.getPersistentDataContainer().has(assemblyStationKey, PersistentDataType.BYTE)) {
                Inventory inv = event.getInventory();
                ItemStack[] itemsToSave = new ItemStack[27];
                itemsToSave[10] = inv.getItem(10);
                itemsToSave[12] = inv.getItem(12);
                itemsToSave[14] = inv.getItem(14);
                itemsToSave[16] = inv.getItem(16);

                state.getPersistentDataContainer().set(inventoryDataKey, PersistentDataType.STRING, itemStackArrayToBase64(itemsToSave));
                state.update();
            }
        }
    }

    private static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private static ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            return null;
        }
    }

    private static void registerRecipes() {
        ItemStack[] stationGrid = new ItemStack[9];
        stationGrid[1] = MercuryManager.getMercury();
        stationGrid[4] = new ItemStack(Material.ANVIL);
        stationGrid[6] = new ItemStack(Material.IRON_INGOT);
        stationGrid[7] = new ItemStack(Material.IRON_INGOT);
        stationGrid[8] = new ItemStack(Material.IRON_INGOT);

        ItemStack resultStation = getAssemblyStation();
        WorkbenchManager.registerCustomRecipe(resultStation, stationGrid);
        BlueprintItem.registerToGroup("FIREARM", resultStation, WorkbenchManager.getWorkbenchItem(), stationGrid);

        BulletManager.TipType defaultType = BulletManager.TipType.LEAD;
        ItemStack[] blueprintGrid = new ItemStack[9];
        blueprintGrid[3] = BulletManager.getEmptyBullet(1);
        blueprintGrid[4] = new ItemStack(Material.GUNPOWDER);
        blueprintGrid[5] = BulletManager.getBulletTip(defaultType, 1);

        ItemStack resultBlueprint = getAssembledBullet(defaultType, 1);
        BlueprintItem.registerToGroup("FIREARM", resultBlueprint, getAssemblyStation(), blueprintGrid);
    }
}