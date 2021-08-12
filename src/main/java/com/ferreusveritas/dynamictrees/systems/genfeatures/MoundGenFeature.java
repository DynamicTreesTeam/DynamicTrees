package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.GenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PreGenerationContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

public class MoundGenFeature extends GenFeature {

    private static final SimpleVoxmap moundMap = new SimpleVoxmap(5, 4, 5, new byte[]{
            0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0,
            0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0,
            0, 1, 1, 1, 0, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0
    }).setCenter(new BlockPos(2, 3, 2));

    public static final ConfigurationProperty<Integer> MOUND_CUTOFF_RADIUS = ConfigurationProperty.integer("mound_cutoff_radius");

    public MoundGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MOUND_CUTOFF_RADIUS);
    }

    @Override
    protected ConfiguredGenFeature createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MOUND_CUTOFF_RADIUS, 5);
    }

    /**
     * Used to create a 5x4x5 rounded mound that is one block higher than the ground surface. This is meant to replicate
     * the appearance of a root hill and gives generated surface roots a better appearance.
     *
     * @param configuration                The {@link ConfiguredGenFeature} instance.
     * @param context       The {@link GenerationContext}.
     * @return The modified {@link BlockPos} of the rooty dirt that is one block higher.
     */
    @Override
    protected BlockPos preGenerate(ConfiguredGenFeature configuration, PreGenerationContext context) {
        final IWorld world = context.world();
        BlockPos rootPos = context.pos();

        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) && context.isWorldGen()) {
            BlockState initialDirtState = world.getBlockState(rootPos);
            BlockState initialUnderState = world.getBlockState(rootPos.below());

            if (initialUnderState.getMaterial() == Material.AIR ||
                    (initialUnderState.getMaterial() != Material.DIRT && initialUnderState.getMaterial() != Material.STONE)
            ) {
                final Biome biome = world.getUncachedNoiseBiome(
                        rootPos.getX() >> 2,
                        rootPos.getY() >> 2,
                        rootPos.getZ() >> 2
                );
                initialUnderState = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
            }

            rootPos = rootPos.above();

            for (Cell cell : moundMap.getAllNonZeroCells()) {
                final BlockState placeState = cell.getValue() == 1 ? initialDirtState : initialUnderState;
                world.setBlock(rootPos.offset(cell.getPos()), placeState, 3);
            }
        }

        return rootPos;
    }

    /**
     * Creates a 3x2x3 cube of dirt around the base of the tree using blocks derived from the environment.  This is used
     * to cleanup the overhanging trunk that happens when a thick tree is generated next to a drop off.  Only runs when
     * the radius is greater than 8.
     */
    @Override
    protected boolean postGenerate(ConfiguredGenFeature configuration, PostGenerationContext context) {
        // A mound was already generated in preGen and worldgen test
        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) || !context.isWorldGen()) {
            return false;
        }

        final IWorld world = context.world();
        final BlockPos rootPos = context.pos();
        final BlockPos treePos = rootPos.above();
        final BlockState belowState = world.getBlockState(rootPos.below());

        // Place dirt blocks around rooty dirt block if tree has a > 8 radius.
        final BlockState branchState = world.getBlockState(treePos);
        if (TreeHelper.getTreePart(branchState).getRadius(branchState) > BranchBlock.MAX_RADIUS) {
            for (Surround dir : Surround.values()) {
                BlockPos dPos = rootPos.offset(dir.getOffset());
                world.setBlock(dPos, context.initialDirtState(), 3);
                world.setBlock(dPos.below(), belowState, 3);
            }
            return true;
        }

        return false;
    }
}
