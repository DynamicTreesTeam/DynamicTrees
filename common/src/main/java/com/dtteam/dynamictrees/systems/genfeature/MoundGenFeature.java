package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.systems.genfeature.context.GenFeatureContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PreGenerationContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.utility.CoordUtils.Surround;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MoundGenFeature extends GenFeature {

    private static final SimpleVoxmap moundMap = new SimpleVoxmap(5, 4, 5, new byte[]{
            0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0,
            0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0,
            0, 1, 1, 1, 0, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0
    }).setCenter(new BlockPos(2, 3, 2));

    public static final ConfigurationProperty<Integer> MOUND_CUTOFF_RADIUS = ConfigurationProperty.integer("mound_cutoff_radius");
    public static final ConfigurationProperty<Boolean> OFFSET = ConfigurationProperty.bool("offset");

    public MoundGenFeature(Identifier registryName) {
        super(registryName);
    }

    protected void registerProperties() {
        this.register(MOUND_CUTOFF_RADIUS, OFFSET);
    }

    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MOUND_CUTOFF_RADIUS, 5)
                .with(OFFSET, true);
    }

    /**
     * Used to create a 5x4x5 rounded mound that is one block higher than the ground surface. This is meant to replicate
     * the appearance of a root hill and gives generated surface roots a better appearance.
     *
     * @param configuration                The {@link GenFeatureConfiguration} instance.
     * @param context       The {@link GenFeatureContext}.
     * @return The modified {@link BlockPos} of the rooty dirt that is one block higher.
     */
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        final LevelAccessor level = context.level();
        BlockPos rootPos = context.pos();

        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) && context.isWorldGen()) {
            BlockState initialDirtState = level.getBlockState(rootPos);
            BlockState initialUnderState = level.getBlockState(rootPos.below());

            if (configuration.get(OFFSET)){
                rootPos = rootPos.above();
            }

            for (SimpleVoxmap.VoxmapCell cell : moundMap.getAllNonZeroCells()) {
                final BlockState placeState = cell.getValue() == 1 ? initialDirtState : initialUnderState;
                level.setBlock(rootPos.offset(cell.getPos()), placeState, 3);
            }
        }

        return rootPos;
    }

    /**
     * Creates a 3x2x3 cube of dirt around the base of the tree using blocks derived from the environment.  This is used
     * to cleanup the overhanging trunk that happens when a thick tree is generated next to a drop off.  Only runs when
     * the radius is greater than 8.
     */
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        // A mound was already generated in preGen and worldgen test
        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) || !context.isWorldGen()) {
            return false;
        }

        final LevelAccessor level = context.level();
        final BlockPos rootPos = context.pos();
        final BlockPos treePos = rootPos.above();
        final BlockState belowState = level.getBlockState(rootPos.below());

        // Place dirt blocks around rooty dirt block if tree has a > 8 radius.
        final BlockState branchState = level.getBlockState(treePos);
        if (TreeHelper.getTreePart(branchState).getRadius(branchState) > BranchBlock.MAX_RADIUS) {
            for (Surround dir : Surround.values()) {
                BlockPos dPos = rootPos.offset(dir.getOffset());
                level.setBlock(dPos, context.initialDirtState(), 3);
                level.setBlock(dPos.below(), belowState, 3);
            }
            return true;
        }

        return false;
    }
}
