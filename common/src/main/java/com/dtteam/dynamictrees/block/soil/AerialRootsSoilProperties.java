package com.dtteam.dynamictrees.block.soil;

import net.minecraft.world.level.LevelReader;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.BlockWithDynamicHardness;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.animation.*;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.dtteam.dynamictrees.systems.nodemapper.RootIntegrityNode;
import com.dtteam.dynamictrees.tree.ChunkTreeHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.EntityUtils;
import com.dtteam.dynamictrees.utility.ItemUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
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
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AerialRootsSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(AerialRootsSoilProperties::new);

    protected UndergroundRootsFamily family;
    public AerialRootsSoilProperties(final Identifier registryName) {
        super(registryName);
        this.soilStateGenerator.reset(blockStateGenerators.get(DynamicTrees.location("aerial_root_soil")));
    }

    public void setFamily(UndergroundRootsFamily family) {
        this.family = family;
    }

    public UndergroundRootsFamily getFamily() {
        return family;
    }

    protected SoilBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootSoilBlock(this, blockProperties);
    }

    public BlockState getSoilState(BlockState primitiveSoilState, int fertility, boolean requireTileEntity){
        BlockState rootyState = super.getSoilState(primitiveSoilState, fertility, requireTileEntity);
        if (rootyState.getBlock() instanceof RootSoilBlock){
            return rootyState.setValue(RootSoilBlock.WATERLOGGED, primitiveSoilState.getFluidState().is(Fluids.WATER));
        }
        return rootyState;
    }

    public static class RootSoilBlock extends SoilBlock implements SimpleWaterloggedBlock {

        private static final int MIN_RADIUS = 1;
        private static final int MAX_RADIUS = 8;

        public static final IntegerProperty RADIUS = IntegerProperty.create("radius", MIN_RADIUS, MAX_RADIUS);
        public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

        public RootSoilBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(RADIUS, MAX_RADIUS).setValue(WATERLOGGED, false));
            soilBlockDecayer = (level, rootPos, rootyState, species) -> true;
        }

        public BlockState GetStateFromIndex(int index){
            if (index <= MAX_RADIUS && index >= MIN_RADIUS)
                return defaultBlockState().setValue(RADIUS, index);
            return defaultBlockState();
        }

        public int getStateIndex(BlockState state){
            if (!state.hasProperty(RADIUS)) return 0;
            return state.getValue(RADIUS);
        }

        public AerialRootsSoilProperties getSoilProperties() {
            return (AerialRootsSoilProperties) super.getSoilProperties();
        }

        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder.add(RADIUS, WATERLOGGED));
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

        public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
            BlockState up = level.getBlockState(pos.above());
            float hardness = 2.0f;
            if (up.getBlock() instanceof BlockWithDynamicHardness upBlock){
                hardness = upBlock.getHardness(up, level, pos.above());
            }
            return (float)(hardness * DTConfigs.SERVER.rootyBlockHardnessMultiplier.get());
        }

        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            int radius = state.getValue(RADIUS);
            return Block.box(8-radius,0,8-radius,radius+8,16,radius+8);
        }

        protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return getShape(state, level, pos, context);
        }

        protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return getShape(state, level, pos, context);
        }

        protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
            return getShape(state, level, pos, CollisionContext.empty());
        }

        public int getRadius(BlockState state) {
            return state.getValue(RADIUS);
        }

        public boolean isStructurallyUnstable(LevelAccessor level, BlockPos rootPos){
            BlockPos belowPos = rootPos.below();
            final RootIntegrityNode node = new RootIntegrityNode();
            BlockState belowState = level.getBlockState(belowPos);
            if (!TreeHelper.isTreePart(belowState)) return true;
            TreeHelper.getTreePart(belowState).analyse(belowState, level, belowPos, null, new MapSignal(node));
            return node.getStable().isEmpty();
        }

        public boolean fallWithTree(BlockState state, Level level, BlockPos pos, boolean hasRoots) {
            if (hasRoots){
                //tick would set this to a branch so we set it to air before that happens.
                level.setBlock(pos, getDecayBlockStateAir(state, level, pos), 2);
                return true;
            }
            return false;
        }

        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (!state.is(this)) return;

            if (isStructurallyUnstable(level, pos)){
                dropWholeTree(level, pos, null, FallingTreeEntity.DestroyType.HARVEST);
            } else {
                updateRadius(level, state, pos, 3, false);
            }

            super.tick(level.getBlockState(pos), level, pos, random);
        }

        public void updateTree(BlockState rootyState, Level level, BlockPos rootPos, RandomSource random, boolean natural) {
            int radOld = TreeHelper.getRadius(level, rootPos.offset(getTrunkDirection(level, rootPos).getUnitVec3i()));

            super.updateTree(rootyState, level, rootPos, random, natural);

            int radNew = TreeHelper.getRadius(level, rootPos.offset(getTrunkDirection(level, rootPos).getUnitVec3i()));
            //If the radius was updated, tick the root block
            if (radOld != radNew) level.scheduleTick(rootPos, this, 1);
        }

        //This makes the block decay like normal, usually just as air
        public BlockState getDecayBlockStateAir(BlockState state, BlockGetter level, BlockPos pos){
            if (state.hasProperty(WATERLOGGED)) {
                return getFluidState(state).createLegacyBlock();
            }
            return Blocks.AIR.defaultBlockState();
        }

        public BlockState getDecayBlockState(BlockState state, BlockGetter level, BlockPos pos) {
            BranchBlock branch = getSoilProperties().getFamily().getBranch().orElse(null);
            if (branch == null) return super.getDecayBlockState(state, level, pos);

            //Decay into the family's branch block. Might act weird but it looks nice.
            BlockState decay = branch.getStateForRadius(getRadius(state));
            if (state.hasProperty(WATERLOGGED) && state.getValue(WATERLOGGED) && decay.hasProperty(WATERLOGGED)) {
                return decay.setValue(WATERLOGGED, true);
            }
            return decay;
        }

        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            if (!level.isClientSide())
                this.dropWholeTree(level, pos, player, FallingTreeEntity.DestroyType.HARVEST);
            return level.isClientSide() ? level.setBlock(pos, fluid.createLegacyBlock(), 11) : level.removeBlock(pos, false);
        }

        public void dropWholeTree(Level level, BlockPos rootPos, @Nullable Player player, FallingTreeEntity.DestroyType destroyType){
            Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.above()));
            Optional<BranchBlock> roots = TreeHelper.getBranchOpt(level.getBlockState(rootPos.below()));

            BranchDestructionData destroyData = null;
            Optional<Direction> toolDir = Optional.empty();
            if (player != null) toolDir = Optional.of(EntityUtils.getHitDirection(player));
            if (toolDir.isPresent() && (toolDir.get().getAxis() == Direction.Axis.Y)) toolDir = Optional.empty();
            Direction fallingDir = toolDir.orElse(Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom()));

            if (branch.isPresent()) {
                destroyData = branch.get().destroyBranchFromNode(level, rootPos.above(), fallingDir, player == null, player);
            }
            if (roots.isPresent()) {
                BranchDestructionData rootDestroyData = roots.get().destroyBranchFromNode(level, rootPos.below(), fallingDir, player == null, player);
                if (destroyData == null){
                    destroyData = rootDestroyData;
                } else {
                    destroyData = destroyData.merge(rootDestroyData);
                }
            }
            if (destroyData != null){

                final ItemStack heldItem = player == null ? ItemStack.EMPTY : player.getMainHandItem();
                final int fortune = ItemUtils.getEnchantmentLevel(Enchantments.FORTUNE, heldItem, level.registryAccess());
                final float fortuneFactor = 1.0f + 0.25f * fortune;
                final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
                woodVolume.multiplyVolume(fortuneFactor);
                final List<ItemStack> woodItems = destroyData.species.getBranchesDrops(level, woodVolume, heldItem);

                FallingTreeEntity.dropTree(level, destroyData, woodItems, destroyType);

                BlockState rootState = level.getBlockState(rootPos);
                if (player != null)
                    ItemUtils.damageAxe(player, heldItem, getRadius(rootState), woodVolume, true);
                //The root is removed as soon as the tree spawns.
                //Give it a few ticks to allow the tree model to grab it first.
                level.scheduleTick(rootPos, this, FalloverAnimationHandler.TICKS_BEFORE_CHECKING_COLLISION - 1);
            }
        }

        public int updateRadius (LevelAccessor level, BlockState state, BlockPos pos, int flags, boolean force) {
            if (!(state.getBlock() instanceof RootSoilBlock)) return MAX_RADIUS;
            int upRad = TreeHelper.getRadius(level, pos.above());
            if (upRad > 0){
                int thisRad = state.getValue(RootSoilBlock.RADIUS);
                if (upRad != thisRad || force){
                    int newRadius = Math.min(upRad, MAX_RADIUS);
                    level.setBlock(pos, state.setValue(RootSoilBlock.RADIUS, newRadius), flags);
                    return newRadius;
                }
                return upRad;
            }
            return 0;
        }
    }

    public List<TagKey<Block>> defaultSoilBlockTags() {
        List<TagKey<Block>> defaultTags = new LinkedList<>(super.defaultSoilBlockTags());
        defaultTags.add(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL);
        return defaultTags;
    }


}
