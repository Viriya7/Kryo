package org.viriya.kryo.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BlueprintClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        @SuppressWarnings("deprecation")
        String title = event.getView().getTitle();

        if (title.startsWith("§9Blueprint")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }
        }
    }
}