package com.ferreusveritas.dynamictrees.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

/**
 * @author Harley O'Connor
 */
public interface GrowableBlock {

    class Info {
        private final LevelAccessor level;
        private final BlockPos pos;
        private final BlockState state;

        public Info(LevelAccessor level, BlockPos pos, BlockState state) {
            this.level = level;
            this.pos = pos;
            this.state = state;
        }

    }

    /**
     * Performs the default mature action for this block. This will be called on tick if the block is mature and
     * {@linkplain MatureAction mature action} is set to {@linkplain MatureAction#DEFAULT default}.
     */
    void performMatureAction(LevelAccessor level, BlockPos pos, BlockState state);

    default void drop(LevelAccessor level, BlockPos pos, BlockState state) {
        level.destroyBlock(pos, true);
    }

    default void rot(LevelAccessor level, BlockPos pos, BlockState state) {
        level.destroyBlock(pos, false);
    }

    /**
     * Checks if the block is supported. An unsupported growable block should drop.
     *
     * @return {@code true} if this block is supported
     */
    boolean isSupported(LevelReader level, BlockPos pos, BlockState state);

    /**
     * Defines what should happen when the fruit matures. A mature fruit is one that has reached its maximum age.
     */
    enum MatureAction {
        /**
         * Performs the default mature action (defined by the implementation of {@link
         * GrowableBlock#performMatureAction(LevelAccessor, BlockPos, BlockState)}) for the set block.
         */
        DEFAULT((block, info) -> {
            block.performMatureAction(info.level, info.pos, info.state);
        }),
        /**
         * Drops the fruit on the ground.
         */
        DROP((block, info) -> {
            block.drop(info.level, info.pos, info.state);
        }),
        /**
         * Rots the fruit by destroying it without dropping anything.
         */
        ROT((block, info) -> {
            block.rot(info.level, info.pos, info.state);
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
