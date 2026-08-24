package net.magnaqore.core.spawn;

import java.util.Random;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

/**
 * MagnaQore castle spawn — fully procedural, no third-party builds.
 *
 * Layout (center C = world spawn area, footprint ~90x90):
 *  - curtain walls: square, half-size 36, h=9, battlements + walkway
 *  - 4 corner towers: r=6, h=18, cherry conical roofs
 *  - gatehouse (south): twin towers r=4, arched gate, MQ banners
 *  - keep (north part): 3 tiers (25x25 h12, 17x17 h9, 11x11 h8),
 *    beacon with purple beam through the spire
 *  - courtyard (south part): paved plaza, spawn podium (amethyst + end rod),
 *    lamp posts, benches, gardens
 * Palette: stone/deepslate texture noise + cherry wood + calcite + amethyst.
 */
public final class CastleSpawnBuilder {

    private static final int HALF = 36;          // curtain wall half-size
    private static final int WALL_H = 9;
    private static final int TOWER_R = 6;
    private static final int TOWER_H = 18;
    private static final int GATE_TOWER_R = 4;
    private static final int KEEP_HALF = 12;     // tier 1 half-size
    private static final int KEEP_H1 = 12;
    private static final int KEEP_H2 = 9;
    private static final int KEEP_H3 = 8;
    private static final int CLEAR_H = 60;

    private final World world;
    private final int cx;
    private final int cz;
    private final int y0;                        // castle ground level
    private final Random noise = new Random(20260824);

    private final int keepCz;
    private final int podiumCz;

    public CastleSpawnBuilder(World world, int cx, int cz) {
        this.world = world;
        this.cx = cx;
        this.cz = cz;
        this.y0 = world.getHighestBlockYAt(cx, cz);
        this.keepCz = cz - 16;
        this.podiumCz = cz + 14;
    }

    public Location build() {
        prepareTerrain();
        courtyardFloor();
        curtainWalls();
        cornerTowers();
        gatehouse();
        keep();
        podium();
        lampsAndBenches();
        gardens();
        outerPath();
        return new Location(world, cx + 0.5, y0 + 2, podiumCz + 3.5, 180f, 0f);
    }

    // ------------------------------------------------------------------ utils

    private void set(int x, int y, int z, Material m) {
        world.getBlockAt(x, y, z).setType(m, false);
    }

    private void setData(int x, int y, int z, BlockData d) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(d.getMaterial(), false);
        b.setBlockData(d, false);
    }

    private Material wallMat() {
        double r = noise.nextDouble();
        if (r < 0.62) return Material.STONE_BRICKS;
        if (r < 0.78) return Material.CRACKED_STONE_BRICKS;
        if (r < 0.90) return Material.ANDESITE;
        return Material.COBBLESTONE;
    }

    private Material keepMat() {
        double r = noise.nextDouble();
        if (r < 0.70) return Material.STONE_BRICKS;
        if (r < 0.82) return Material.POLISHED_ANDESITE;
        return Material.CRACKED_STONE_BRICKS;
    }

    private void stairs(int x, int y, int z, Material m, BlockFace facing, boolean top) {
        Stairs s = (Stairs) m.createBlockData();
        s.setFacing(facing);
        s.setHalf(top ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
        setData(x, y, z, s);
    }

    private void fillDown(int x, int yFrom, int z, Material m, int maxDepth) {
        for (int y = yFrom; y >= yFrom - maxDepth; y--) {
            Block b = world.getBlockAt(x, y, z);
            if (b.getType().isSolid() && !b.getType().name().contains("LEAVES")
                    && !b.getType().name().contains("LOG")) {
                break;
            }
            set(x, y, z, m);
        }
    }

    // ------------------------------------------------------------- big passes

    private void prepareTerrain() {
        int m = HALF + TOWER_R + 4;
        for (int x = cx - m; x <= cx + m; x++) {
            for (int z = cz - m; z <= cz + m; z++) {
                for (int y = y0 + 1; y <= y0 + CLEAR_H; y++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        set(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }

    private void courtyardFloor() {
        for (int x = cx - HALF + 1; x <= cx + HALF - 1; x++) {
            for (int z = cz - HALF + 1; z <= cz + HALF - 1; z++) {
                double d = Math.hypot(x - cx, z - podiumCz);
                Material floor;
                if (d <= 11 && Math.abs(d - 8) < 0.6) {
                    floor = Material.CALCITE;
                } else if (d <= 11) {
                    floor = Material.CHERRY_PLANKS;
                } else {
                    // stone paving with grass patches near edges
                    double r = noise.nextDouble();
                    if (r < 0.12) floor = Material.GRASS_BLOCK;
                    else if (r < 0.55) floor = Material.STONE_BRICKS;
                    else if (r < 0.75) floor = Material.ANDESITE;
                    else if (r < 0.9) floor = Material.POLISHED_ANDESITE;
                    else floor = Material.COBBLESTONE;
                }
                set(x, y0, z, floor);
                fillDown(x, y0 - 1, z, Material.DEEPSLATE, 10);
            }
        }
    }

    private void curtainWalls() {
        for (int i = -HALF; i <= HALF; i++) {
            wallColumn(cx + i, cz - HALF, false);            // north
            wallColumn(cx + i, cz + HALF, isGateSpan(cx + i)); // south (gate gap)
            wallColumn(cx - HALF, cz + i, false);            // west
            wallColumn(cx + HALF, cz + i, false);            // east
        }
        battlements();
    }

    private boolean isGateSpan(int x) {
        return Math.abs(x - cx) <= 3;
    }

    private void wallColumn(int x, int z, boolean gateGap) {
        // wall thickness 3: outer face, core, inner face
        int dxo = x == cx - HALF ? 1 : x == cx + HALF ? -1 : 0;
        int dzo = z == cz - HALF ? 1 : z == cz + HALF ? -1 : 0;
        for (int t = 0; t < 3; t++) {
            int wx = x + dxo * t;
            int wz = z + dzo * t;
            fillDown(wx, y0 - 1, wz, Material.DEEPSLATE_BRICKS, 12);
            int h = gateGap ? 0 : WALL_H;
            for (int y = 1; y <= h; y++) {
                // deepslate pillar accents every 6 blocks on the outer face
                boolean pillar = t == 0 && ((Math.abs(x - cx) % 6 == 0) || (Math.abs(z - cz) % 6 == 0));
                set(wx, y0 + y, wz, pillar ? Material.DEEPSLATE_BRICKS : wallMat());
            }
            if (gateGap && t < 3) {
                // gate passage floor
                set(wx, y0, wz, Material.POLISHED_DEEPSLATE);
            }
        }
        // arrow slits on the outer face, mid height, every 5 blocks
        if (!gateGap && (Math.abs(x - cx) % 5 == 2 || Math.abs(z - cz) % 5 == 2)) {
            set(x, y0 + 4, z, Material.AIR);
            set(x, y0 + 5, z, Material.AIR);
        }
    }

    private void battlements() {
        for (int i = -HALF; i <= HALF; i++) {
            merlon(cx + i, cz - HALF, i);
            if (!isGateSpan(cx + i)) {
                merlon(cx + i, cz + HALF, i);
            }
            merlon(cx - HALF, cz + i, i);
            merlon(cx + HALF, cz + i, i);
        }
        // walkway floor + inner railing
        for (int i = -HALF + 2; i <= HALF - 2; i++) {
            walkway(cx + i, cz - HALF + 2);
            if (!isGateSpan(cx + i)) {
                walkway(cx + i, cz + HALF - 2);
            }
            walkway(cx - HALF + 2, cz + i);
            walkway(cx + HALF - 2, cz + i);
        }
    }

    private void merlon(int x, int z, int idx) {
        set(x, y0 + WALL_H + 1, z, idx % 2 == 0 ? Material.DEEPSLATE_TILES : Material.AIR);
    }

    private void walkway(int x, int z) {
        set(x, y0 + WALL_H, z, Material.POLISHED_DEEPSLATE);
        Lantern l = (Lantern) Material.LANTERN.createBlockData();
        l.setHanging(false);
        if ((x + z) % 12 == 0) {
            setData(x, y0 + WALL_H + 1, z, l);
        }
    }

    private void cornerTowers() {
        int[][] corners = {{cx - HALF, cz - HALF}, {cx + HALF, cz - HALF},
                {cx - HALF, cz + HALF}, {cx + HALF, cz + HALF}};
        for (int[] c : corners) {
            tower(c[0], c[1], TOWER_R, TOWER_H, true);
        }
    }

    private void gatehouse() {
        tower(cx - 5, cz + HALF, GATE_TOWER_R, 13, false);
        tower(cx + 5, cz + HALF, GATE_TOWER_R, 13, false);
        // arch over the gate
        for (int dx = -3; dx <= 3; dx++) {
            for (int t = 0; t < 3; t++) {
                int z = cz + HALF - t;
                int top = Math.abs(dx) == 3 ? 5 : Math.abs(dx) == 2 ? 6 : 7;
                for (int y = top; y <= 9; y++) {
                    set(cx + dx, y0 + y, z, wallMat());
                }
                if (Math.abs(dx) == 2) {
                    stairs(cx + dx, y0 + top - 1, z, Material.STONE_BRICK_STAIRS,
                            dx < 0 ? BlockFace.WEST : BlockFace.EAST, true);
                }
            }
        }
        // banners on both sides of the gate (brand: purple with cyan gradient)
        banner(cx - 2, y0 + 8, cz + HALF - 3, BlockFace.NORTH);
        banner(cx + 2, y0 + 8, cz + HALF - 3, BlockFace.NORTH);
        banner(cx - 2, y0 + 8, cz + HALF + 1, BlockFace.SOUTH);
        banner(cx + 2, y0 + 8, cz + HALF + 1, BlockFace.SOUTH);
        // lanterns in the passage
        Lantern hang = (Lantern) Material.LANTERN.createBlockData();
        hang.setHanging(true);
        setData(cx, y0 + 4, cz + HALF - 1, hang);
    }

    private void banner(int x, int y, int z, BlockFace facing) {
        Directionalize:
        {
            Block b = world.getBlockAt(x, y, z);
            b.setType(Material.PURPLE_WALL_BANNER, false);
            if (b.getBlockData() instanceof org.bukkit.block.data.Directional dir) {
                dir.setFacing(facing);
                b.setBlockData(dir, false);
            }
            if (b.getState() instanceof Banner banner) {
                banner.addPattern(new Pattern(DyeColor.CYAN, PatternType.GRADIENT_UP));
                banner.addPattern(new Pattern(DyeColor.PURPLE, PatternType.RHOMBUS));
                banner.update(true, false);
            }
        }
    }

    private void tower(int tx, int tz, int r, int h, boolean roof) {
        for (int x = tx - r; x <= tx + r; x++) {
            for (int z = tz - r; z <= tz + r; z++) {
                double d = Math.hypot(x - tx, z - tz);
                if (d > r + 0.4) continue;
                fillDown(x, y0 - 1, z, Material.DEEPSLATE_BRICKS, 12);
                if (d > r - 1.2) {
                    // shell
                    for (int y = 0; y <= h; y++) {
                        set(x, y0 + y, z, wallMat());
                    }
                } else {
                    // hollow inside, floor
                    set(x, y0, z, Material.POLISHED_DEEPSLATE);
                    for (int y = 1; y <= h; y++) {
                        set(x, y0 + y, z, Material.AIR);
                    }
                }
            }
        }
        // windows at two heights, 4 directions
        for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            for (int wy : new int[]{h / 2, h - 3}) {
                set(tx + dir[0] * r, y0 + wy, tz + dir[1] * r, Material.AIR);
                set(tx + dir[0] * r, y0 + wy + 1, tz + dir[1] * r, Material.AIR);
            }
        }
        if (roof) {
            coneRoof(tx, tz, r + 1, y0 + h + 1);
        } else {
            // battlement top
            for (int x = tx - r; x <= tx + r; x++) {
                for (int z = tz - r; z <= tz + r; z++) {
                    double d = Math.hypot(x - tx, z - tz);
                    if (d <= r + 0.4 && d > r - 1.2 && (x + z) % 2 == 0) {
                        set(x, y0 + h + 1, z, Material.DEEPSLATE_TILES);
                    }
                }
            }
        }
    }

    private void coneRoof(int tx, int tz, int baseR, int yBase) {
        double r = baseR;
        int y = yBase;
        while (r > 0.4) {
            for (int x = tx - baseR; x <= tx + baseR; x++) {
                for (int z = tz - baseR; z <= tz + baseR; z++) {
                    double d = Math.hypot(x - tx, z - tz);
                    if (d <= r + 0.4 && d > r - 1.1) {
                        set(x, y, z, Material.CHERRY_PLANKS);
                    } else if (d <= r - 1.1 && y == yBase) {
                        set(x, y, z, Material.CHERRY_PLANKS); // seal the base
                    }
                }
            }
            r -= 0.75;
            y++;
        }
        set(tx, y, tz, Material.CHERRY_FENCE);
        Lantern l = (Lantern) Material.LANTERN.createBlockData();
        l.setHanging(false);
        setData(tx, y + 1, tz, l);
    }

    private void keep() {
        int[] halves = {KEEP_HALF, KEEP_HALF - 4, KEEP_HALF - 7};
        int[] heights = {KEEP_H1, KEEP_H2, KEEP_H3};
        int base = y0;
        for (int tier = 0; tier < 3; tier++) {
            int half = halves[tier];
            int h = heights[tier];
            for (int x = cx - half; x <= cx + half; x++) {
                for (int z = keepCz - half; z <= keepCz + half; z++) {
                    boolean shell = Math.abs(x - cx) == half || Math.abs(z - keepCz) == half;
                    boolean corner = Math.abs(x - cx) >= half - 1 && Math.abs(z - keepCz) >= half - 1;
                    if (tier == 0) {
                        fillDown(x, y0 - 1, z, Material.DEEPSLATE_BRICKS, 12);
                    }
                    for (int y = 1; y <= h; y++) {
                        if (shell) {
                            set(x, base + y, z, corner ? Material.DEEPSLATE_BRICKS : keepMat());
                        } else {
                            set(x, base + y, z, Material.AIR);
                        }
                    }
                    // tier floor
                    set(x, base + (tier == 0 ? 0 : 1) , z,
                            tier == 0 ? Material.POLISHED_DEEPSLATE : Material.CHERRY_PLANKS);
                    // tier top rim
                    if (shell) {
                        set(x, base + h + 1, z, (x + z) % 2 == 0 ? Material.DEEPSLATE_TILES : Material.AIR);
                    }
                }
            }
            // arched windows on each face of this tier
            keepWindows(base, half, h);
            base += h;
        }
        // spire with the beacon
        int topY = base + 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(cx + dx, topY - 1, keepCz + dz, Material.IRON_BLOCK);
                set(cx + dx, topY + 1, keepCz + dz, Material.AIR);
            }
        }
        set(cx, topY, keepCz, Material.BEACON);
        set(cx, topY + 1, keepCz, Material.PURPLE_STAINED_GLASS);
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                set(cx + dx, topY, keepCz + dz, Material.DEEPSLATE_TILES);
                set(cx + dx, topY + 1, keepCz + dz, Material.AMETHYST_BLOCK);
                set(cx + dx, topY + 2, keepCz + dz, Material.SMALL_AMETHYST_BUD);
            }
        }
        // entrance: south face of tier 1, arched, cherry doors opening
        for (int dx = -2; dx <= 2; dx++) {
            int top = Math.abs(dx) == 2 ? 3 : 4;
            for (int y = 1; y <= top; y++) {
                set(cx + dx, y0 + y, keepCz + KEEP_HALF, Material.AIR);
            }
        }
        // interior: ground hall lighting + central column
        for (int y = 1; y <= 3; y++) {
            set(cx, y0 + y, keepCz, Material.POLISHED_DEEPSLATE);
        }
        set(cx, y0 + 4, keepCz, Material.AMETHYST_BLOCK);
        Lantern hang = (Lantern) Material.LANTERN.createBlockData();
        hang.setHanging(true);
        setData(cx + 3, y0 + 5, keepCz, hang);
        setData(cx - 3, y0 + 5, keepCz, hang);
        setData(cx, y0 + 5, keepCz + 3, hang);
        setData(cx, y0 + 5, keepCz - 3, hang);
    }

    private void keepWindows(int base, int half, int h) {
        int wy = base + h / 2;
        for (int off = -half + 4; off <= half - 4; off += 5) {
            for (int y = wy; y <= wy + 2; y++) {
                set(cx + off, y, keepCz - half, Material.AIR);
                set(cx + off, y, keepCz + half, Material.AIR);
                set(cx - half, y, keepCz + off, Material.AIR);
                set(cx + half, y, keepCz + off, Material.AIR);
            }
        }
    }

    private void podium() {
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = podiumCz - 3; z <= podiumCz + 3; z++) {
                double d = Math.hypot(x - cx, z - podiumCz);
                if (d <= 2.4) {
                    set(x, y0 + 1, z, Material.CALCITE);
                } else if (d <= 3.4) {
                    set(x, y0 + 1, z, Material.DEEPSLATE_TILES);
                }
            }
        }
        set(cx, y0 + 2, podiumCz, Material.AMETHYST_BLOCK);
        set(cx, y0 + 3, podiumCz, Material.END_ROD);
    }

    private void lampsAndBenches() {
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(k * 45 + 22.5);
            int x = cx + (int) Math.round(9 * Math.sin(a));
            int z = podiumCz + (int) Math.round(9 * Math.cos(a));
            set(x, y0 + 1, z, Material.POLISHED_DEEPSLATE);
            set(x, y0 + 2, z, Material.CHERRY_FENCE);
            set(x, y0 + 3, z, Material.CHERRY_FENCE);
            Lantern l = (Lantern) Material.LANTERN.createBlockData();
            l.setHanging(false);
            setData(x, y0 + 4, z, l);
        }
    }

    private void gardens() {
        int[][] spots = {{cx - 24, cz + 20}, {cx + 24, cz + 20}, {cx - 24, cz - 2}, {cx + 24, cz - 2}};
        for (int[] s : spots) {
            for (int x = s[0] - 4; x <= s[0] + 4; x++) {
                for (int z = s[1] - 4; z <= s[1] + 4; z++) {
                    double d = Math.hypot(x - s[0], z - s[1]);
                    if (d > 4.4) continue;
                    set(x, y0, z, Material.GRASS_BLOCK);
                    if (d > 3.4) {
                        set(x, y0 + 1, z, Material.STONE_BRICK_WALL);
                    } else if (noise.nextDouble() < 0.5) {
                        set(x, y0 + 1, z, noise.nextDouble() < 0.6
                                ? Material.PINK_PETALS : Material.SHORT_GRASS);
                    }
                }
            }
            set(s[0], y0 + 1, s[1], Material.CHERRY_SAPLING);
        }
    }

    private void outerPath() {
        for (int i = 1; i <= 14; i++) {
            for (int w = -2; w <= 2; w++) {
                int x = cx + w;
                int z = cz + HALF + 2 + i;
                int y = world.getHighestBlockYAt(x, z);
                if (world.getBlockAt(x, y, z).getType() != Material.WATER) {
                    set(x, y, z, Math.abs(w) == 2 ? Material.CALCITE : Material.CHERRY_PLANKS);
                }
            }
        }
    }
}
