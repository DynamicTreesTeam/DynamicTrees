package com.ferreusveritas.dynamictrees.block.rooty;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.data.AerialRootsSoilGenerator;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AerialRootsSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(AerialRootsSoilProperties::new);

    protected MangroveFamily family;
    public AerialRootsSoilProperties(final ResourceLocation registryName) {
        super(registryName);
        this.soilStateGenerator.reset(AerialRootsSoilGenerator::new);
    }

    public void setFamily(MangroveFamily family) {
        this.family = family;
    }

    public MangroveFamily getFamily() {
        return family;
    }

    @Override
    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootRootyBlock(this, blockProperties);
    }

    public static class RootRootyBlock extends RootyBlock implements SimpleWaterloggedBlock {

        protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
        public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

        public RootRootyBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(RADIUS, 8).setValue(WATERLOGGED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder.add(RADIUS, WATERLOGGED));
        }

        @Override
        public FluidState getFluidState(BlockState state) {
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }

        @Override
        public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
            if (stateIn.getValue(WATERLOGGED)) {
                level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
        }

        @Override
        public BlockState getDecayBlockState(BlockState state, BlockGetter level, BlockPos pos) {
            if (state.hasProperty(WATERLOGGED) && !state.getValue(WATERLOGGED)) {
                return Blocks.AIR.defaultBlockState();
            }
            return super.getDecayBlockState(state, level, pos);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            int radius = state.getValue(RADIUS);
            return Block.box(8-radius,0,8-radius,radius+8,16,radius+8);
        }

        protected void updateRadius (LevelAccessor level, BlockState state, BlockPos pos){
            int upRad = TreeHelper.getRadius(level, pos.above());
            if (upRad > 0){
                int thisRad = state.getValue(RADIUS);
                if (upRad != thisRad)
                    level.setBlock(pos, state.setValue(RADIUS, Math.min(upRad, 8)), 3);
            }
        }

        @Override
        public MapSignal startAnalysis(LevelAccessor level, BlockPos rootPos, MapSignal signal) {
            updateRadius(level, level.getBlockState(rootPos), rootPos);
            return super.startAnalysis(level, rootPos, signal);
        }

        @Override
        public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
            updateRadius(pLevel, pState, pPos);
            super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        }

        public boolean fallWithTree(BlockState state, Level level, BlockPos pos) {
            //The block is removed when this is checked because it means it got attached to a tree
            level.setBlockAndUpdate(pos, getDecayBlockState(state, level, pos));
            return true;
        }
    }


}
