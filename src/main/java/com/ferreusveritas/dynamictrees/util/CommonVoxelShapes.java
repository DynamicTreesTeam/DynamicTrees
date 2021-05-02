package com.ferreusveritas.dynamictrees.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class CommonVoxelShapes {

    /** Holds common {@link VoxelShape}s keyed by a string, allowing easy access via Json elements. */
    public static final Map<String, VoxelShape> SHAPES = new HashMap<>();

    public static final VoxelShape SAPLING_TRUNK = Block.box(7D, 0D, 7D, 9D, 5D, 9D);
    public static final VoxelShape SAPLING_LEAVES = Block.box(4D, 4D, 4D, 12D, 12D, 12D);
    public static final VoxelShape SLIM_SAPLING_LEAVES = Block.box(5D, 4D, 5D, 11D, 14D, 11D);
    public static final VoxelShape MUSHROOM_STEM = Block.box(7D, 0D, 7D, 9D, 5D, 9D);
    public static final VoxelShape MUSHROOM_CAP_FLAT = Block.box(4D, 5D, 4D, 12D, 8D, 12D);
    public static final VoxelShape MUSHROOM_CAP_ROUND = Block.box(5D, 3D, 5D, 11D, 8D, 11D);
    public static final VoxelShape MUSHROOM_BRIM_E = Block.box(11D, 3D, 5D, 12D, 5D, 11D);
    public static final VoxelShape MUSHROOM_BRIM_W = Block.box(4D, 3D, 5D, 5D, 5D, 11D);
    public static final VoxelShape MUSHROOM_BRIM_S = Block.box(4D, 3D, 11D, 12D, 5D, 12D);
    public static final VoxelShape MUSHROOM_BRIM_N = Block.box(4D, 3D, 4D, 12D, 5D, 5D);

    public static final VoxelShape SAPLING = VoxelShapes.or(SAPLING_TRUNK, SAPLING_LEAVES);
    public static final VoxelShape SLIM_SAPLING = VoxelShapes.or(SAPLING_TRUNK, SLIM_SAPLING_LEAVES);
    public static final VoxelShape FLAT_MUSHROOM = VoxelShapes.or(MUSHROOM_STEM, MUSHROOM_CAP_FLAT);
    public static final VoxelShape ROUND_MUSHROOM = VoxelShapes.or(MUSHROOM_STEM, MUSHROOM_CAP_ROUND);
    public static final VoxelShape ROUND_MUSHROOM_RIM = VoxelShapes.or(MUSHROOM_STEM, MUSHROOM_CAP_ROUND, MUSHROOM_BRIM_E, MUSHROOM_BRIM_W, MUSHROOM_BRIM_S, MUSHROOM_BRIM_N);

    static {
        SHAPES.put("empty", VoxelShapes.empty());
        SHAPES.put("block", VoxelShapes.block());
        SHAPES.put("sapling", SAPLING);
        SHAPES.put("slim_sapling", SLIM_SAPLING);
        SHAPES.put("flat_mushroom", FLAT_MUSHROOM);
        SHAPES.put("round_mushroom", ROUND_MUSHROOM);
        SHAPES.put("round_mushroom_rim", ROUND_MUSHROOM_RIM);
    }

}
