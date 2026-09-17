package org.viriya.kryo.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class BlueprintListener implements Listener {

    private final JavaPlugin plugin;

    public BlueprintListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "blueprint");

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if ("BLUEPRINT".equals(id)) {
                event.setCancelled(true);
            }
        }
    }
}