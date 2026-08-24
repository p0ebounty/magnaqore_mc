package net.magnaqore.core.spawn;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Pastes a WorldEdit schematic from plugins/MagnaQoreCore/schematics/.
 * Console-driven (the vanilla //paste needs a player session), so the whole
 * spawn build can be scripted and re-run deterministically.
 */
public final class SchematicPaster {

    public record Result(int width, int height, int length, int minY, int maxY) {}

    private SchematicPaster() {
    }

    /**
     * @param file    schematic file (.schem / .schematic)
     * @param world   target world
     * @param x       paste center X
     * @param z       paste center Z
     * @param baseY   Y where the schematic's bottom layer lands
     * @param ignoreAir paste air blocks too (false keeps surrounding terrain)
     */
    public static Result paste(File file, World world, int x, int z, int baseY, boolean ignoreAir)
            throws Exception {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IllegalArgumentException("unsupported schematic format: " + file.getName());
        }
        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        }

        BlockVector3 dim = clipboard.getDimensions();
        // Anchor: horizontal center of the schematic, bottom layer at baseY.
        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3 min = clipboard.getMinimumPoint();
        int offsetX = origin.x() - min.x();
        int offsetY = origin.y() - min.y();
        int offsetZ = origin.z() - min.z();
        BlockVector3 target = BlockVector3.at(
                x - dim.x() / 2 + offsetX,
                baseY + offsetY,
                z - dim.z() / 2 + offsetZ);

        try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operations.complete(new ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(target)
                    .ignoreAirBlocks(!ignoreAir)
                    .copyEntities(false)
                    .build());
        }
        return new Result(dim.x(), dim.y(), dim.z(), baseY, baseY + dim.y());
    }

    /** Clears a box of terrain so a schematic can land on flat ground. */
    public static void flatten(World world, int cx, int cz, int halfX, int halfZ, int groundY,
                               org.bukkit.Material floor) {
        for (int x = cx - halfX; x <= cx + halfX; x++) {
            for (int z = cz - halfZ; z <= cz + halfZ; z++) {
                for (int y = groundY + 1; y <= groundY + 80; y++) {
                    if (world.getBlockAt(x, y, z).getType() != org.bukkit.Material.AIR) {
                        world.getBlockAt(x, y, z).setType(org.bukkit.Material.AIR, false);
                    }
                }
                world.getBlockAt(x, groundY, z).setType(floor, false);
                for (int y = groundY - 1; y >= groundY - 8; y--) {
                    var b = world.getBlockAt(x, y, z);
                    if (b.getType().isSolid()) {
                        break;
                    }
                    b.setType(org.bukkit.Material.DIRT, false);
                }
            }
        }
    }

    public static Location spawnPointOn(World world, int x, int z, int y) {
        return new Location(world, x + 0.5, y + 1, z + 0.5, 180f, 0f);
    }

    /** Regenerates a box back to seed-original terrain (full height). */
    public static void regen(World world, int x1, int z1, int x2, int z2) throws Exception {
        var weWorld = BukkitAdapter.adapt(world);
        var region = new com.sk89q.worldedit.regions.CuboidRegion(
                weWorld,
                BlockVector3.at(Math.min(x1, x2), weWorld.getMinY(), Math.min(z1, z2)),
                BlockVector3.at(Math.max(x1, x2), weWorld.getMaxY(), Math.max(z1, z2)));
        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            weWorld.regenerate(region, session);
        }
    }
}
