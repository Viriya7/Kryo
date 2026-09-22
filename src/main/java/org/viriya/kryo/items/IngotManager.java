package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.items.SieveManager;
import org.viriya.kryo.workbench.WorkbenchManager;

public class IngotManager implements Listener {

    private static NamespacedKey ingotKey;
    private static NamespacedKey karatKey;
    private static Plugin pluginInstance;

    public static void init(Plugin plugin) {
        pluginInstance = plugin;
        ingotKey = new NamespacedKey(plugin, "custom_ingot_type");
        karatKey = new NamespacedKey(plugin, "gold_karat_value");
        registerIngotRecipes();
    }

    public static ItemStack createIngot(String ingotName, Material material, String ingotId, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(ingotName, color).decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(ingotKey, PersistentDataType.STRING, ingotId);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createGoldKaratIngot(int karat) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component nameComponent = Component.text("Gold Ingot ", NamedTextColor.GOLD)
                    .append(Component.text("(" + karat + "K)", NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false);

            meta.displayName(nameComponent);
            meta.getPersistentDataContainer().set(ingotKey, PersistentDataType.STRING, "gold_karat_" + karat);
            meta.getPersistentDataContainer().set(karatKey, PersistentDataType.INTEGER, karat);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomIngot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(ingotKey, PersistentDataType.STRING);
        return value != null;
    }

    public static ItemStack getCopperIngot() {
        return createIngot("Copper Ingot", Material.BRICK, "copper_ingot", NamedTextColor.GOLD);
    }

    public static ItemStack getLeadIngot() {
        return createIngot("Lead Ingot", Material.IRON_INGOT, "lead_ingot", NamedTextColor.GRAY);
    }

    public static ItemStack getTinIngot() {
        return createIngot("Tin Ingot", Material.IRON_INGOT, "tin_ingot", NamedTextColor.WHITE);
    }

    private static void registerIngotRecipes() {
        if (pluginInstance == null) return;

        ItemStack sieveStation = SieveManager.getSieveItem();
        ItemStack workbenchStation = WorkbenchManager.getWorkbenchItem();

        ItemStack copperIngot = getCopperIngot();
        ItemStack[] copperRecipe = {
                null, null, null,
                null, DustManager.createDust("Copper Dust", Material.GLOWSTONE_DUST, "copper_dust"), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", copperIngot, sieveStation, copperRecipe);

        ItemStack leadIngot = getLeadIngot();
        ItemStack[] leadRecipe = {
                null, null, null,
                null, DustManager.createDust("Lead Dust", Material.SUGAR, "lead_dust"), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", leadIngot, sieveStation, leadRecipe);

        ItemStack tinIngot = getTinIngot();
        ItemStack[] tinRecipe = {
                null, null, null,
                null, DustManager.createDust("Tin Dust", Material.SUGAR, "tin_dust"), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", tinIngot, sieveStation, tinRecipe);

        ItemStack goldDust = DustManager.createDust("Gold Dust", Material.GLOWSTONE_DUST, "gold_dust");

        for (int karat = 4; karat <= 24; karat += 2) {
            ItemStack[] recipe;
            ItemStack targetResult = createGoldKaratIngot(karat);

            if (karat == 4) {
                recipe = new ItemStack[] {
                        goldDust.clone(), null, null,
                        null, null, null,
                        null, null, null
                };
            } else {
                ItemStack previousGoldIngot = createGoldKaratIngot(karat - 2);
                recipe = new ItemStack[] {
                        goldDust.clone(), previousGoldIngot, null,
                        null, null, null,
                        null, null, null
                };
            }

            WorkbenchManager.registerCustomRecipe(targetResult, recipe);
            BlueprintItem.registerToGroup("RESOURCES", targetResult, workbenchStation, recipe);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();

        if (!DustManager.isCustomDust(source)) return;

        ItemMeta meta = source.getItemMeta();
        if (meta == null) return;

        String dustId = meta.getPersistentDataContainer().get(
                new NamespacedKey(pluginInstance, "custom_dust_type"),
                PersistentDataType.STRING
        );

        if (dustId == null) return;

        ItemStack result;

        switch (dustId) {
            case "iron_dust":
                result = new ItemStack(Material.IRON_INGOT);
                break;
            case "copper_dust":
                result = getCopperIngot();
                break;
            case "lead_dust":
                result = getLeadIngot();
                break;
            case "tin_dust":
                result = getTinIngot();
                break;
            case "gold_dust":
                result = new ItemStack(Material.GOLD_INGOT);
                break;
            case "carbon_dust":
                event.setCancelled(true);
                return;
            default:
                return;
        }

        event.setResult(result);
    }
}