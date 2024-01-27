package com.ferreusveritas.dynamictrees.block;

import com.ferreusveritas.dynamictrees.block.branch.BasicBranchBlock;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * An abstract class to allow for Blocks with dynamic hardness.
 * <p>
 * The main use of this class is {@link BasicBranchBlock}, as its hardness depends on the radius of the branch.
 *
 * @author Harley O'Connor
 */
public abstract class BlockWithDynamicHardness extends Block {

    public BlockWithDynamicHardness(Properties properties) {
        super(properties.destroyTime(2.0f));

        // Create and fill a new state container.
        final StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
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
     * @param level An {@link BlockGetter} instance.
     * @param pos   The {@link BlockPos}.
     * @return The hardness value.
     */
    public float getHardness(BlockState state, final BlockGetter level, final BlockPos pos) {
        return 2.0f;
    }

    /**
     * Sub-classes can override this method to return if there is a tile entity based on the blockstate.
     * Used for rooty blocks.
     * @param state
     * @return
     */
    public boolean hasTileEntity(BlockState state) {
        return state.getBlock() instanceof EntityBlock;
    }

    /**
     * Custom extension of {@link BlockState} to allow for dynamic hardness.
     */
    protected final class DynamicHardnessBlockState extends BlockState {

        public DynamicHardnessBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertiesToValueMap, MapCodec<BlockState> codec) {
            super(block, propertiesToValueMap, codec);
        }

        @Override
        public float getDestroySpeed(BlockGetter level, BlockPos pos) {
            return getHardness(this, level, pos);
        }

        public boolean hasBlockEntity() {
            return hasTileEntity(this);
        }

    }

}
