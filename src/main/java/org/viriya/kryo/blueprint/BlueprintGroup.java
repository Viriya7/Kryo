package org.viriya.kryo.blueprint;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BlueprintGroup {

    private final String id;
    private final Material icon;
    private final Component name;
    private final List<ItemStack> items = new ArrayList<>();

    public BlueprintGroup(String id, Material icon, Component name) {
        this.id = id;
        this.icon = icon;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public Material getIcon() {
        return icon;
    }

    public Component getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }

    public ItemStack getIconItem() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}