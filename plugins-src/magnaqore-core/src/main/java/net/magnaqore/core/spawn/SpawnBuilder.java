package net.magnaqore.core.spawn;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Stairs;

/**
 * MagnaQore custom spawn: a circular plaza themed for the cherry grove
 * (cherry wood + calcite + polished deepslate + amethyst/end-rod brand accent).
 * Fully generated in code — no third-party builds (owner requirement).
 *
 * Geometry (relative to center C at plaza floor level Y):
 *  - r<=12  floor: cherry planks, calcite band at r==7, polished deepslate rim r 11..12
 *  - r<=2   raised calcite podium (+1) with deepslate-brick edge, amethyst core + end rod
 *  - 8 lamp posts at r=10 (every 45deg), benches between them
 *  - 4 cherry-log gates at cardinal points (r=14), hanging lanterns
 *  - terrain: air carved to Y+9 inside r<=13, deepslate-brick skirt below the rim
 *  - pink petals sprinkled on surrounding grass r 14..19
 */
public final class SpawnBuilder {

    private static final int R_PLAZA = 12;
    private static final int R_RIM_IN = 11;
    private static final int R_BAND = 7;
    private static final int R_PODIUM = 2;
    private static final int R_POSTS = 10;
    private static final int R_GATES = 14;
    private static final int CLEAR_H = 9;

    private final World world;
    private final int cx;
    private final int cy;
    private final int cz;
    private final Random random = new Random(4242);

    public SpawnBuilder(World world, int cx, int cz) {
        this.world = world;
        this.cx = cx;
        this.cz = cz;
        this.cy = world.getHighestBlockYAt(cx, cz);
    }

    /** Builds the plaza and returns the ideal spawn location (podium edge, facing north gate). */
    public Location build() {
        carveAndFloor();
        podium();
        lampPostsAndBenches();
        gates();
        petals();
        return new Location(world, cx + 0.5, cy + 2, cz + 3.5, 180f, 0f);
    }

    private void set(int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
    }

    private void setData(int x, int y, int z, BlockData data) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(data.getMaterial(), false);
        b.setBlockData(data, false);
    }

    private double dist(int x, int z) {
        return Math.hypot(x - cx, z - cz);
    }

    private void carveAndFloor() {
        for (int x = cx - R_PLAZA - 2; x <= cx + R_PLAZA + 2; x++) {
            for (int z = cz - R_PLAZA - 2; z <= cz + R_PLAZA + 2; z++) {
                double r = dist(x, z);
                if (r > R_PLAZA + 1.4) {
                    continue;
                }
                // clear air above the floor
                for (int y = cy + 1; y <= cy + CLEAR_H; y++) {
                    set(x, y, z, Material.AIR);
                }
                // floor
                Material floor;
                if (r > R_RIM_IN - 0.5) {
                    floor = Material.POLISHED_DEEPSLATE;
                } else if (Math.abs(r - R_BAND) < 0.6) {
                    floor = Material.CALCITE;
                } else {
                    floor = Material.CHERRY_PLANKS;
                }
                set(x, cy, z, floor);
                // skirt: solid support below floor down to terrain
                for (int y = cy - 1; y >= cy - 6; y--) {
                    Block below = world.getBlockAt(x, y, z);
                    if (below.getType().isSolid() && !below.getType().name().contains("LEAVES")) {
                        break;
                    }
                    set(x, y, z, r > R_RIM_IN - 0.5 ? Material.DEEPSLATE_BRICKS : Material.DEEPSLATE);
                }
            }
        }
    }

    private void podium() {
        for (int x = cx - R_PODIUM - 1; x <= cx + R_PODIUM + 1; x++) {
            for (int z = cz - R_PODIUM - 1; z <= cz + R_PODIUM + 1; z++) {
                double r = dist(x, z);
                if (r <= R_PODIUM + 0.4) {
                    set(x, cy + 1, z, Material.CALCITE);
                } else if (r <= R_PODIUM + 1.4) {
                    set(x, cy + 1, z, Material.DEEPSLATE_TILES);
                }
            }
        }
        set(cx, cy + 2, cz, Material.AMETHYST_BLOCK);
        set(cx, cy + 3, cz, Material.END_ROD);
        // four small amethyst buds on the podium corners
        int d = R_PODIUM;
        set(cx + d, cy + 2, cz + d, Material.SMALL_AMETHYST_BUD);
        set(cx - d, cy + 2, cz + d, Material.SMALL_AMETHYST_BUD);
        set(cx + d, cy + 2, cz - d, Material.SMALL_AMETHYST_BUD);
        set(cx - d, cy + 2, cz - d, Material.SMALL_AMETHYST_BUD);
    }

    private void lampPostsAndBenches() {
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(k * 45);
            int x = cx + (int) Math.round(R_POSTS * Math.sin(a));
            int z = cz + (int) Math.round(R_POSTS * Math.cos(a));
            set(x, cy + 1, z, Material.POLISHED_DEEPSLATE);
            set(x, cy + 2, z, Material.CHERRY_FENCE);
            set(x, cy + 3, z, Material.CHERRY_FENCE);
            Lantern lantern = (Lantern) Material.LANTERN.createBlockData();
            lantern.setHanging(false);
            setData(x, cy + 4, z, lantern);

            // bench between this post and the next (at +22.5deg), facing center
            double b = Math.toRadians(k * 45 + 22.5);
            for (int i = -1; i <= 1; i++) {
                double ang = b + Math.toRadians(i * 6);
                int bx = cx + (int) Math.round((R_POSTS + 0.5) * Math.sin(ang));
                int bz = cz + (int) Math.round((R_POSTS + 0.5) * Math.cos(ang));
                Stairs stairs = (Stairs) Material.CHERRY_STAIRS.createBlockData();
                stairs.setFacing(faceTowardsCenter(bx, bz));
                stairs.setHalf(Bisected.Half.BOTTOM);
                setData(bx, cy + 1, bz, stairs);
            }
        }
    }

    private BlockFace faceTowardsCenter(int x, int z) {
        int dx = cx - x;
        int dz = cz - z;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        }
        return dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    private void gates() {
        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}}; // S, N, E, W (dx,dz as sin/cos pairs)
        for (int[] dir : dirs) {
            int gx = cx + dir[0] * R_GATES;
            int gz = cz + dir[1] * R_GATES;
            // pillar offsets perpendicular to the gate direction
            int px = dir[1];
            int pz = dir[0];
            int baseMax = Integer.MIN_VALUE;
            for (int side = -2; side <= 2; side += 4) {
                int bx = gx + px * side;
                int bz = gz + pz * side;
                int by = world.getHighestBlockYAt(bx, bz);
                baseMax = Math.max(baseMax, by);
            }
            for (int side = -2; side <= 2; side += 4) {
                int bx = gx + px * side;
                int bz = gz + pz * side;
                set(bx, baseMax + 1, bz, Material.POLISHED_DEEPSLATE);
                for (int h = 2; h <= 5; h++) {
                    set(bx, baseMax + h, bz, Material.STRIPPED_CHERRY_LOG);
                }
                // fill any gap below the pillar base
                for (int y = baseMax; y >= baseMax - 4; y--) {
                    Block below = world.getBlockAt(bx, y, bz);
                    if (below.getType().isSolid()) {
                        break;
                    }
                    set(bx, y, bz, Material.POLISHED_DEEPSLATE);
                }
            }
            // crossbar + hanging lantern
            int topY = baseMax + 6;
            for (int side = -2; side <= 2; side++) {
                set(gx + px * side, topY, gz + pz * side, Material.CHERRY_SLAB);
            }
            Lantern hanging = (Lantern) Material.LANTERN.createBlockData();
            hanging.setHanging(true);
            setData(gx, topY - 1, gz, hanging);
            // path stub through the gate towards the plaza
            for (int i = R_PLAZA + 1; i < R_GATES + 3; i++) {
                for (int w = -1; w <= 1; w++) {
                    int sx = cx + dir[0] * i + px * w;
                    int sz = cz + dir[1] * i + pz * w;
                    int sy = world.getHighestBlockYAt(sx, sz);
                    Material ground = world.getBlockAt(sx, sy, sz).getType();
                    if (ground != Material.WATER) {
                        set(sx, sy, sz, w == 0 ? Material.CHERRY_PLANKS : Material.CALCITE);
                    }
                }
            }
        }
    }

    private void petals() {
        for (int x = cx - 19; x <= cx + 19; x++) {
            for (int z = cz - 19; z <= cz + 19; z++) {
                double r = dist(x, z);
                if (r < R_PLAZA + 2 || r > 19 || random.nextDouble() > 0.25) {
                    continue;
                }
                int y = world.getHighestBlockYAt(x, z);
                Block ground = world.getBlockAt(x, y, z);
                Block above = world.getBlockAt(x, y + 1, z);
                if ((ground.getType() == Material.GRASS_BLOCK) && above.getType() == Material.AIR) {
                    above.setType(Material.PINK_PETALS, false);
                }
            }
        }
    }
}
