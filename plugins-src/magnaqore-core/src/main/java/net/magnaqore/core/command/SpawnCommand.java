package net.magnaqore.core.command;

import net.magnaqore.core.MagnaQoreCore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SpawnCommand implements CommandExecutor {

    private final MagnaQoreCore plugin;

    public SpawnCommand(MagnaQoreCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().msg(sender, "spawn-players-only"));
            return true;
        }
        Location spawn = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        player.teleportAsync(spawn).thenAccept(ok -> {
            if (ok) {
                player.sendMessage(plugin.lang().msg(player, "spawn-done"));
            }
        });
        return true;
    }
}
