package org.viriya.kryo.blueprint;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class BlueprintGroup {

    private final String id;
    private final Material icon;
    private final String base64Texture;
    private final Component name;
    private final List<ItemStack> items = new ArrayList<>();

    public BlueprintGroup(String id, Material icon, Component name) {
        this.id = id;
        this.icon = icon;
        this.base64Texture = null;
        this.name = name;
    }

    public BlueprintGroup(String id, String base64Texture, Component name) {
        this.id = id;
        this.icon = Material.PLAYER_HEAD;
        this.base64Texture = base64Texture;
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

        if (icon == Material.PLAYER_HEAD && base64Texture != null && !base64Texture.isEmpty()) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            if (skullMeta != null) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", base64Texture));
                skullMeta.setPlayerProfile(profile);

                skullMeta.displayName(name.decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(skullMeta);
                return item;
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }
}