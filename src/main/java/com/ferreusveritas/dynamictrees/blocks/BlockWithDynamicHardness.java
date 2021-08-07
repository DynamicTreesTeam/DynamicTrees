package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.blocks.branches.BasicBranchBlock;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * An abstract class to allow for Blocks with dynamic hardness.
 * <p>
 * The main use of this class is {@link BasicBranchBlock}, as its hardness depends on the radius of the branch.
 *
 * @author Harley O'Connor
 */
public abstract class BlockWithDynamicHardness extends Block {

    public BlockWithDynamicHardness(Properties properties) {
        super(properties);

        // Create and fill a new state container.
        final StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.createBlockStateDefinition(builder);

        // Set the state container to use our custom BlockState class.
        this.stateDefinition = builder.create(Block::defaultBlockState, DynamicHardnessBlockState::new);

        // Sets the default state to the current default state, but with our new BlockState class.
        this.registerDefaultState(this.stateDefinition.any());
    }

    /**
     * Sub-classes can override this method to return a hardness value that could, for example, depend on the {@link
     * BlockState}.
     *
     * @param world An {@link IBlockReader} instance.
     * @param pos   The {@link BlockPos}.
     * @return The hardness value.
     */
    public float getHardness(final IBlockReader world, final BlockPos pos) {
        return 2.0f;
    }

    /**
     * Custom extension of {@link BlockState} to allow for dynamic hardness.
     */
    protected final class DynamicHardnessBlockState extends BlockState {

        public DynamicHardnessBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertiesToValueMap, MapCodec<BlockState> codec) {
            super(block, propertiesToValueMap, codec);
        }

        @Override
        public float getDestroySpeed(IBlockReader worldIn, BlockPos pos) {
            return getHardness(worldIn, pos);
        }

    }

}
