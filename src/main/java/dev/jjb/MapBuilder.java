package dev.jjb;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Строит 4 арены-региона высоко над миром. Центры заданы в REGIONS. */
public final class MapBuilder {
    private MapBuilder() {}

    public static final int Y = 100;
    public static final int HALF = 20; // арена 41x41

    public static final String[] NAMES = {
            "Город Сиросава", "Пустошь Разлома", "Белый Квартал", "Цитадель Пустоты"
    };

    /** Центры регионов (x, z). */
    public static final int[][] REGIONS = {
            {0, 0}, {400, 0}, {400, 400}, {0, 400}
    };

    private static final Block[] FLOOR = {
            Blocks.STONE_BRICKS, Blocks.SANDSTONE, Blocks.QUARTZ_BLOCK, Blocks.POLISHED_BLACKSTONE
    };
    private static final Block[] WALL = {
            Blocks.DEEPSLATE_BRICK_WALL, Blocks.CUT_SANDSTONE, Blocks.QUARTZ_PILLAR, Blocks.OBSIDIAN
    };
    private static final Block[] LIGHT = {
            Blocks.LANTERN, Blocks.SEA_LANTERN, Blocks.SEA_LANTERN, Blocks.SOUL_LANTERN
    };

    public static void buildAll(ServerWorld w) {
        for (int i = 0; i < REGIONS.length; i++) build(w, i);
    }

    public static void build(ServerWorld w, int region) {
        int cx = REGIONS[region][0];
        int cz = REGIONS[region][1];
        Block floor = FLOOR[region];
        Block wall = WALL[region];
        Block light = LIGHT[region];

        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                BlockPos floorPos = new BlockPos(cx + dx, Y, cz + dz);
                w.setBlockState(floorPos, floor.getDefaultState());
                // очистка пространства над ареной
                for (int dy = 1; dy <= 8; dy++) {
                    w.setBlockState(floorPos.up(dy), Blocks.AIR.getDefaultState());
                }
                boolean edge = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                if (edge) {
                    for (int dy = 1; dy <= 3; dy++) {
                        w.setBlockState(floorPos.up(dy), wall.getDefaultState());
                    }
                }
            }
        }
        // колонны по углам и в центре
        int[][] pillars = {{-12, -12}, {12, -12}, {-12, 12}, {12, 12}, {0, 0}};
        for (int[] p : pillars) {
            for (int dy = 1; dy <= 6; dy++) {
                w.setBlockState(new BlockPos(cx + p[0], Y + dy, cz + p[1]), wall.getDefaultState());
            }
            w.setBlockState(new BlockPos(cx + p[0], Y + 7, cz + p[1]), light.getDefaultState());
        }
    }

    public static BlockPos center(int region) {
        return new BlockPos(REGIONS[region][0], Y + 1, REGIONS[region][1]);
    }

    public static BlockPos landmark(int region) {
        return new BlockPos(REGIONS[region][0] + 16, Y + 1, REGIONS[region][1] + 16);
    }
}
