package com.dtteam.dynamictrees.block.soil;

import net.minecraft.util.RandomSource;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author Max Hyper
 */
public class WaterSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(WaterSoilProperties::new);

    public WaterSoilProperties(final Identifier registryName) {
        super(null, registryName);

        this.soilStateGenerator.reset(blockStateGenerators.get(DynamicTrees.location("water_root_soil")));
    }

    protected SoilBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new SoilWaterBlock(this, blockProperties);
    }

    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.WATER);
    }

    public static class SoilWaterBlock extends SoilBlock implements SimpleWaterloggedBlock {

        protected static final AABB WATER_ROOTS_AABB = new AABB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
        public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

        public SoilWaterBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(WATERLOGGED, true));
        }

        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder.add(WATERLOGGED));
        }

        public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return 1;
        }

        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
            BlockState upState = level.getBlockState(pos.above());
            if (TreeHelper.isBranch(upState)) {
                return TreeHelper.getBranch(upState).getFamily().getBranchItem()
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
            return (float) (0.5 * DTConfigs.SERVER.rootyBlockHardnessMultiplier.get());
        }

        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return Shapes.create(WATER_ROOTS_AABB);
        }

        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
            return Shapes.empty();
        }

        public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
            return false;
        }

        public FluidState getFluidState(BlockState state) {
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }

        public BlockState updateShape(BlockState stateIn, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
            if (stateIn.getValue(WATERLOGGED)) {
                ticks.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return super.updateShape(stateIn, level, ticks, currentPos, facing, facingPos, facingState, random);
        }

        public BlockState getDecayBlockState(BlockState state, BlockGetter level, BlockPos pos) {
            if (state.hasProperty(WATERLOGGED) && !state.getValue(WATERLOGGED)) {
                return Blocks.AIR.defaultBlockState();
            }
            return super.getDecayBlockState(state, level, pos);
        }

        ///////////////////////////////////////////
        // RENDERING
        ///////////////////////////////////////////

        public boolean getColorFromBark() {
            return true;
        }

        public boolean fallWithTree(BlockState state, Level level, BlockPos pos, boolean hasRoots) {
            return true;
        }

    }

}
