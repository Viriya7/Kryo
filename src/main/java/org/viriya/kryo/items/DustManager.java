package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;

public class DustManager {

    private static NamespacedKey dustKey;

    public static void init(Plugin plugin) {
        dustKey = new NamespacedKey(plugin, "custom_dust_type");
        registerDustRecipes();
    }

    public static ItemStack createDust(String dustName, Material material, String dustId) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamedTextColor textColor = (material == Material.GLOWSTONE_DUST) ? NamedTextColor.GOLD : NamedTextColor.WHITE;

            meta.displayName(Component.text(dustName, textColor).decoration(TextDecoration.ITALIC, false));

            meta.getPersistentDataContainer().set(dustKey, PersistentDataType.STRING, dustId);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomDust(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(dustKey, PersistentDataType.STRING);
        return value != null;
    }

    private static void registerDustRecipes() {
        ItemStack sieveStation = SieveManager.getSieveItem();

        ItemStack ironDust = createDust("Iron Dust", Material.GUNPOWDER, "iron_dust");
        ItemStack[] ironRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", ironDust, sieveStation, ironRecipe);

        ItemStack goldDust = createDust("Gold Dust", Material.GLOWSTONE_DUST, "gold_dust");
        ItemStack[] goldRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", goldDust, sieveStation, goldRecipe);

        ItemStack copperDust = createDust("Copper Dust", Material.GLOWSTONE_DUST, "copper_dust");
        ItemStack[] copperRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", copperDust, sieveStation, copperRecipe);

        ItemStack carbonDust = createDust("Carbon Dust", Material.BLACK_DYE, "carbon_dust");
        ItemStack[] carbonRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", carbonDust, sieveStation, carbonRecipe);

        ItemStack leadDust = createDust("Lead Dust", Material.SUGAR, "lead_dust");
        ItemStack[] leadRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", leadDust, sieveStation, leadRecipe);

        ItemStack tinDust = createDust("Tin Dust", Material.SUGAR, "tin_dust");
        ItemStack[] tinRecipe = {
                null, null, null,
                null, new ItemStack(Material.GRAVEL), null,
                null, null, null
        };
        BlueprintItem.registerToGroup("RESOURCES", tinDust, sieveStation, tinRecipe);
    }
}