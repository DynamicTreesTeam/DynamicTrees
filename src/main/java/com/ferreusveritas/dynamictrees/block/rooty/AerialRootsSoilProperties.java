package com.ferreusveritas.dynamictrees.block.rooty;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.data.AerialRootsSoilGenerator;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.RootIntegrityNode;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.EntityUtils;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public static int updateRadius (LevelAccessor level, BlockState state, BlockPos pos, int flags){
        if (!(state.getBlock() instanceof RootRootyBlock)) return 8;
        int upRad = TreeHelper.getRadius(level, pos.above());
        if (upRad > 0){
            int thisRad = state.getValue(RootRootyBlock.RADIUS);
            if (upRad != thisRad){
                int newRadius = Math.min(upRad, 8);
                level.setBlock(pos, state.setValue(RootRootyBlock.RADIUS, newRadius), flags);
                return newRadius;
            }
            return upRad;
        }
        return 0;
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



        public boolean isStructurallyStable(LevelAccessor level, BlockPos rootPos){
            BlockPos belowPos = rootPos.below();
            final RootIntegrityNode node = new RootIntegrityNode();
            BlockState belowState = level.getBlockState(belowPos);
            TreeHelper.getTreePart(belowState).analyse(belowState, level, belowPos, null, new MapSignal(node)); // Analyze entire tree network to find root node and species.
            if (node.getStable().isEmpty())
                return false;
            else {
                System.out.println("found stable: "+ node.getStable().get(0));
                return true;
            }
        }

        @Override
        public MapSignal startAnalysis(LevelAccessor level, BlockPos rootPos, MapSignal signal) {
            updateRadius(level, level.getBlockState(rootPos), rootPos, 3);
            return super.startAnalysis(level, rootPos, signal);
        }

        @Override
        public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
            updateRadius(pLevel, pState, pPos, 3);
            super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        }

        public boolean fallWithTree(BlockState state, Level level, BlockPos pos) {
            //The block is removed when this is checked because it means it got attached to a tree
            level.setBlockAndUpdate(pos, getDecayBlockState(state, level, pos));
            return true;
        }

//        @Override
//        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
//            playerWillDestroy(level, pos, state, player);
//            //Do not destroy roots if broken, we will do that
//            return false;
//        }

        public void destroyTree(Level level, BlockPos rootPos, @Nullable Player player) {
            Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.above()));
            Optional<BranchBlock> root = TreeHelper.getBranchOpt(level.getBlockState(rootPos.below()));

            if (branch.isPresent()) {
                BranchDestructionData destroyData = branch.get().destroyBranchFromNode(level, rootPos.above(), Direction.DOWN, true, null);
                FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
            }
            if (root.isPresent()) {
                BranchDestructionData destroyData = root.get().destroyBranchFromNode(level, rootPos.below(), Direction.UP, true, null);
                FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
            }
        }

        @Override
        public void playerWillDestroy(Level level, @Nonnull BlockPos pos, BlockState state, @Nonnull Player player) {
            this.dropWholeTree(level, pos, player);
            super.playerWillDestroy(level, pos, state, player);
        }

        public void dropWholeTree(Level level, BlockPos rootPos, @Nullable Player player){
            Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.above()));
            Optional<BranchBlock> root = TreeHelper.getBranchOpt(level.getBlockState(rootPos.below()));

            BranchDestructionData destroyData = null;
            Optional<Direction> toolDir = Optional.empty();
            if (player != null){
                final double reachDistance = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue();
                final BlockHitResult ragTraceResult = EntityUtils.playerRayTrace(player, reachDistance, 1.0F);
                toolDir = Optional.of(ragTraceResult != null ? (player.isShiftKeyDown() ? ragTraceResult.getDirection().getOpposite() : ragTraceResult.getDirection()) : Direction.DOWN);
            }

            if (branch.isPresent()) {
                destroyData = branch.get().destroyBranchFromNode(level, rootPos.above(), toolDir.orElse(Direction.DOWN), false, player);
            }
            if (root.isPresent()) {
                BranchDestructionData rootDestroyData = root.get().destroyBranchFromNode(level, rootPos.below(), toolDir.orElse(Direction.UP), false, player);
                if (destroyData == null){
                    destroyData = rootDestroyData;
                } else {
                    destroyData = destroyData.merge(rootDestroyData);
                }
            }
            if (destroyData == null){
                fallWithTree(level.getBlockState(rootPos), level, rootPos);
            } else {

                final ItemStack heldItem = player == null ? ItemStack.EMPTY : player.getMainHandItem();
                final int fortune = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
                final float fortuneFactor = 1.0f + 0.25f * fortune;
                final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
                woodVolume.multiplyVolume(fortuneFactor);
                final List<ItemStack> woodItems = destroyData.species.getBranchesDrops(level, woodVolume, heldItem);

                FallingTreeEntity.dropTree(level, destroyData, woodItems, FallingTreeEntity.DestroyType.HARVEST);

                if (player != null)
                    ItemUtils.damageAxe(player, heldItem, getRadius(level.getBlockState(rootPos)), woodVolume, true);
            }
        }

    }


}
