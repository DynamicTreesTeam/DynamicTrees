package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.Cell;
import com.dtteam.dynamictrees.api.cell.CellNull;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.BlockWithDynamicHardness;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.tree.ChunkTreeHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class SoilBlock extends BlockWithDynamicHardness implements TreePart, EntityBlock, BonemealableBlock {

    public static SoilBlockDecayer soilBlockDecayer = null;

    public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 0, 15);
    public static final BooleanProperty IS_VARIANT = BooleanProperty.create("is_variant");

    private final SoilProperties properties;

    public SoilBlock(Identifier id, SoilProperties properties, Properties blockProperties) {
        super(blockProperties.randomTicks().pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)));
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
        return getSoilProperties().getPrimitiveSoilBlock();
    }

    public BlockState getPrimitiveSoilState(BlockState currentSoilState) {
        return getSoilProperties().getPrimitiveSoilState(currentSoilState);
    }

    ///////////////////////////////////////////
    // BLOCK PROPERTIES
    ///////////////////////////////////////////

    @Override
    public float getSpeedFactor() {
        return getPrimitiveSoilBlock().getSpeedFactor();
    }

    @Override
    public float getJumpFactor() {
        return getPrimitiveSoilBlock().getJumpFactor();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //shapes get cached before the primitive soil gets set, so we default to full block
        if (getPrimitiveSoilBlock() == Blocks.AIR) return super.getShape(state, level, pos, context);
        return getPrimitiveSoilBlock().defaultBlockState().getShape(level, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (getPrimitiveSoilBlock() == Blocks.AIR) return super.getCollisionShape(state, level, pos, context);
        return getPrimitiveSoilBlock().defaultBlockState().getCollisionShape(level, pos, context);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (getPrimitiveSoilBlock() == Blocks.AIR) return super.getVisualShape(state, level, pos, context);
        return getPrimitiveSoilBlock().defaultBlockState().getVisualShape(level, pos, context);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (getPrimitiveSoilBlock() == Blocks.AIR) return super.getBlockSupportShape(state, level, pos);
        return getPrimitiveSoilBlock().defaultBlockState().getBlockSupportShape(level, pos);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return getPrimitiveSoilBlock().defaultBlockState().getSoundType();
    }

//    @Override
//    protected boolean propagatesSkylightDown(BlockState state) {
//        return getPrimitiveSoilBlock().defaultBlockState().propagatesSkylightDown(state);
//    }

    @Override
    public float getFriction() {
        return getPrimitiveSoilBlock().getFriction();
    }

    @Override
    public float getExplosionResistance() {
        return getPrimitiveSoilBlock().getExplosionResistance();
    }

//    @Override
//    protected int getLightDampening(BlockState state) {
//        return getPrimitiveSoilBlock().defaultBlockState().getLightDampening(state);
//    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return getPrimitiveSoilState(state).getDrops(builder);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return getPrimitiveSoilState(state).getCloneItemStack(level, pos, includeData);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        return (float) (getPrimitiveSoilState(state).getDestroySpeed(level, pos) * DTConfigs.SERVER.rootyBlockHardnessMultiplier.get());
    }

    ///////////////////////////////////////////
    // BLOCKSTATES
    ///////////////////////////////////////////

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FERTILITY).add(IS_VARIANT);
    }

    public BlockState GetStateFromIndex(int index){
        return defaultBlockState();
    }

    public int getStateIndex(BlockState state){
        return 0;
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (pState.getValue(IS_VARIANT)) {
            return new SpeciesBlockEntity(pPos,pState);
        }
        return null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(IS_VARIANT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double growthMultiplier = DTConfigs.SERVER.treeGrowthMultiplier.get();
        //Growth multiplier lower than 1 causes only some ticks to grow
        if (random.nextFloat() > growthMultiplier) return;

        //Growth multiplier higher than 1 causes multiple growth per tick.
        int attempts = (int)Math.ceil(growthMultiplier);
        for (int i=0; i<attempts; i++){
            updateTree(state, level, pos, random, true);
        }
    }

    public Direction getTrunkDirection(BlockGetter access, BlockPos rootPos) {
        return Direction.UP;
    }

    public void updateTree(BlockState rootyState, Level level, BlockPos soilPos, RandomSource random, boolean natural) {
        if (ChunkTreeHelper.isSurroundedByLoadedChunks(level, soilPos)) {
            boolean viable = false;

            Species species = getSpecies(rootyState, level, soilPos);
            if (species.isValid()) {
                BlockPos treePos = soilPos.relative(getTrunkDirection(level, soilPos));
                TreePart treeBase = TreeHelper.getTreePart(level.getBlockState(treePos));
                if (treeBase != TreeHelper.NULL_TREE_PART) {
                    viable = species.update(level, this, soilPos, getFertility(rootyState, level, soilPos), treeBase, treePos, random, natural);
                }
            }

            if (!viable) {
                doDecay(level, soilPos, rootyState);
            }

        }

    }

    /**
     * This is the state the rooty dirt returns to once it no longer supports a tree structure.
     *
     * @param pos    The position of the {@link SoilBlock}
     */
    public BlockState getDecayBlockState(BlockState state, BlockGetter level, BlockPos pos) {
        return getPrimitiveSoilState(state);
    }

    /**
     * Forces the {@link SoilBlock} to decay if it's there, turning it back to its primitive soil block. Custom decay
     * logic is also supported, see {@link SoilBlockDecayer} for details.
     *
     * @param level      The {@link Level} instance.
     * @param rootPos    The {@link BlockPos} of the {@link SoilBlock}.
     * @param rootyState The {@link BlockState} of the {@link SoilBlock}.
     */
    public void doDecay(Level level, BlockPos rootPos, BlockState rootyState) {
        level.setBlock(rootPos, getDecayBlockState(rootyState, level, rootPos), Block.UPDATE_ALL);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return getFertility(state, level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return getFamily(state, level, pos).onTreeActivated(
                new Family.TreeActivationContext(
                        level, TreeHelper.findRootNode(level, pos), pos, state, player, hand, stack, hitResult
                )
        ) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public void destroyTree(Level level, BlockPos rootPos){
        destroyTree(level, rootPos, null);
    }
    public void destroyTree(Level level, BlockPos rootPos, @Nullable Player player) {
        destroyTree(level, rootPos, player, getTrunkDirection(level, rootPos)); //Tree
        destroyTree(level, rootPos, player, getTrunkDirection(level, rootPos).getOpposite()); //Roots
    }
    public void destroyTree(Level level, BlockPos rootPos, @Nullable Player player, Direction dir) {
        Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.offset(dir.getUnitVec3i())));

        if (branch.isPresent()) {
            BranchDestructionData destroyData = branch.get().destroyBranchFromNode(level, rootPos.offset(dir.getUnitVec3i()), dir.getOpposite(), true, player);
            FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
        }
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid){
        if (getSpecies(state, level, pos).soilDestroyAction(level, pos, state, player)){
            this.spawnDestroyParticles(level, player, pos, state);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return false;
        }
        return level.isClientSide() ? level.setBlock(pos, fluid.createLegacyBlock(), 11) : level.removeBlock(pos, false);
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
        destroyTree(level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        wasExploded(level, pos, explosion);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        this.destroyTree(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Usually does nothing as rooty blocks usually don't have radius.
     * Overriden by #AerialRootsSoilProperties
     * @param level
     * @param state
     * @param pos
     * @param flags
     */
    public int updateRadius(LevelAccessor level, BlockState state, BlockPos pos, int flags, boolean force) {
        return getRadius(state);
    }

    /**
     * The following 3 methods are overridden by {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
     * and they are not normally called. However, they are here for mod compatibility.
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        if (levelReader instanceof Level level)
            return getSpecies(blockState, level, blockPos).canBoneMealTree();
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState){
        return true;
    }
    @Override
    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState){
        Species species = getSpecies(pState, pLevel, pPos);
        if (species.isValid()){
            species.applySubstance(pLevel, pPos, pPos, null, null, new ItemStack(Items.BONE_MEAL));
        }

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

    //TODO: can be optimized
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        boolean shouldUpdate = false;
        if (orientation != null){
            for (Direction dir : orientation.getDirections()){
                BlockPos neighborPos = pos.offset(dir.getUnitVec3i());
                if (neighborPos.equals(pos.relative(getTrunkDirection(level, pos)))){
                    shouldUpdate = true;
                    break;
                }
            }
        }
        if (shouldUpdate){
            level.scheduleTick(pos, this, 1);
        }

        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }


    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) return; //The root has already been destroyed / removed.
        if (getMainTrunk(level, pos) == null){
            doDecay(level, pos, state);
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        Direction supportDir = branch instanceof BasicRootsBlock ? Direction.UP : Direction.DOWN;
        return (dir == supportDir) ? BranchBlock.setSupport(1, 1) : 0;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos rootPos) {
        BranchBlock branchBlock = getMainTrunk(level, rootPos);
        return branchBlock != null ? branchBlock.getFamily() : Family.NULL_FAMILY;
    }

    @Nullable
    protected BranchBlock getMainTrunk(BlockGetter level, BlockPos soilPos){
        BlockPos pos = soilPos.relative(getTrunkDirection(level, soilPos));
        BlockState trunkState = level.getBlockState(pos);
        return TreeHelper.getBranch(trunkState);
    }

    @Nullable
    protected SpeciesBlockEntity getTileEntitySpecies(LevelAccessor level, BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity == null)
            return null;
        return blockEntity instanceof SpeciesBlockEntity ? (SpeciesBlockEntity) blockEntity : null;
    }

    /**
     * Rooty Dirt can report whatever {@link Family} species it wants to be. We'll use a stored value to determine the
     * species for the {@link BlockEntity} version. Otherwise, we'll just make it report whatever {@link DynamicTrees} the
     * above {@link BranchBlock} says it is.
     */
    public Species getSpecies(BlockState state, LevelAccessor level, BlockPos soilPos) {
        Family family = getFamily(state, level, soilPos);
        if (!family.isValid()) return Species.NULL_SPECIES;

        SpeciesBlockEntity rootyDirtTE = getTileEntitySpecies(level, soilPos);

        if (rootyDirtTE != null) {
            Species species = rootyDirtTE.getSpecies();
            if (species.getFamily() == family) {//As a sanity check we should see if the tree and the stored species are a match
                return rootyDirtTE.getSpecies();
            }
        }

        return family.getSpeciesForLocation(level, soilPos.relative(getTrunkDirection(level, soilPos)));
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

    public boolean getColorFromBark() {
        return false;
    }

    public int rootColor(BlockState state, BlockGetter blockAccess, BlockPos pos) {
        return getFamily(state, blockAccess, pos).getRootColor(state, getColorFromBark());
    }

    public boolean fallWithTree(BlockState state, Level level, BlockPos pos, boolean hasRoots) {
        return false;
    }
}
