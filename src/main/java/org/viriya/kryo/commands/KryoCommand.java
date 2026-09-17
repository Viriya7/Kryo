package org.viriya.kryo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.viriya.kryo.blueprint.Blueprint;

public class KryoCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public KryoCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Command ini hanya dapat dijalankan oleh player.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("blueprint")) {
            player.getInventory().addItem(Blueprint.createBlueprint(plugin));
            player.sendMessage(Component.text("Kamu menerima item Blueprint!", NamedTextColor.GREEN));
            return true;
        }

        player.sendMessage(Component.text("Gunakan: /kryo blueprint", NamedTextColor.RED));
        return true;
    }
}