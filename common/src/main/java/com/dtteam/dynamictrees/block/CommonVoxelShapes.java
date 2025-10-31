package com.dtteam.dynamictrees.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class CommonVoxelShapes {

    /**
     * Holds common {@link VoxelShape}s keyed by a string, allowing easy access via Json elements.
     */
    public static final Map<String, VoxelShape> SHAPES = new HashMap<>();

    public static final VoxelShape SLAB = Shapes.create(0,0,0,1,0.5,1);
    public static final VoxelShape TALL_SLAB = Shapes.create(0,0,0,1,0.75,1);
    public static final VoxelShape HALF_SLAB = Shapes.create(0,0,0,1,0.25,1);

    public static final VoxelShape SAPLING_TRUNK = Block.box(7D, 0D, 7D, 9D, 5D, 9D);
    public static final VoxelShape FAT_SAPLING_TRUNK = Block.box(6.5D, 0D, 6.5D, 9.5D, 5D, 9.5D);
    public static final VoxelShape SAPLING_LEAVES = Block.box(4D, 4D, 4D, 12D, 12D, 12D);
    public static final VoxelShape FAT_SAPLING_LEAVES = Block.box(3D, 4D, 3D, 13D, 14D, 13D);
    public static final VoxelShape SLIM_SAPLING_LEAVES = Block.box(5D, 5D, 5D, 11D, 14D, 11D);
    public static final VoxelShape MUSHROOM_CAP_FLAT = Block.box(4D, 5D, 4D, 12D, 8D, 12D);
    public static final VoxelShape FAT_MUSHROOM_CAP_FLAT = Block.box(3D, 5D, 3D, 13D, 9D, 13D);
    public static final VoxelShape MUSHROOM_CAP_ROUND = Block.box(5D, 3D, 5D, 11D, 8D, 11D);
    public static final VoxelShape FAT_MUSHROOM_CAP_ROUND = Block.box(4D, 3D, 4D, 12D, 9D, 12D);
    public static final VoxelShape MUSHROOM_RIM = Block.box(4D, 3D, 4D, 12D, 5D, 12D);
    public static final VoxelShape FAT_MUSHROOM_RIM = Block.box(3D, 3D, 3D, 13D, 5D, 13D);

    public static final VoxelShape SAPLING = Shapes.or(SAPLING_TRUNK, SAPLING_LEAVES);
    public static final VoxelShape SLIM_SAPLING = Shapes.or(SAPLING_TRUNK, SLIM_SAPLING_LEAVES);
    public static final VoxelShape FAT_SAPLING = Shapes.or(FAT_SAPLING_TRUNK, FAT_SAPLING_LEAVES);
    public static final VoxelShape FLAT_MUSHROOM = Shapes.or(SAPLING_TRUNK, MUSHROOM_CAP_FLAT);
    public static final VoxelShape FAT_FLAT_MUSHROOM = Shapes.or(FAT_SAPLING_TRUNK, FAT_MUSHROOM_CAP_FLAT);
    public static final VoxelShape ROUND_MUSHROOM = Shapes.or(SAPLING_TRUNK, MUSHROOM_CAP_ROUND);
    public static final VoxelShape FAT_ROUND_MUSHROOM = Shapes.or(FAT_SAPLING_TRUNK, FAT_MUSHROOM_CAP_ROUND);
    public static final VoxelShape ROUND_MUSHROOM_RIM = Shapes.or(SAPLING_TRUNK, MUSHROOM_CAP_ROUND, MUSHROOM_RIM);
    public static final VoxelShape FAT_ROUND_MUSHROOM_RIM = Shapes.or(FAT_SAPLING_TRUNK, FAT_MUSHROOM_CAP_ROUND, FAT_MUSHROOM_RIM);

    static {
        SHAPES.put("empty", Shapes.empty());
        SHAPES.put("block", Shapes.block());
        SHAPES.put("slab", SLAB);
        SHAPES.put("tall_slab", TALL_SLAB);
        SHAPES.put("half_slab", HALF_SLAB);
        SHAPES.put("sapling", SAPLING);
        SHAPES.put("slim_sapling", SLIM_SAPLING);
        SHAPES.put("fat_sapling", FAT_SAPLING);
        SHAPES.put("flat_mushroom", FLAT_MUSHROOM);
        SHAPES.put("fat_flat_mushroom", FAT_FLAT_MUSHROOM);
        SHAPES.put("round_mushroom", ROUND_MUSHROOM);
        SHAPES.put("fat_round_mushroom", FAT_ROUND_MUSHROOM);
        SHAPES.put("round_mushroom_rim", ROUND_MUSHROOM_RIM);
        SHAPES.put("fat_round_mushroom_rim", FAT_ROUND_MUSHROOM_RIM);
    }

}
