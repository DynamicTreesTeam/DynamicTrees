package com.ferreusveritas.dynamictrees.block.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class PalmLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(PalmLeavesProperties::new);

    public PalmLeavesProperties(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(BlockBehaviour.Properties properties) {
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
        public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
            if (state.getBlock() == this) {
                int dist = state.getValue(DISTANCE);
                if ((dist == 1 || dist == 2) && state.getValue(DIRECTION) == 0) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    return;
                }
            }
            super.randomTick(state, level, pos, rand);
        }

        public DynamicPalmLeavesBlock(LeavesProperties leavesProperties, Properties properties) {
            super(leavesProperties, properties);
            registerDefaultState(defaultBlockState().setValue(DIRECTION, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
        public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return side == Direction.UP && from.getFamily().isCompatibleDynamicLeaves(Species.NULL_SPECIES, state, level, pos) ? fromRadius : 0;
        }

        @Override
        public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
            return branch.getFamily() == getFamily(state, level, pos) ? BranchBlock.setSupport(0, 1) : 0;
        }

        @Override
        public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
            return true;
        }

        @Override
        public BlockState getLeavesBlockStateForPlacement(LevelAccessor level, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
            for (CoordUtils.Surround surround : CoordUtils.Surround.values()) {
                BlockState offstate = level.getBlockState(pos.offset(surround.getOffset()));
                if (offstate.getBlock() == this && offstate.getValue(DISTANCE) == 3) {
                    return getDirectionState(leavesStateWithHydro, surround);
                }
            }
            return leavesStateWithHydro;
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
            AABB base = super.getOcclusionShape(state, level, pos).bounds();
            base.inflate(1, 0, 1);
            base.inflate(-1, -0, -1);
            return Shapes.create(base);
        }

    }

    @Override
    public LootTable.Builder createBlockDrops() {
        if (primitiveLeaves != null && getPrimitiveLeavesBlock().isPresent()) {
            return DTLootTableProvider.createPalmLeavesBlockDrops(primitiveLeaves.getBlock(), seedDropChances);
        }
        return DTLootTableProvider.createPalmLeavesDrops(seedDropChances, LootContextParamSets.BLOCK);
    }

    @Override
    public LootTable.Builder createDrops() {
        return DTLootTableProvider.createPalmLeavesDrops(seedDropChances, DTLootParameterSets.LEAVES);
    }

}
