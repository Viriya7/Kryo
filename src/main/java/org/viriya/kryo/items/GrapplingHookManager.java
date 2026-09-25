package org.viriya.kryo.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.viriya.kryo.blueprint.BlueprintItem;
import org.viriya.kryo.workbench.WorkbenchManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GrapplingHookManager implements Listener {

    private static NamespacedKey grapplingKey;
    private static Plugin pluginInstance;
    private static final Map<UUID, Arrow> activeGrapples = new HashMap<>();

    public static void init(Plugin plugin) {
        pluginInstance = plugin;
        if (grapplingKey == null) {
            grapplingKey = new NamespacedKey(plugin, "custom_grappling_hook");
        }

        ItemStack[] recipe = {
                null, null, IngotManager.getTinIngot(),
                null, new ItemStack(Material.STRING), null,
                new ItemStack(Material.STRING), null, null
        };
        WorkbenchManager.registerCustomRecipe(GrapplingHookManager::getGrapplingHookItem, recipe);
        BlueprintItem.registerToGroup("TOOLS", getGrapplingHookItem(), WorkbenchManager.getWorkbenchItem(), recipe);
    }

    public static ItemStack getGrapplingHookItem() {
        ItemStack item = new ItemStack(Material.LEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Grappling Hook", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Right Click to grappling", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            if (grapplingKey != null) {
                meta.getPersistentDataContainer().set(grapplingKey, PersistentDataType.BYTE, (byte) 1);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isCustomGrapplingHook(ItemStack item) {
        if (item == null || !item.hasItemMeta() || grapplingKey == null) return false;
        ItemMeta meta = item.getItemMeta();
        Byte value = meta.getPersistentDataContainer().get(grapplingKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCustomGrapplingHook(item)) return;
        event.setCancelled(true);

        if (activeGrapples.containsKey(player.getUniqueId())) {
            return;
        }

        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setShooter(player);
        arrow.setGravity(true);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 1.0f);

        activeGrapples.put(player.getUniqueId(), arrow);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        if (!activeGrapples.containsKey(player.getUniqueId()) || !activeGrapples.get(player.getUniqueId()).equals(arrow)) {
            return;
        }

        if (event.getHitBlock() != null) {
            Location targetLoc = arrow.getLocation();
            arrow.remove();

            player.getWorld().playSound(targetLoc, Sound.ENTITY_LEASH_KNOT_PLACE, 1.0f, 1.0f);

            Particle.DustOptions leadColor = new Particle.DustOptions(Color.fromRGB(115, 80, 50), 1.0f);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead() || targetLoc.getWorld() == null || !targetLoc.getWorld().equals(player.getWorld())) {
                        activeGrapples.remove(player.getUniqueId());
                        this.cancel();
                        return;
                    }

                    Location pLoc = player.getLocation().add(0, 1, 0);
                    Vector direction = targetLoc.toVector().subtract(pLoc.toVector());
                    double distance = pLoc.distance(targetLoc);

                    for (double d = 0; d < distance; d += 0.5) {
                        Vector step = direction.clone().normalize().multiply(d);
                        Location particleLoc = pLoc.clone().add(step);
                        player.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1, leadColor);
                    }

                    Location playerLoc = player.getLocation();
                    boolean blockInFront = !playerLoc.add(0, 1, 0).getBlock().isPassable() || !playerLoc.getBlock().isPassable();

                    if (distance < 2.0 || blockInFront) {
                        activeGrapples.remove(player.getUniqueId());
                        this.cancel();
                        return;
                    }

                    Vector velocity = targetLoc.toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.42);
                    player.setVelocity(velocity);
                }
            }.runTaskTimer(pluginInstance, 0L, 2L);
        } else {
            Bukkit.getScheduler().runTaskLater(pluginInstance, () -> {
                if (activeGrapples.get(player.getUniqueId()) == arrow) {
                    activeGrapples.remove(player.getUniqueId());
                    arrow.remove();
                }
            }, 40L);
        }
    }
}