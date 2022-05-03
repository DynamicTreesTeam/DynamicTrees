package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.data.WaterRootGenerator;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author Max Hyper
 */
public class WaterSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(WaterSoilProperties::new);

    public WaterSoilProperties(final ResourceLocation registryName) {
        super(null, registryName);

        this.soilStateGenerator.reset(WaterRootGenerator::new);
    }

    @Override
    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootyWaterBlock(this, blockProperties);
    }

    @Override
    public Material getDefaultMaterial() {
        return Material.WATER;
    }

    @Override
    public BlockBehaviour.Properties getDefaultBlockProperties(Material material, MaterialColor materialColor) {
        return BlockBehaviour.Properties.copy(Blocks.WATER);
    }

    public static class RootyWaterBlock extends RootyBlock implements SimpleWaterloggedBlock {

        protected static final AABB WATER_ROOTS_AABB = new AABB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
        public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

        public RootyWaterBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(WATERLOGGED, true));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder.add(WATERLOGGED));
        }

        @Override
        public int getRadiusForConnection(BlockState state, BlockGetter reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return 1;
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
            BlockState upState = world.getBlockState(pos.above());
            if (TreeHelper.isBranch(upState)) {
                return TreeHelper.getBranch(upState).getFamily().getBranchItem()
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public float getHardness(BlockState state, BlockGetter worldIn, BlockPos pos) {
            return (float) (0.5 * DTConfigs.ROOTY_BLOCK_HARDNESS_MULTIPLIER.get());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return Shapes.create(WATER_ROOTS_AABB);
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
            return Shapes.empty();
        }

        @Override
        public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
            return false;
        }

        @Override
        public FluidState getFluidState(BlockState state) {
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }

        @Override
        public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
            if (stateIn.getValue(WATERLOGGED)) {
                worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
            }
            return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }

        @Override
        public BlockState getDecayBlockState(BlockState state, BlockGetter access, BlockPos pos) {
            if (state.hasProperty(WATERLOGGED) && !state.getValue(WATERLOGGED)) {
                return Blocks.AIR.defaultBlockState();
            }
            return super.getDecayBlockState(state, access, pos);
        }

        ///////////////////////////////////////////
        // RENDERING
        ///////////////////////////////////////////

        @Override
        public boolean getColorFromBark() {
            return true;
        }

        public boolean fallWithTree(BlockState state, Level world, BlockPos pos) {
            //The block is removed when this is checked because it means it got attached to a tree
            world.setBlockAndUpdate(pos, getDecayBlockState(state, world, pos));
            return true;
        }

    }

}
