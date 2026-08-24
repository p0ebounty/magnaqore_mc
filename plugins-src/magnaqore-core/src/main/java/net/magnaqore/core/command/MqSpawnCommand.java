package net.magnaqore.core.command;

import java.io.File;
import net.magnaqore.core.MagnaQoreCore;
import net.magnaqore.core.spawn.CastleSpawnBuilder;
import net.magnaqore.core.spawn.SchematicPaster;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Dev tooling for the spawn (permission magnaqore.dev):
 *   /mqspawn build [x z]                      — procedural castle generator
 *   /mqspawn paste <file> [x z] [baseY]       — paste a schematic from
 *                                               plugins/MagnaQoreCore/schematics/
 *   /mqspawn flatten <halfX> <halfZ> [x z y]  — clear/level a build site
 *   /mqspawn setpoint <x> <y> <z>             — set the world spawn point
 *   /mqspawn list                             — list available schematics
 */
public final class MqSpawnCommand implements CommandExecutor {

    private final MagnaQoreCore plugin;

    public MqSpawnCommand(MagnaQoreCore plugin) {
        this.plugin = plugin;
    }

    private File schematicsDir() {
        File dir = new File(plugin.getDataFolder(), "schematics");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        World world = plugin.getServer().getWorlds().getFirst();
        String sub = args.length > 0 ? args[0].toLowerCase() : "";

        switch (sub) {
            case "build" -> {
                int[] xz = coords(args, 1, world);
                sender.sendMessage(plugin.lang().msg(sender, "mqspawn-building"));
                Location spawn = new CastleSpawnBuilder(world, xz[0], xz[1]).build();
                world.setSpawnLocation(spawn);
                sender.sendMessage(plugin.lang().msg(sender, "mqspawn-done"));
            }
            case "paste" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                File file = new File(schematicsDir(), args[1]);
                if (!file.isFile()) {
                    sender.sendMessage(text("file not found: " + file.getName(), NamedTextColor.RED));
                    return true;
                }
                int[] xz = coords(args, 2, world);
                int baseY = args.length >= 5 ? Integer.parseInt(args[4])
                        : world.getHighestBlockYAt(xz[0], xz[1]);
                sender.sendMessage(plugin.lang().msg(sender, "mqspawn-building"));
                try {
                    var r = SchematicPaster.paste(file, world, xz[0], xz[1], baseY, false);
                    sender.sendMessage(text("pasted " + file.getName() + " ("
                            + r.width() + "x" + r.height() + "x" + r.length()
                            + ") at " + xz[0] + "," + baseY + "," + xz[1], NamedTextColor.GREEN));
                } catch (Exception e) {
                    sender.sendMessage(text("paste failed: " + e.getMessage(), NamedTextColor.RED));
                    plugin.getLogger().warning("schematic paste failed: " + e);
                }
            }
            case "flatten" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                int halfX = Integer.parseInt(args[1]);
                int halfZ = Integer.parseInt(args[2]);
                int[] xz = coords(args, 3, world);
                int groundY = args.length >= 6 ? Integer.parseInt(args[5])
                        : world.getHighestBlockYAt(xz[0], xz[1]);
                SchematicPaster.flatten(world, xz[0], xz[1], halfX, halfZ, groundY, Material.GRASS_BLOCK);
                sender.sendMessage(text("flattened " + (halfX * 2 + 1) + "x" + (halfZ * 2 + 1)
                        + " at y=" + groundY, NamedTextColor.GREEN));
            }
            case "setpoint" -> {
                if (args.length < 4) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                int sx = Integer.parseInt(args[1]);
                int sz = Integer.parseInt(args[3]);
                double sy = args[2].equals("~")
                        ? world.getHighestBlockYAt(sx, sz) + 1
                        : Double.parseDouble(args[2]);
                Location loc = new Location(world, sx + 0.5, sy, sz + 0.5, 180f, 0f);
                world.setSpawnLocation(loc);
                sender.sendMessage(text("spawn point set", NamedTextColor.GREEN));
            }
            case "regen" -> {
                if (args.length < 5) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                int x1 = Integer.parseInt(args[1]);
                int z1 = Integer.parseInt(args[2]);
                int x2 = Integer.parseInt(args[3]);
                int z2 = Integer.parseInt(args[4]);
                sender.sendMessage(text("regenerating " + x1 + "," + z1 + " .. " + x2 + "," + z2
                        + "…", NamedTextColor.GRAY));
                try {
                    SchematicPaster.regen(world, x1, z1, x2, z2);
                    sender.sendMessage(text("region regenerated to seed terrain", NamedTextColor.GREEN));
                } catch (Exception e) {
                    sender.sendMessage(text("regen failed: " + e.getMessage(), NamedTextColor.RED));
                    plugin.getLogger().warning("regen failed: " + e);
                }
            }
            case "biome" -> {
                if (args.length < 6) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                int x1 = Math.min(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
                int x2 = Math.max(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
                int z1 = Math.min(Integer.parseInt(args[2]), Integer.parseInt(args[4]));
                int z2 = Math.max(Integer.parseInt(args[2]), Integer.parseInt(args[4]));
                var key = org.bukkit.NamespacedKey.minecraft(args[5].toLowerCase());
                var biome = org.bukkit.Registry.BIOME.get(key);
                if (biome == null) {
                    sender.sendMessage(text("unknown biome: " + args[5], NamedTextColor.RED));
                    return true;
                }
                // biomes are stored in 4x4x4 cells; stepping by 4 covers everything
                int cells = 0;
                for (int x = x1; x <= x2; x += 4) {
                    for (int z = z1; z <= z2; z += 4) {
                        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y += 4) {
                            world.setBiome(x, y, z, biome);
                            cells++;
                        }
                    }
                }
                sender.sendMessage(text("biome set to " + args[5] + " (" + cells + " cells); "
                        + "chunks refresh for players on relog/render", NamedTextColor.GREEN));
            }
            case "desnow" -> {
                if (args.length < 5) {
                    sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
                    return true;
                }
                int x1 = Math.min(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
                int x2 = Math.max(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
                int z1 = Math.min(Integer.parseInt(args[2]), Integer.parseInt(args[4]));
                int z2 = Math.max(Integer.parseInt(args[2]), Integer.parseInt(args[4]));
                int removed = 0;
                for (int x = x1; x <= x2; x++) {
                    for (int z = z1; z <= z2; z++) {
                        int top = world.getHighestBlockYAt(x, z);
                        for (int y = top + 1; y >= top - 30; y--) {
                            var b = world.getBlockAt(x, y, z);
                            if (b.getType() == Material.SNOW || b.getType() == Material.POWDER_SNOW) {
                                b.setType(Material.AIR, true);
                                removed++;
                            }
                        }
                    }
                }
                sender.sendMessage(text("removed " + removed + " snow layers", NamedTextColor.GREEN));
            }
            case "list" -> {
                File[] files = schematicsDir().listFiles((d, n) ->
                        n.endsWith(".schem") || n.endsWith(".schematic"));
                if (files == null || files.length == 0) {
                    sender.sendMessage(text("no schematics in " + schematicsDir(), NamedTextColor.GRAY));
                    return true;
                }
                for (File f : files) {
                    sender.sendMessage(text(f.getName() + "  (" + (f.length() / 1024) + " KB)",
                            NamedTextColor.GRAY));
                }
            }
            default -> sender.sendMessage(plugin.lang().msg(sender, "mqspawn-usage"));
        }
        return true;
    }

    private int[] coords(String[] args, int from, World world) {
        if (args.length >= from + 2) {
            return new int[]{Integer.parseInt(args[from]), Integer.parseInt(args[from + 1])};
        }
        Location spawn = world.getSpawnLocation();
        return new int[]{spawn.getBlockX(), spawn.getBlockZ()};
    }

    private Component text(String s, NamedTextColor color) {
        return Component.text(s, color);
    }
}
