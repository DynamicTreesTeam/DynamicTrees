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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/**
 * An abstract class to allow for Blocks with dynamic hardness.
 *
 * The main use of this class is {@link BasicBranchBlock}, as its hardness depends on the radius of the branch.
 *
 * @author Harley O'Connor
 */
public abstract class BlockWithDynamicHardness extends Block {

    // Obfuscated field name of stateContainer in Block.class
    private static final String STATE_CONTAINER_FIELD_NAME = "field_176227_L";

    public BlockWithDynamicHardness(Properties properties) {
        super(properties);

        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.fillStateContainer(builder);

        try {
            // Grab the stateContainer field.
            final Field stateContainer = ObfuscationReflectionHelper.findField(Block.class, STATE_CONTAINER_FIELD_NAME);
            // Give us access to the stateContainer.
            stateContainer.setAccessible(true);
            // Set the state container to use our custom BlockState.
            stateContainer.set(this, builder.func_235882_a_(Block::getDefaultState, DynamicHardnessBlockState::new));
        } catch (IllegalAccessException ignored) {}

        this.setDefaultState(this.stateContainer.getBaseState());
    }

    /**
     * Sub-classes can override this method to return a hardness value that could, for example,
     * depend on the {@link BlockState}.
     *
     * @param world An {@link IBlockReader} world instance.
     * @param pos The {@link BlockPos}.
     * @return The hardness value.
     */
    public float getHardness (IBlockReader world, BlockPos pos) {
        return 2.0f;
    }

    /**
     * Custom extension of {@link BlockState} to allow for dynamic hardness.
     */
    protected final class DynamicHardnessBlockState extends BlockState {

        public DynamicHardnessBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertiesToValueMap, MapCodec<BlockState> codec) {
            super (block, propertiesToValueMap, codec);
        }

        @Override
        public float getBlockHardness (IBlockReader worldIn, BlockPos pos) {
            return getHardness(worldIn, pos);
        }
    }

}
