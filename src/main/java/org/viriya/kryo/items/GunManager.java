package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class GunManager implements Listener {

    private static NamespacedKey glockKey;
    private static NamespacedKey ammoStorageKey;
    private static NamespacedKey firedBulletTypeKey;
    private static final String GUI_TITLE = "Glock Ammo Storage (5 Slots)";
    private static final int STORAGE_SIZE = 5;

    private static final Map<UUID, Long> shootCooldown = new HashMap<>();

    public static void init(Plugin plugin) {
        if (glockKey == null) {
            glockKey = new NamespacedKey(plugin, "custom_glock");
        }
        if (ammoStorageKey == null) {
            ammoStorageKey = new NamespacedKey(plugin, "glock_ammo_storage");
        }
        if (firedBulletTypeKey == null) {
            firedBulletTypeKey = new NamespacedKey(plugin, "fired_bullet_type");
        }
        plugin.getServer().getPluginManager().registerEvents(new GunManager(), plugin);
        registerRecipes();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (isGlock(item)) {
                    int totalAmmo = countTotalAmmo(item);
                    Component actionbarText = Component.text("Ammo: ", NamedTextColor.GRAY)
                            .append(Component.text(totalAmmo, NamedTextColor.GOLD, TextDecoration.BOLD));
                    player.sendActionBar(actionbarText);
                }
            }
        }, 0L, 10L);
    }

    public static ItemStack getGlock() {
        ItemStack item = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Glock", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(glockKey, PersistentDataType.BYTE, (byte) 1);

            if (meta instanceof Damageable damageable) {
                damageable.setDamage(Material.STONE_HOE.getMaxDurability());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static boolean isGlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(glockKey, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    private static int countTotalAmmo(ItemStack glock) {
        if (!isGlock(glock)) return 0;
        ItemMeta meta = glock.getItemMeta();
        String serialized = meta.getPersistentDataContainer().get(ammoStorageKey, PersistentDataType.STRING);
        if (serialized == null || serialized.isEmpty()) return 0;

        ItemStack[] items = itemStackArrayFromBase64(serialized);
        int total = 0;
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null && BulletAssemblyManager.getAssembledBulletType(item) != null) {
                    total += item.getAmount();
                }
            }
        }
        return total;
    }

    private static void updateGlockVisuals(ItemStack glock, int totalAmmo) {
        ItemMeta meta = glock.getItemMeta();
        if (meta == null) return;

        if (meta instanceof Damageable damageable) {
            int maxDurability = Material.STONE_HOE.getMaxDurability();
            int cap = Math.min(totalAmmo, 64);
            int durabilityVal = maxDurability - (int)(((double)cap / 64.0) * maxDurability);
            damageable.setDamage(Math.max(0, durabilityVal));
        }
        glock.setItemMeta(meta);
    }

    private static void openGlockStorageGUI(Player player, ItemStack glock) {
        Inventory gui = Bukkit.createInventory(null, 9, Component.text(GUI_TITLE));

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text("Locked Slot", NamedTextColor.RED));
            filler.setItemMeta(fillerMeta);
        }

        for (int i = STORAGE_SIZE; i < 9; i++) {
            gui.setItem(i, filler);
        }

        ItemMeta meta = glock.getItemMeta();
        if (meta != null) {
            String serialized = meta.getPersistentDataContainer().get(ammoStorageKey, PersistentDataType.STRING);
            if (serialized != null && !serialized.isEmpty()) {
                ItemStack[] items = itemStackArrayFromBase64(serialized);
                if (items != null) {
                    for (int i = 0; i < Math.min(items.length, STORAGE_SIZE); i++) {
                        gui.setItem(i, items[i]);
                    }
                }
            }
        }
        player.openInventory(gui);
    }

    private static void registerRecipes() {
        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemStack oak = new ItemStack(Material.OAK_LOG);

        ItemStack[] recipeGrid = new ItemStack[9];
        recipeGrid[0] = iron;
        recipeGrid[1] = iron;
        recipeGrid[2] = iron;
        recipeGrid[3] = oak;
        recipeGrid[4] = iron;
        recipeGrid[5] = iron;
        recipeGrid[6] = oak;

        ItemStack glockResult = getGlock();
        WorkbenchManager.registerCustomRecipe(glockResult, recipeGrid);
        BlueprintItem.registerToGroup("FIREARM", glockResult, WorkbenchManager.getWorkbenchItem(), recipeGrid);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isGlock(item)) return;
        event.setCancelled(true);

        if (player.isSneaking()) {
            openGlockStorageGUI(player, item);
            return;
        }

        long now = System.currentTimeMillis();
        if (shootCooldown.containsKey(player.getUniqueId())) {
            if (now - shootCooldown.get(player.getUniqueId()) < 200) {
                return;
            }
        }
        shootCooldown.put(player.getUniqueId(), now);

        shootGlock(player, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(GUI_TITLE))) return;

        int rawSlot = event.getRawSlot();
        if (rawSlot >= STORAGE_SIZE && rawSlot < 9) {
            event.setCancelled(true);
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && cursor.getType() != Material.AIR) {
            if (BulletAssemblyManager.getAssembledBulletType(cursor) == null) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    player.sendMessage(Component.text("Only bullets can be loaded into the Glock!", NamedTextColor.RED));
                }
                return;
            }
        }
        if (current != null && current.getType() != Material.AIR) {
            if (BulletAssemblyManager.getAssembledBulletType(current) == null && event.getAction().name().contains("PLACE")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().title().equals(Component.text(GUI_TITLE))) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        ItemStack glock = player.getInventory().getItemInMainHand();
        if (!isGlock(glock)) {
            glock = player.getInventory().getItemInOffHand();
            if (!isGlock(glock)) return;
        }

        Inventory inv = event.getInventory();
        ItemStack[] itemsToSave = new ItemStack[STORAGE_SIZE];
        for (int i = 0; i < STORAGE_SIZE; i++) {
            itemsToSave[i] = inv.getItem(i);
        }

        ItemMeta meta = glock.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ammoStorageKey, PersistentDataType.STRING, itemStackArrayToBase64(itemsToSave));
            glock.setItemMeta(meta);

            int total = countTotalAmmo(glock);
            updateGlockVisuals(glock, total);
        }
        player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 0.8f, 1f);
    }

    private void shootGlock(Player player, ItemStack glock) {
        int totalAmmo = countTotalAmmo(glock);
        if (totalAmmo <= 0) {
            player.sendMessage(Component.text("Empty Glock! Shift + Right Click to open bullet storage.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        ItemMeta meta = glock.getItemMeta();
        String serialized = meta.getPersistentDataContainer().get(ammoStorageKey, PersistentDataType.STRING);
        ItemStack[] items = itemStackArrayFromBase64(serialized);

        BulletManager.TipType firedTipType = null;

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    BulletManager.TipType tip = BulletAssemblyManager.getAssembledBulletType(items[i]);
                    if (tip != null) {
                        firedTipType = tip;
                        items[i].setAmount(items[i].getAmount() - 1);
                        if (items[i].getAmount() <= 0) {
                            items[i] = null;
                        }
                        break;
                    }
                }
            }
            meta.getPersistentDataContainer().set(ammoStorageKey, PersistentDataType.STRING, itemStackArrayToBase64(items));
            glock.setItemMeta(meta);
        }

        int newTotal = countTotalAmmo(glock);
        updateGlockVisuals(glock, newTotal);

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.2f, 1.5f);

        Snowball bulletProjectile = player.launchProjectile(Snowball.class);
        bulletProjectile.setShooter(player);
        bulletProjectile.setVelocity(player.getLocation().getDirection().multiply(3.0));
        bulletProjectile.setGravity(false);

        if (firedTipType != null) {
            bulletProjectile.getPersistentDataContainer().set(firedBulletTypeKey, PersistentDataType.STRING, firedTipType.name());
        }

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!bulletProjectile.isValid() || bulletProjectile.isDead()) {
                    this.cancel();
                    return;
                }
                bulletProjectile.getWorld().spawnParticle(Particle.SMOKE_LARGE, bulletProjectile.getLocation(), 1, 0.05, 0.05, 0.05, 0.0);
            }
        }.runTaskTimer(org.viriya.kryo.Kryo.getPlugin(org.viriya.kryo.Kryo.class), 0L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();
        if (!(projectile instanceof Snowball snowball)) return;
        if (!snowball.getPersistentDataContainer().has(firedBulletTypeKey, PersistentDataType.STRING)) return;

        String tipName = snowball.getPersistentDataContainer().get(firedBulletTypeKey, PersistentDataType.STRING);
        BulletManager.TipType tipType;
        try {
            tipType = BulletManager.TipType.valueOf(tipName);
        } catch (IllegalArgumentException e) {
            return;
        }

        Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof LivingEntity livingEntity) {
            Player shooter = snowball.getShooter() instanceof Player p ? p : null;

            livingEntity.setNoDamageTicks(0);

            switch (tipType) {
                case GOLD:
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1200, 0, false, true));
                    livingEntity.damage(6.0, shooter);
                    break;
                case COPPER:
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, false, true));
                    livingEntity.damage(4.0, shooter);
                    break;
                case TIN:
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 255, false, false));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false));
                    livingEntity.damage(3.0, shooter);
                    break;
                case IRON:
                    livingEntity.damage(10.0, shooter);
                    break;
                case MERCURY:
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1200, 0, false, true));
                    livingEntity.damage(3.0, shooter);
                    break;
                case LEAD:
                default:
                    livingEntity.damage(5.0, shooter);
                    break;
            }
        }
        snowball.remove();
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
}