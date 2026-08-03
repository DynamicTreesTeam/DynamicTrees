package com.dtteam.dynamictrees.api.treedata;

import net.minecraft.core.Direction;

/**
 * params in order of {@link Direction#get2DDataValue()}
 */
public record SurfaceRootShapeState(byte south, byte west, byte north, byte east, byte core) {

    private static final int[] BLOCK_START = new int[9]; // indices 1 to 8
    public static final int TOTAL_STATES;

    static {
        int sum = 0;
        for (int core = 1; core <= 8; ++core) {
            // Each horizontal side: 0 (no connection) or 1 to core => (core + 1) values
            int size = (int) Math.pow(core + 1, 4);
            BLOCK_START[core] = sum;
            sum += size;
        }
        TOTAL_STATES = sum; // 15,914
    }

    /**
     * Mixed-radix index over 4 horizontal sides.
     * Each side value is in [0, core], where 0 means no connection.
     */
    public int toIndex() {
        final int base = core + 1;
        int local = north;
        local = local * base + south;
        local = local * base + west;
        local = local * base + east;
        return BLOCK_START[core] + local;
    }

    public static SurfaceRootShapeState fromArray(byte[] radii) {
        return new SurfaceRootShapeState(radii[0], radii[1], radii[2], radii[3], radii[4]);
    }
}