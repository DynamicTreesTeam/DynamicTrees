package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class PalmLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(PalmLeavesProperties::new);

    public PalmLeavesProperties(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(AbstractBlock.Properties properties) {
        return new DynamicPalmLeavesBlock(this, properties);
    }

    public static class DynamicPalmLeavesBlock extends DynamicLeavesBlock {

        public static final IntegerProperty DIRECTION = IntegerProperty.create("direction", 0, 8);

        public static final CoordUtils.Surround[][] hydroSurroundMap = new CoordUtils.Surround[][]{
                {}, //distance 0
                {CoordUtils.Surround.NE, CoordUtils.Surround.SE, CoordUtils.Surround.SW, CoordUtils.Surround.NW}, //distance 1
                {CoordUtils.Surround.N, CoordUtils.Surround.E, CoordUtils.Surround.S, CoordUtils.Surround.W}, //distance 2
                {}, //distance 3
                {}, //distance 4
                {}, //distance 5
                {}, //distance 6
                {}  //distance 7
        };

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
            if (state.getBlock() == this) {
                int dist = state.getValue(DISTANCE);
                if ((dist == 1 || dist == 2) && state.getValue(DIRECTION) == 0) {
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    return;
                }
            }
            super.randomTick(state, world, pos, rand);
        }

        public DynamicPalmLeavesBlock(LeavesProperties leavesProperties, Properties properties) {
            super(leavesProperties, properties);
            registerDefaultState(defaultBlockState().setValue(DIRECTION, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(DIRECTION);
        }

        public static BlockState getDirectionState(BlockState state, CoordUtils.Surround surround) {
            if (state == null) {
                return null;
            }
            return state.setValue(DIRECTION, surround == null ? 0 : surround.ordinal() + 1);
        }

        @Override
        public int getRadiusForConnection(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return side == Direction.UP && from.getFamily().isCompatibleDynamicLeaves(Species.NULL_SPECIES, state, reader, pos) ? fromRadius : 0;
        }

        @Override
        public int branchSupport(BlockState state, IBlockReader reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
            return branch.getFamily() == getFamily(state, reader, pos) ? BranchBlock.setSupport(0, 1) : 0;
        }

        @Override
        public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
            return true;
        }

        @Override
        public BlockState getLeavesBlockStateForPlacement(IWorld world, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
            for (CoordUtils.Surround surround : CoordUtils.Surround.values()) {
                BlockState offstate = world.getBlockState(pos.offset(surround.getOffset()));
                if (offstate.getBlock() == this && offstate.getValue(DISTANCE) == 3) {
                    return getDirectionState(leavesStateWithHydro, surround);
                }
            }
            return leavesStateWithHydro;
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
            AxisAlignedBB base = super.getOcclusionShape(state, world, pos).bounds();
            base.inflate(1, 0, 1);
            base.inflate(-1, -0, -1);
            return VoxelShapes.create(base);
        }

    }

}
