package com.ferreusveritas.dynamictrees.block.rooty;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AerialRootsSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(AerialRootsSoilProperties::new);

    public AerialRootsSoilProperties(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootRootyBlock(this, blockProperties);
    }

    public static class RootRootyBlock extends RootyBlock {

        protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);

        public RootRootyBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(RADIUS, 8));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FERTILITY, IS_VARIANT, RADIUS);
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
    }


}
