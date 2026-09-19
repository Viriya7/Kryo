package org.viriya.kryo.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.viriya.kryo.api.KryoRegistry;
import org.viriya.kryo.blueprint.BlueprintGUI;
import org.viriya.kryo.blueprint.BlueprintGroup;
import org.viriya.kryo.blueprint.BlueprintItem;

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

            if (slot == 52) {
                BlueprintGUI.openMenu(player);
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                for (BlueprintGroup group : KryoRegistry.getGroups()) {
                    BlueprintItem.ItemRecipe recipe = BlueprintItem.getRecipe(group.getId(), clickedItem);
                    if (recipe != null) {
                        BlueprintItem.openBlueprintRecipeGUI(player, recipe);
                        break;
                    }
                }
            }
        } else if (title.startsWith("Blueprint > ")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            if (slot == 0) {
                ItemStack resultItem = event.getInventory().getItem(16);
                if (resultItem != null) {
                    for (BlueprintGroup group : KryoRegistry.getGroups()) {
                        BlueprintItem.ItemRecipe recipe = BlueprintItem.getRecipe(group.getId(), resultItem);
                        if (recipe != null) {
                            BlueprintGUI.openGroupMenu(player, group);
                            return;
                        }
                    }
                }
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