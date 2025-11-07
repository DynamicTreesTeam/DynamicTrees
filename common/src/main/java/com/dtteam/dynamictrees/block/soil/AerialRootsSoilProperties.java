package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.BlockWithDynamicHardness;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.animation.FalloverAnimationHandler;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.dtteam.dynamictrees.systems.nodemapper.RootIntegrityNode;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.dtteam.dynamictrees.utility.EntityUtils;
import com.dtteam.dynamictrees.utility.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
    public AerialRootsSoilProperties(final ResourceLocation registryName) {
        super(registryName);
        this.soilStateGenerator.reset(blockStateGenerators.get(DynamicTrees.location("aerial_root_soil")));
    }

    public void setFamily(UndergroundRootsFamily family) {
        this.family = family;
    }

    public UndergroundRootsFamily getFamily() {
        return family;
    }

    @Override
    protected SoilBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootSoilBlock(this, blockProperties);
    }

    @Override
    public BlockState getSoilState(BlockState primitiveSoilState, int fertility, boolean requireTileEntity){
        BlockState rootyState = super.getSoilState(primitiveSoilState, fertility, requireTileEntity);
        if (rootyState.getBlock() instanceof RootSoilBlock){
            return rootyState.setValue(RootSoilBlock.WATERLOGGED, primitiveSoilState.getFluidState().is(Fluids.WATER));
        }
        return rootyState;
    }

    @Override
    public boolean inheritsPrimitiveProperties() {
        return false;
    }

    public static class RootSoilBlock extends SoilBlock implements SimpleWaterloggedBlock {

        public static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
        public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

        public RootSoilBlock(SoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
            registerDefaultState(defaultBlockState().setValue(RADIUS, 8).setValue(WATERLOGGED, false));
        }

        @Override
        public AerialRootsSoilProperties getSoilProperties() {
            return (AerialRootsSoilProperties) super.getSoilProperties();
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
        public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
            BlockState up = level.getBlockState(pos.above());
            float hardness = 2.0f;
            if (up.getBlock() instanceof BlockWithDynamicHardness upBlock){
                hardness = upBlock.getHardness(up, level, pos.above());
            }
            return (float)(hardness * Services.CONFIG.getDoubleConfig(IConfigHelper.ROOTY_BLOCK_HARDNESS_MULTIPLIER));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            int radius = state.getValue(RADIUS);
            return Block.box(8-radius,0,8-radius,radius+8,16,radius+8);
        }

        @Override
        public int getRadius(BlockState state) {
            return state.getValue(RADIUS);
        }

        public boolean isStructurallyUnstable(LevelAccessor level, BlockPos rootPos){
            BlockPos belowPos = rootPos.below();
            final RootIntegrityNode node = new RootIntegrityNode();
            BlockState belowState = level.getBlockState(belowPos);
            if (!TreeHelper.isTreePart(belowState)) return true;
            TreeHelper.getTreePart(belowState).analyse(belowState, level, belowPos, null, new MapSignal(node)); // Analyze entire tree network to find root node and species.
            return node.getStable().isEmpty();
        }

        @Override
        public boolean fallWithTree(BlockState state, Level level, BlockPos pos, boolean hasRoots) {
            if (hasRoots && level.isClientSide()){
                //This only removes the block on the client side!
                //The actual removal of the root block is handled by tick.
                level.setBlock(pos, getDecayBlockStateAir(state, level, pos), 2);
                return true;
            }
            return false;
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (!state.is(this)) return; //The root has already been destroyed / removed.
            boolean removed = false;
            //If no roots support it then drop the whole tree.
            if (isStructurallyUnstable(level, pos)){
                dropWholeTree(level, pos, null, FallingTreeEntity.DestroyType.HARVEST);
                removed = true;
            }
            //if the species is not valid then this block should be removed asap
            if (!getSpecies(state, level, pos).isValid()){
                level.setBlockAndUpdate(pos, getDecayBlockStateAir(state, level, pos));
                removed = true;
            }
            if (!removed){
                updateRadius(level, state, pos, 3, false);
            }
            super.tick(state, level, pos, random);
        }

        @Override
        public void updateTree(BlockState rootyState, Level level, BlockPos rootPos, RandomSource random, boolean natural) {
            int radOld = TreeHelper.getRadius(level, rootPos.offset(getTrunkDirection(level, rootPos).getNormal()));
            super.updateTree(rootyState, level, rootPos, random, natural);
            int radNew = TreeHelper.getRadius(level, rootPos.offset(getTrunkDirection(level, rootPos).getNormal()));
            //If the radius was updated, tick the root block
            if (radOld != radNew) level.scheduleTick(rootPos, this, 1);
        }

        //This makes the block decay like normal, usually just as air
        public BlockState getDecayBlockStateAir(BlockState state, BlockGetter level, BlockPos pos){
            if (state.hasProperty(WATERLOGGED)) {
                return getFluidState(state).createLegacyBlock();
            }
            return super.getDecayBlockState(state, level, pos);
        }

        @Override
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

        /**
         * Called when a player removes a block.
         * This is responsible for actually destroying the block, and the block is intact at time of call.
         * This is called regardless of whether the player can harvest the block or not.
         * @return true if the block is actually destroyed.
         * Note: When used in multiplayer, this is called on both client and server sides!
         */
        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            if (!level.isClientSide)
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
                destroyData = branch.get().destroyBranchFromNode(level, rootPos.above(), fallingDir, false, player);
            }
            if (roots.isPresent()) {
                BranchDestructionData rootDestroyData = roots.get().destroyBranchFromNode(level, rootPos.below(), fallingDir, false, player);
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

        @Override
        public int updateRadius (LevelAccessor level, BlockState state, BlockPos pos, int flags, boolean force) {
            if (!(state.getBlock() instanceof RootSoilBlock)) return 8;
            int upRad = TreeHelper.getRadius(level, pos.above());
            if (upRad > 0){
                int thisRad = state.getValue(RootSoilBlock.RADIUS);
                if (upRad != thisRad || force){
                    int newRadius = Math.min(upRad, 8);
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
