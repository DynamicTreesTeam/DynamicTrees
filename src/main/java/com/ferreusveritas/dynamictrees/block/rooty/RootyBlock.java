package com.ferreusveritas.dynamictrees.block.rooty;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockDecayer;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A version of Rooty Dirt block that holds on to a species with a TileEntity.
 * <p>
 * When to use this: You can't determine a species of a tree family by location alone (e.g. Swamp Oak by biome) The
 * species is rare and you don't want to commit all the resources necessary to make a whole tree family(e.g. Apple Oak)
 * <p>
 * This is a great method for creating numerous fruit species(Pam's Harvestcraft) under one {@link Family} family.
 *
 * @author ferreusveritas
 */
@SuppressWarnings("deprecation")
public class RootyBlock extends BlockWithDynamicHardness implements TreePart, EntityBlock {

    public static RootyBlockDecayer rootyBlockDecayer = null;

    public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 0, 15);
    public static final BooleanProperty IS_VARIANT = BooleanProperty.create("is_variant");

    private final SoilProperties properties;

    public RootyBlock(SoilProperties properties, Properties blockProperties) {
        super(blockProperties.randomTicks());
        this.properties = properties;
        registerDefaultState(defaultBlockState().setValue(FERTILITY, 0).setValue(IS_VARIANT, false));
    }

    ///////////////////////////////////////////
    // SOIL PROPERTIES
    ///////////////////////////////////////////

    public SoilProperties getSoilProperties() {
        return properties;
    }

    public Block getPrimitiveSoilBlock() {
        return properties.getPrimitiveSoilBlock();
    }
    public BlockState getPrimitiveSoilState(BlockState currentSoilState) {
        return properties.getPrimitiveSoilState(currentSoilState);
    }

    ///////////////////////////////////////////
    // BLOCK PROPERTIES
    ///////////////////////////////////////////

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getPrimitiveSoilBlock().getSoundType(getDecayBlockState(state, level, pos), level, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return getPrimitiveSoilBlock().getLightEmission(getDecayBlockState(state, level, pos), level, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return getPrimitiveSoilBlock().propagatesSkylightDown(getDecayBlockState(state, level, pos), level, pos);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return getPrimitiveSoilBlock().getLightBlock(getDecayBlockState(state, level, pos), level, pos);
    }

    @Override
    public MaterialColor defaultMaterialColor() {
        return getPrimitiveSoilBlock().defaultMaterialColor();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPrimitiveSoilBlock().getShape(getDecayBlockState(state, level, pos), level, pos, context);
    }

    @Override
    public float getFriction() {
        return getPrimitiveSoilBlock().getFriction();
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getPrimitiveSoilBlock().getExplosionResistance(getDecayBlockState(state, level, pos), level, pos, explosion);
    }

    @Override
    public float getSpeedFactor() {
        return getPrimitiveSoilBlock().getSpeedFactor();
    }

    @Override
    public float getJumpFactor() {
        return getPrimitiveSoilBlock().getJumpFactor();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getPrimitiveSoilBlock().getFireSpreadSpeed(getDecayBlockState(state, level, pos), level, pos, face);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return getPrimitiveSoilBlock().isFireSource(getDecayBlockState(state, level, pos), level, pos, side);
    }

    @Nonnull
    @Override
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        return getPrimitiveSoilState(state).getDrops(builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getPrimitiveSoilBlock().getCloneItemStack(getDecayBlockState(state, level, pos), target, level, pos, player);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        return (float) (getDecayBlockState(state, level, pos).getDestroySpeed(level, pos) * DTConfigs.ROOTY_BLOCK_HARDNESS_MULTIPLIER.get());
    }

    ///////////////////////////////////////////
    // BLOCKSTATES
    ///////////////////////////////////////////

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FERTILITY).add(IS_VARIANT);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////


    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
//        if (pState.getValue(IS_VARIANT)) {
            return new SpeciesBlockEntity(pPos,pState);
//        }
//        return null;
    }
//
//    @Override
//    public boolean hasTileEntity(BlockState state) {
//        return state.getValue(IS_VARIANT);
//    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (random.nextInt(DTConfigs.TREE_GROWTH_FOLDING.get()) == 0) {
            updateTree(state, level, pos, random, true);
        }
    }

    public Direction getTrunkDirection(BlockGetter access, BlockPos rootPos) {
        return Direction.UP;
    }

    public void updateTree(BlockState rootyState, Level level, BlockPos rootPos, Random random, boolean natural) {

        if (CoordUtils.isSurroundedByLoadedChunks(level, rootPos)) {

            boolean viable = false;

            Species species = getSpecies(rootyState, level, rootPos);

            if (species.isValid()) {
                BlockPos treePos = rootPos.relative(getTrunkDirection(level, rootPos));
                TreePart treeBase = TreeHelper.getTreePart(level.getBlockState(treePos));
                if (treeBase != TreeHelper.NULL_TREE_PART) {
                    viable = species.update(level, this, rootPos, getFertility(rootyState, level, rootPos), treeBase, treePos, random, natural);
                }
            }

            if (!viable) {
                //TODO: Attempt to destroy what's left of the tree before setting rooty to dirt
                level.setBlock(rootPos, getDecayBlockState(rootyState, level, rootPos), 3);
            }

        }

    }

    /**
     * This is the state the rooty dirt returns to once it no longer supports a tree structure.
     *
     * @param pos    The position of the {@link RootyBlock}
     */
    public BlockState getDecayBlockState(BlockState state, BlockGetter level, BlockPos pos) {
        return getPrimitiveSoilState(state);
    }

    /**
     * Forces the {@link RootyBlock} to decay if it's there, turning it back to its primitive soil block. Custom decay
     * logic is also supported, see {@link RootyBlockDecayer} for details.
     *
     * @param level      The {@link Level} instance.
     * @param rootPos    The {@link BlockPos} of the {@link RootyBlock}.
     * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
     * @param species    The {@link Species} of the tree that was removed.
     */
    public void doDecay(Level level, BlockPos rootPos, BlockState rootyState, Species species) {
        if (level.isClientSide || !TreeHelper.isRooty(rootyState)) {
            return;
        }

        this.updateTree(rootyState, level, rootPos, level.random, true); // This will turn the rooty dirt back to it's default soil block.
        final BlockState newState = level.getBlockState(rootPos);

        // Make sure we're not still a rooty block and return if custom decay returns true.
        if (TreeHelper.isRooty(newState) || (rootyBlockDecayer != null && rootyBlockDecayer.decay(level, rootPos, rootyState, species))) {
            return;
        }

        final BlockState primitiveDirt = this.getDecayBlockState(rootyState, level, rootPos);

        level.setBlock(rootPos, primitiveDirt, Block.UPDATE_ALL);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return getFertility(blockState, level, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        final ItemStack heldItem = player.getItemInHand(hand);
        return getFamily(state, level, pos).onTreeActivated(
                new Family.TreeActivationContext(
                        level, TreeHelper.findRootNode(level, pos), pos, state, player, hand, heldItem, hitResult
                )
        ) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public void destroyTree(Level level, BlockPos rootPos) {
        Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.above()));

        if (branch.isPresent()) {
            BranchDestructionData destroyData = branch.get().destroyBranchFromNode(level, rootPos.above(), Direction.DOWN, true, null);
            FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
        }
    }

    @Override
    public void playerWillDestroy(Level level, @Nonnull BlockPos pos, BlockState state, @Nonnull Player player) {
        this.destroyTree(level, pos);
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        destroyTree(level, pos);
        super.onBlockExploded(state, level, pos, explosion);
    }


    @Nonnull
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    ///////////////////////////////////////////
    // TREE STUFF
    ///////////////////////////////////////////

    public int getFertility(BlockState blockState, BlockGetter blockAccess, BlockPos pos) {
        return blockState.getValue(FERTILITY);
    }

    public void setFertility(Level level, BlockPos rootPos, int fertility) {
        final BlockState currentState = level.getBlockState(rootPos);
        final Species species = this.getSpecies(currentState, level, rootPos);

        level.setBlock(rootPos, currentState.setValue(FERTILITY, Mth.clamp(fertility, 0, 15)), 3);
        level.updateNeighborsAt(rootPos, this); // Notify all neighbors of NSEWUD neighbors (for comparator).
        this.setSpecies(level, rootPos, species);
    }

    public boolean fertilize(Level level, BlockPos pos, int amount) {
        int fertility = this.getFertility(level.getBlockState(pos), level, pos);
        if ((fertility == 0 && amount < 0) || (fertility == 15 && amount > 0)) {
            return false;//Already maxed out
        }
        setFertility(level, pos, fertility + amount);
        return true;
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int getRadius(BlockState state) {
        return 8;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return 8;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return 0;
    }

    /**
     * Analysis typically begins with the root node.  This function allows the rootyBlock to direct the analysis in the
     * direction of the tree since trees are not always "up" from the rootyBlock
     */
    public MapSignal startAnalysis(LevelAccessor level, BlockPos rootPos, MapSignal signal) {
        Direction dir = getTrunkDirection(level, rootPos);
        BlockPos treePos = rootPos.relative(dir);
        BlockState treeState = level.getBlockState(treePos);

        TreeHelper.getTreePart(treeState).analyse(treeState, level, treePos, null, signal);

        return signal;
    }

    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        signal.run(state, level, pos, fromDir);//Run inspector of choice

        if (signal.root == null) {
            signal.root = pos;
        } else {
            signal.multiroot = true;
        }

        signal.foundRoot = true;

        return signal;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return dir == Direction.DOWN ? BranchBlock.setSupport(1, 1) : 0;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos rootPos) {
        BlockPos treePos = rootPos.relative(getTrunkDirection(level, rootPos));
        BlockState treeState = level.getBlockState(treePos);
        return TreeHelper.isBranch(treeState) ? TreeHelper.getBranch(treeState).getFamily(treeState, level, treePos) : Family.NULL_FAMILY;
    }

    @Nullable
    private SpeciesBlockEntity getTileEntitySpecies(LevelAccessor level, BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity == null)
            return null;
        return blockEntity instanceof SpeciesBlockEntity ? (SpeciesBlockEntity) blockEntity : null;
    }

    /**
     * Rooty Dirt can report whatever {@link Family} species it wants to be. We'll use a stored value to determine the
     * species for the {@link BlockEntity} version. Otherwise we'll just make it report whatever {@link DynamicTrees} the
     * above {@link BranchBlock} says it is.
     */
    public Species getSpecies(BlockState state, LevelAccessor level, BlockPos rootPos) {

        Family tree = getFamily(state, level, rootPos);

        SpeciesBlockEntity rootyDirtTE = getTileEntitySpecies(level, rootPos);

        if (rootyDirtTE != null) {
            Species species = rootyDirtTE.getSpecies();
            if (species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
                return rootyDirtTE.getSpecies();
            }
        }

        return tree.getSpeciesForLocation(level, rootPos.relative(getTrunkDirection(level, rootPos)));
    }

    public void setSpecies(Level level, BlockPos rootPos, Species species) {
        SpeciesBlockEntity rootyDirtTE = getTileEntitySpecies(level, rootPos);
        if (rootyDirtTE != null) {
            rootyDirtTE.setSpecies(species);
        }
    }

    public final TreePartType getTreePartType() {
        return TreePartType.ROOT;
    }

    @Override
    public final boolean isRootNode() {
        return true;
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    public int colorMultiplier(BlockColors blockColors, BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        final int white = 0xFFFFFFFF;
        switch (tintIndex) {
            case 0:
                return blockColors.getColor(getPrimitiveSoilState(state), level, pos, tintIndex);
            case 1:
                return state.getBlock() instanceof RootyBlock ? rootColor(state, level, pos) : white;
            default:
                return white;
        }
    }

    public boolean getColorFromBark() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public int rootColor(BlockState state, BlockGetter blockAccess, BlockPos pos) {
        return getFamily(state, blockAccess, pos).getRootColor(state, getColorFromBark());
    }

    public boolean fallWithTree(BlockState state, Level level, BlockPos pos) {
        return false;
    }

}
