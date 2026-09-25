package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.util.ArrayList;
import java.util.List;

public class BulletManager implements Listener {

    private static NamespacedKey emptyBulletKey;
    private static NamespacedKey tipTypeKey;

    public enum TipType {
        GOLD("gold_tip", Material.GOLD_NUGGET),
        COPPER("copper_tip", Material.GOLD_NUGGET),
        TIN("tin_tip", Material.GOLD_NUGGET),
        LEAD("lead_tip", Material.GOLD_NUGGET),
        IRON("iron_tip", Material.GOLD_NUGGET),
        MERCURY("mercury_tip", Material.GOLD_NUGGET);

        private final String keyName;
        private final Material material;

        TipType(String keyName, Material material) {
            this.keyName = keyName;
            this.material = material;
        }
    }

    public static void init(Plugin plugin) {
        if (emptyBulletKey == null) {
            emptyBulletKey = new NamespacedKey(plugin, "custom_empty_bullet");
        }
        if (tipTypeKey == null) {
            tipTypeKey = new NamespacedKey(plugin, "bullet_tip_type");
        }
        registerRecipes();
    }

    public static ItemStack getEmptyBullet(int amount) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Empty Bullet", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            if (emptyBulletKey != null) {
                meta.getPersistentDataContainer().set(emptyBulletKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getBulletTip(TipType type, int amount) {
        ItemStack item = new ItemStack(type.material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Bullet Tip", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            switch (type) {
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

            if (tipTypeKey != null) {
                meta.getPersistentDataContainer().set(tipTypeKey, PersistentDataType.STRING, type.keyName);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomEmptyBullet(ItemStack item) {
        if (item == null || !item.hasItemMeta() || emptyBulletKey == null) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(emptyBulletKey, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    public static TipType getTipType(ItemStack item) {
        if (item == null || !item.hasItemMeta() || tipTypeKey == null) return null;
        String val = item.getItemMeta().getPersistentDataContainer().get(tipTypeKey, PersistentDataType.STRING);
        if (val == null) return null;

        for (TipType type : TipType.values()) {
            if (type.keyName.equals(val)) return type;
        }
        return null;
    }

    private static void registerRecipes() {
        // --- Recipes for Empty Bullet ---
        ItemStack[] emptyBulletGrid = new ItemStack[9];
        emptyBulletGrid[0] = IngotManager.getLeadIngot();
        emptyBulletGrid[2] = IngotManager.getLeadIngot();
        emptyBulletGrid[3] = IngotManager.getLeadIngot();
        emptyBulletGrid[5] = IngotManager.getLeadIngot();
        emptyBulletGrid[6] = IngotManager.getLeadIngot();
        emptyBulletGrid[7] = IngotManager.getLeadIngot();
        emptyBulletGrid[8] = IngotManager.getLeadIngot();

        ItemStack emptyBulletWorkbenchResult = getEmptyBullet(8);
        WorkbenchManager.registerCustomRecipe(emptyBulletWorkbenchResult, emptyBulletGrid);

        ItemStack emptyBulletBlueprintResult = getEmptyBullet(1);
        BlueprintItem.registerToGroup("FIREARM", emptyBulletBlueprintResult, WorkbenchManager.getWorkbenchItem(), emptyBulletGrid);

        for (TipType type : TipType.values()) {
            ItemStack centerItem;
            switch (type) {
                case GOLD:
                    centerItem = IngotManager.createGoldKaratIngot(24);
                    break;
                case COPPER:
                    centerItem = IngotManager.getCopperIngot();
                    break;
                case TIN:
                    centerItem = IngotManager.getTinIngot();
                    break;
                case LEAD:
                    centerItem = IngotManager.getLeadIngot();
                    break;
                case IRON:
                    centerItem = new ItemStack(Material.IRON_INGOT);
                    break;
                case MERCURY:
                    centerItem = MercuryManager.getMercury();
                    break;
                default:
                    continue;
            }

            ItemStack[] tipGrid = new ItemStack[9];
            ItemStack lead = IngotManager.getLeadIngot();

            tipGrid[1] = lead;
            tipGrid[3] = lead;
            tipGrid[4] = centerItem; // 'c'
            tipGrid[5] = lead;

            ItemStack tipWorkbenchResult = getBulletTip(type, 8);
            WorkbenchManager.registerCustomRecipe(tipWorkbenchResult, tipGrid);

            ItemStack tipBlueprintResult = getBulletTip(type, 1);
            BlueprintItem.registerToGroup("FIREARM", tipBlueprintResult, WorkbenchManager.getWorkbenchItem(), tipGrid);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity)) return;
        LivingEntity livingVictim = (LivingEntity) victim;

        if (event.getDamager() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getDamager();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            TipType type = getTipType(itemInHand);
            if (type == null) return;

            switch (type) {
                case GOLD:
                    livingVictim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 1200, 0, false, true));
                    break;
                case COPPER:
                    livingVictim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WITHER, 200, 0, false, true));
                    break;
                case TIN:
                    livingVictim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 60, 255, false, true));
                    break;
                case IRON:
                    event.setDamage(event.getDamage() * 2);
                    break;
                case MERCURY:
                    livingVictim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.POISON, 1200, 0, false, true));
                    break;
                case LEAD:
                default:
                    break;
            }
        }
    }
}