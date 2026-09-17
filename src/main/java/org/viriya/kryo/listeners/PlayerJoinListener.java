package org.viriya.kryo.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.viriya.kryo.blueprint.Blueprint;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(plugin, "has_received_blueprint");

        if (!player.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            player.getInventory().addItem(Blueprint.createBlueprint(plugin));
            player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        }
    }
}