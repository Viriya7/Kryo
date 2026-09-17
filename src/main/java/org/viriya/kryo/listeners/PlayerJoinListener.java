package org.viriya.kryo.listeners;

import org.viriya.kryo.blueprint.Blueprint;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!hasBlueprint(player)) {
            ItemStack blueprint = Blueprint.createBlueprint(plugin);
            player.getInventory().addItem(blueprint);
        }
    }

    private boolean hasBlueprint(Player player) {
        NamespacedKey key = new NamespacedKey(plugin, "blueprint");

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if ("BLUEPRINT".equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
}