package com.ferreusveritas.dynamictrees.api.treedata;

public record BranchShapeState(byte down, byte up, byte north, byte south, byte west, byte east, byte core){

    private static final int[] BLOCK_START = new int[8]; // start offsets for each family of shapes (by core)
    public static final int TOTAL_STATES;
    static {
        int sum = 0;
        for (int core = 1; core < 8; ++core) {
            int size = (int) Math.pow(core + 1, 6);
            BLOCK_START[core] = sum;
            sum += size;
        }
        TOTAL_STATES = sum; // 446,963
    }

    /**
     * This optimized indexing takes into account that each side radius
     * is capped by the core radius (side <= core).
     * That way the total size is reduced considerably.
     * @return the index where the voxel shape should be stored
     */
    public int toIndex() {
        final int base = core + 1;
        // compute mixed-radix local index for the six sides, down is most-significant, east is the least.
        // local = (((((down * base + up) * base + north) * base + south) * base + west) * base + east)
        int local = down;
        local = local * base + up;
        local = local * base + north;
        local = local * base + south;
        local = local * base + west;
        local = local * base + east;
        return BLOCK_START[core] + local;
    }

    public static BranchShapeState fromArray(byte[] radii){
        return new BranchShapeState(radii[0], radii[1], radii[2], radii[3], radii[4], radii[5], radii[6]);
    }

}