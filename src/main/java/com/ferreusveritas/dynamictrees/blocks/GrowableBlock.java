package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.function.BiConsumer;

/**
 * @author Harley O'Connor
 */
public interface GrowableBlock {

    class Info {
        private final IWorld world;
        private final BlockPos pos;
        private final BlockState state;

        public Info(IWorld world, BlockPos pos, BlockState state) {
            this.world = world;
            this.pos = pos;
            this.state = state;
        }

    }

    /**
     * Performs the default mature action for this block. This will be called on tick if the block is mature and
     * {@linkplain MatureAction mature action} is set to {@linkplain MatureAction#DEFAULT default}.
     */
    void performMatureAction(IWorld world, BlockPos pos, BlockState state);

    default void drop(IWorld world, BlockPos pos, BlockState state) {
        world.destroyBlock(pos, true);
    }

    default void rot(IWorld world, BlockPos pos, BlockState state) {
        world.destroyBlock(pos, false);
    }

    /**
     * Checks if the block is supported. An unsupported growable block should drop.
     *
     * @return {@code true} if this block is supported
     */
    boolean isSupported(IBlockReader world, BlockPos pos, BlockState state);

    /**
     * Defines what should happen when the fruit matures. A mature fruit is one that has reached its maximum age.
     */
    enum MatureAction {
        /**
         * Performs the default mature action (defined by the implementation of {@link
         * GrowableBlock#performMatureAction(IWorld, BlockPos, BlockState)}) for the set block.
         */
        DEFAULT((block, info) -> {
            block.performMatureAction(info.world, info.pos, info.state);
        }),
        /**
         * Drops the fruit on the ground.
         */
        DROP((block, info) -> {
            block.drop(info.world, info.pos, info.state);
        }),
        /**
         * Rots the fruit by destroying it without dropping anything.
         */
        ROT((block, info) -> {
            block.rot(info.world, info.pos, info.state);
        });

        private final BiConsumer<GrowableBlock, Info> action;

        MatureAction(BiConsumer<GrowableBlock, Info> action) {
            this.action = action;
        }

        public void perform(GrowableBlock block, Info blockInfo) {
            action.accept(block, blockInfo);
        }
    }

}
