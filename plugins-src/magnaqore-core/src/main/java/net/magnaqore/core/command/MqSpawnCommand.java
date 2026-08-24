package net.magnaqore.core.command;

import net.magnaqore.core.MagnaQoreCore;
import net.magnaqore.core.spawn.SpawnBuilder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * /mqspawn build [x z] — (re)build the custom spawn plaza and move the world
 * spawn onto it. Dev tool (permission magnaqore.dev), safe to re-run: the
 * builder is deterministic, so iterating on the design just rebuilds in place.
 */
public final class MqSpawnCommand implements CommandExecutor {

    private final MagnaQoreCore plugin;

    public MqSpawnCommand(MagnaQoreCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("build")) {
            sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
            return true;
        }
        World world = plugin.getServer().getWorlds().getFirst();
        int x;
        int z;
        if (args.length >= 3) {
            x = Integer.parseInt(args[1]);
            z = Integer.parseInt(args[2]);
        } else {
            Location current = world.getSpawnLocation();
            x = current.getBlockX();
            z = current.getBlockZ();
        }
        sender.sendMessage(plugin.lang().msg(sender, "mqspawn-building"));
        Location spawn = new SpawnBuilder(world, x, z).build();
        world.setSpawnLocation(spawn);
        sender.sendMessage(plugin.lang().msg(sender, "mqspawn-done"));
        return true;
    }
}
