package org.viriya.kryo.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.viriya.kryo.api.KryoRegistry;
import org.viriya.kryo.blueprint.BlueprintGUI;
import org.viriya.kryo.blueprint.BlueprintGroup;

import java.util.List;

public class BlueprintClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.equals("Blueprint")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();

            if (slot == 0) {
                BlueprintGUI.openSettings(player);
                return;
            }

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                List<BlueprintGroup> groups = KryoRegistry.getGroups();
                for (BlueprintGroup group : groups) {
                    if (clickedItem.getType() == group.getIcon()) {
                        BlueprintGUI.openGroupMenu(player, group);
                        break;
                    }
                }
            }
        } else if (isGroupMenu(title)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            // Tombol Back berada di slot 52 (y = 5, x = 7)
            if (slot == 52) {
                BlueprintGUI.openMenu(player);
            }
        } else if (title.equals("Settings")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            if (slot == 26) {
                BlueprintGUI.openMenu(player);
            }
        }
    }

    private boolean isGroupMenu(String title) {
        List<BlueprintGroup> groups = KryoRegistry.getGroups();
        for (BlueprintGroup group : groups) {
            String groupName = PlainTextComponentSerializer.plainText().serialize(group.getName());
            if (title.equals(groupName)) {
                return true;
            }
        }
        return false;
    }
}