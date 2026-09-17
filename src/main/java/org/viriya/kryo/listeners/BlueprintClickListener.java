package org.viriya.kryo.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.viriya.kryo.blueprint.BlueprintGUI;

public class BlueprintClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Blueprint")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            int currentPage = 0;

            try {
                if (title.contains("Page ")) {
                    currentPage = Integer.parseInt(title.split("Page ")[1].trim()) - 1;
                }
            } catch (Exception ignored) {}

            if (slot == 0) {
                BlueprintGUI.openSettings(player);
            } else if (slot == 46 && event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                BlueprintGUI.openMenu(player, currentPage - 1);
            } else if (slot == 52 && event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                BlueprintGUI.openMenu(player, currentPage + 1);
            }
        } else if (title.contains("Settings")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            if (slot == 26) {
                BlueprintGUI.openMenu(player, 0);
            }
        }
    }
}