package com.ferreusveritas.dynamictrees.blocks;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.lang.reflect.Field;

/**
 * @author Harley O'Connor
 */
public class BlockWithDynamicHardness extends Block {

    private static final String STATE_CONTAINER_FIELD_NAME = "stateContainer";

    public BlockWithDynamicHardness(Properties properties) {
        super(properties);

        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.fillStateContainer(builder);

        try {
            final Field stateContainer = Block.class.getDeclaredField(STATE_CONTAINER_FIELD_NAME);
            stateContainer.setAccessible(true);
            stateContainer.set(this, builder.func_235882_a_(Block::getDefaultState, DynamicHardnessBlockState::new));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}

        this.setDefaultState(this.stateContainer.getBaseState());
    }

    public float getHardness (IBlockReader world, BlockPos pos) {
        return 2.0f;
    }

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
