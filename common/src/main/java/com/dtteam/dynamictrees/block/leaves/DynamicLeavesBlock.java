package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.Cell;
import com.dtteam.dynamictrees.api.cell.CellNull;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.Ageable;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.tags.DTEntityTypeTags;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.ChunkTreeHelper;
import com.dtteam.dynamictrees.util.LevelContext;
import com.dtteam.dynamictrees.util.RayTraceCollision;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DynamicLeavesBlock extends LeavesBlock implements TreePart, Ageable, RayTraceCollision {

    public LeavesProperties properties = LeavesProperties.NULL;

    public DynamicLeavesBlock(final LeavesProperties leavesProperties, final Properties properties) {
        this(properties);
        this.setProperties(leavesProperties);
        leavesProperties.setDynamicLeavesState(defaultBlockState());
    }

    public DynamicLeavesBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, LeavesProperties.maxHydro).setValue(PERSISTENT, false).setValue(WATERLOGGED, false));
    }

    ///////////////////////////////////////////
    // PROPERTIES
    ///////////////////////////////////////////

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    public void setProperties(LeavesProperties properties) {
        this.properties = properties;
    }

    public LeavesProperties getLeavesProperties() {
        return properties;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos pos) {
        return getLeavesProperties().getFamily();
    }

    /** NeoForge override */
    @SuppressWarnings("unused")
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getLeavesProperties().getFlammability();
    }

    /** NeoForge override */
    @SuppressWarnings("unused")
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getLeavesProperties().getFireSpreadSpeed();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return getLeavesProperties().getPrimitiveLeaves().getDestroyProgress(player, level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return getLeavesProperties().getPrimitiveLeavesItemStack();
    }

    ///////////////////////////////////////////
    // GROWTH
    ///////////////////////////////////////////

    @Override
    public int age(LevelAccessor level, BlockPos pos, BlockState state, RandomSource rand, boolean worldgen) {
        return updateLeaves(level, pos, state, rand,worldgen);
    }

    /**
     * TODO: fix this up, the growth multiplier, it's kinda pointless
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        double growthMultiplier = Services.CONFIG.getDoubleConfig(IConfigHelper.TREE_GROWTH_MULTIPLIER);
        if (rand.nextFloat() > growthMultiplier) {
            return;
        }

        if (removeIfInvalid(state, level, pos, rand)) {
            return;
        }

        //Every once in a blue moon, age the leaves
        if (rand.nextFloat() < 0.01){
            age(level, pos, state, rand, false);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        BlockState neighborState = level.getBlockState(neighborPos);
        boolean sideIsLeaves = neighborState.hasProperty(DISTANCE);
        int sideHydro = sideIsLeaves ? neighborState.getValue(DISTANCE) : 0;
        if (!sideIsLeaves || sideHydro < state.getValue(DISTANCE)){
            level.scheduleTick(pos, this, 1);
        }
    }

    /**
     * Ticks scheduled by neighborChanged
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        updateHydro(level, pos, state, false);
    }

    /**
     * Pulses recursively through the canopy using BFS, updating hydro and growing around if possible.
     * @return False if the leaves decayed. True if they survived.
     */
    public boolean updateAllLeaves(LevelAccessor level, BlockPos startPos, BlockState startState, RandomSource rand, boolean worldGen){
        //We store the position and hydro of the next block.
        Queue<Tuple<BlockPos, Integer>> toProcess = new ArrayDeque<>();
        Set<BlockPos> processedPositions = new HashSet<>();
        int firstHydro = updateHydro(level, startPos, startState, worldGen);
        toProcess.add(new Tuple<>(startPos, firstHydro));
        if (firstHydro == 0) return false;
        while (!toProcess.isEmpty() && processedPositions.size() <= getLeavesProperties().maxLeavesRecursion()){
            Tuple<BlockPos, Integer> tup = toProcess.remove();
            BlockPos pos = tup.getA();
            int hydro = tup.getB();
            processedPositions.add(pos);
            for (Direction dir : Direction.values()) { // Go on all 6 sides of this block
                if (hydro > 1 || rand.nextInt(4) == 0) { // we'll give it a 1 in 4 chance to grow leaves if hydro is low to help performance
                    BlockPos sidePos = pos.relative(dir);
                    if (processedPositions.contains(sidePos)) continue;
                    BlockState sideState = level.getBlockState(sidePos);
                    //Check for surrounding leaves. Grow them if there aren't any.
                    boolean hasLeaves = TreeHelper.isLeaves(sideState);
                    int sideHydro;
                    if (hasLeaves) {
                        sideHydro = updateHydro(level, sidePos, sideState, worldGen);
                    } else { //There were no leaves, attempt to grow some
                        sideHydro = growLeavesIfLocationIsSuitable(level, getLeavesProperties(), sidePos, null);
                    }
                    //Do not iterate back through bigger hydro values
                    //or if the leaves failed to grow
                    if (sideHydro == 0 || sideHydro <= hydro){
                        toProcess.add(new Tuple<>(sidePos, sideHydro));
                    }
                }
            }
        }
        return true;
    }

    public int updateLeaves(LevelAccessor level, BlockPos pos, BlockState state, RandomSource rand, boolean worldGen){
        int newHydro = updateHydro(level, pos, state, worldGen);
        if (newHydro == 0) return 0; //If the leaves died don't bother

        if (!worldGen && removeIfInvalid(state, level, pos, rand)) {
            return 0;
        }

        for (Direction dir : Direction.values()) {
            if (newHydro > 1 || rand.nextInt(4) == 0) {
                BlockPos sidePos = pos.relative(dir);
                growLeavesIfLocationIsSuitable(level, getLeavesProperties(), sidePos, null);
            }
        }
        return newHydro;
    }

    //RANDOM TICK -> Destroy leaves if invalid
    //NEIGHBOR UPDATE -> recalculate hydro
    //TREE PULSE -> recalculate hydro
    //              grows new leaves around (BFS)
    public int updateHydro(LevelAccessor accessor, BlockPos pos, BlockState state, boolean worldGen){
        final LeavesProperties leavesProperties = getLeavesProperties();
        final int oldHydro = state.getValue(DISTANCE);

        if (!ChunkTreeHelper.canCheckSurroundings(accessor, pos, 2)) return oldHydro;

        // Check hydration level. Dry leaves (0) are dead leaves.
        final int newHydro = getHydrationLevelFromNeighbors(accessor, pos, leavesProperties);

        if (oldHydro != newHydro) { // Only update if the hydro has changed. A little performance gain.
            BlockState placeState = getLeavesBlockStateForPlacement(accessor, pos, leavesProperties.getDynamicLeavesState(newHydro), oldHydro, worldGen);
            boolean decayed = newHydro == 0;
//           if (decayed && !worldGen && (accessor instanceof Level level)
//                    //if the old hydro is the default then its most likely a block that was just placed and failed
//                    && oldHydro != getLeavesProperties().getCellKit().getDefaultHydration()) {
//                dropResources(state, level, pos);
//            }
            int flag = decayed ? 3 : (appearanceChangesWithHydro(oldHydro, newHydro) ? 2 : 4);
            accessor.setBlock(pos, placeState, flag);
            /* We do not use the 0x02 flag(update client) for performance reasons. The clients do not need to know the hydration level of the leaves blocks as it
             does not affect appearance or behavior, unless appearanceChangesWithHydro. For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
             however if the new hydro is 0, it means the leaves were removed, and we do need to update, so the flag is 3. */
        }
        return newHydro;
    }

    public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
        return false;
    }

    /**
     * Provides a method to add custom leaves properties besides the normal hydro.
     *
     * @param level                The level
     * @param pos                  Position of the new leaves blck
     * @param leavesStateWithHydro The state of the leaves with the hydro applied
     * @param oldHydro             the hydro value of the leaves before the palcement. Will be 0 if its a new leaf
     * @param worldGen             true if this is happening during worldgen
     * @return A provider for adding more blockstate properties
     */
    public BlockState getLeavesBlockStateForPlacement(LevelAccessor level, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
        return leavesStateWithHydro; //by default just pass the blockstate along
    }

    /**
     * Checks to see if the location at pos is suitable for new leaves and if so set new leaves at pos with hydro value
     *
     * @param level      The level
     * @param leavesProp Properties of the leaves we are working with
     * @param pos        The position of interest
     * @param hydro      The hydration value for the resulting cell. If it's null it will be calculated from neighbors
     * @return           The hydro of the placed block. It will be 0 if the leaves were not placed, even if leaves were already there
     */
    public int growLeavesIfLocationIsSuitable(LevelAccessor level, LeavesProperties leavesProp, BlockPos pos, @Nullable Integer hydro) {
        if (isLocationSuitableForNewLeaves(level, leavesProp, pos)) {
            if (hydro == null) hydro = getHydrationLevelFromNeighbors(level, pos, leavesProp);
            else hydro = hydro == 0 ? leavesProp.getCellKit().getDefaultHydration() : hydro;
            level.setBlock(pos, getLeavesBlockStateForPlacement(level, pos, leavesProp.getDynamicLeavesState(hydro), 0, false), 2); // Removed Notify Neighbors Flag for performance.
            return hydro;
        }
        return 0;
    }

    /**
     * Checks the {@link Block} at the given {@link BlockPos} is suitable for growing new leaves.
     *
     * @param level            The {@link Level} instance.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @param pos              The {@link BlockPos} for the new leaves.
     * @return {@code true} if the {@link BlockPos} is suitable for new leaves; {@code false} otherwise.
     */
    public boolean isLocationSuitableForNewLeaves(LevelAccessor level, LeavesProperties leavesProperties, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();

        if (block instanceof DynamicLeavesBlock) {
            return false; //leaves can't grow on leaves duh.
        }

        if (!blockState.getFluidState().isEmpty() && !properties.waterResistant) {
            return false; //leaves drown inside water
        }

        final BlockState belowBlockState = level.getBlockState(pos.below());

        // Prevent leaves from growing on the ground or above liquids.
        if (!leavesProperties.canGrowOnGround() && ((belowBlockState.canOcclude() && !TreeHelper.isBranch(belowBlockState) && !(belowBlockState.getBlock() instanceof LeavesBlock)) || belowBlockState.getBlock() instanceof LiquidBlock)) {
            return false;
        }

        // Help to grow into double tall grass and ferns in a more natural way.
        final BlockState stateDown = level.getBlockState(pos.below());
        if (block instanceof DoublePlantBlock && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER &&
                stateDown.getBlock() instanceof DoublePlantBlock && stateDown.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            if (block == Blocks.TALL_GRASS) {
                level.setBlock(pos.below(), Blocks.SHORT_GRASS.defaultBlockState(), 3);
            } else if (block == Blocks.LARGE_FERN) {
                level.setBlock(pos.below(), Blocks.FERN.defaultBlockState(), 3);
            }
            level.removeBlock(pos, false);
        }

        return (level.isEmptyBlock(pos) || level.getBlockState(pos).canBeReplaced()) && hasAdequateLight(blockState, level, leavesProperties, pos);
    }

    /**
     * Checks that the {@link DynamicLeavesBlock} at the given {@link BlockPos} has enough light to exist.
     *
     * @param state            The {@link BlockState} of the {@link DynamicLeavesBlock} to check.
     * @param level            The {@link LevelAccessor} instance.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @param pos              The {@link BlockPos} of the {@link DynamicLeavesBlock}.
     * @return {@code true} if the {@link Block} has adequate light; {@code false otherwise}.
     */
    public boolean hasAdequateLight(BlockState state, LevelAccessor level, LeavesProperties leavesProperties, BlockPos pos) {

        // If clear sky is above the block then we needn't go any further.
        if (level.canSeeSkyFromBelowWater(pos)) {
            return true;
        }

        final int smother = leavesProperties.getSmotherLeavesMax();

        // Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
        if (smother != 0) {
            if (isBottom(level, pos)) { // Only act on the bottom block of the Growable stack
                // Prevent leaves from growing where they would be "smothered" from too much above foliage

                int smotherLeaves = 0;

                for (int i = 0; i < smother; i++) {
                    smotherLeaves += TreeHelper.isTreePart(level, pos.above(i + 1)) ? 1 : 0;
                }

                if (smotherLeaves >= smother) {
                    return false;
                }
            }
        }

		/* Ensure the leaves don't grow in dark locations. This creates a realistic canopy effect in forests and other nice stuff.
		    If there's already leaves here then don't kill them if it's a little dark.
		    If it's empty space then don't create leaves unless it's sufficiently bright.
		    The range allows for adaptation to the hysteric effect that could cause blocks to rapidly appear and disappear. */
        return level.getBrightness(LightLayer.SKY, pos) >= (TreeHelper.isLeaves(state) ? leavesProperties.getLightRequirement() - 2 : leavesProperties.getLightRequirement());
    }

    public boolean removeIfInvalid(BlockState state, LevelAccessor level, BlockPos pos, RandomSource rand){
        if (getLeavesProperties().updateTick(level, pos, state, rand)) {
            //waterlogged leaves drown
            if (state.getValue(WATERLOGGED) && !properties.waterResistant) {
                level.setBlock(pos, getFluidState(state).createLegacyBlock(), 3);
                return true;
            }
            //no light... no leaves.
            if (!hasAdequateLight(state, level, getLeavesProperties(), pos)) {
                level.removeBlock(pos, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the {@link DynamicLeavesBlock} at the given {@link BlockPos} is at the bottom of the stack.
     *
     * @param level The {@link Level} instance.
     * @param pos   The {@link BlockPos} for the leaves {@link Block} to check.
     * @return {@code true} if the {@link Block} is at the bottom of the stack; {@code false} otherwise.
     */
    public static boolean isBottom(LevelAccessor level, BlockPos pos) {
        final BlockState belowBlockState = level.getBlockState(pos.below());
        final TreePart belowTreepart = TreeHelper.getTreePart(belowBlockState);

        if (belowTreepart != TreeHelper.NULL_TREE_PART) {
            return belowTreepart.getRadius(belowBlockState) > 1; // False for leaves, twigs, and dirt. True for stocky branches.
        }

        return true; // Non-Tree parts below indicate the bottom of stack.
    }

    /**
     * Gathers hydration levels from neighbors before pushing the values into the solver.
     *
     * @param level            The {@link LevelAccessor} instance.
     * @param pos              The {@link BlockPos} to get neighbors for.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @return The hydration from the solved cells.
     */
    public int getHydrationLevelFromNeighbors(LevelAccessor level, BlockPos pos, LeavesProperties leavesProperties) {
        final Cell[] cells = new Cell[6];

        for (Direction dir : Direction.values()) {
            final BlockPos deltaPos = pos.relative(dir);
            final BlockState state = level.getBlockState(deltaPos);
            final TreePart part = TreeHelper.getTreePart(state);

            cells[dir.ordinal()] = part.getHydrationCell(level, deltaPos, state, dir, leavesProperties);
        }

        return leavesProperties.getCellKit().getCellSolver().solve(cells); // Find center cell's value from neighbors.
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties otherProperties) {
        LeavesProperties thisProperties = getLeavesProperties();
        return thisProperties.isCompatibleLeaves(otherProperties) ? thisProperties.getCellKit().getCellForLeaves(state.getValue(LeavesBlock.DISTANCE)) : CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        if (signal.step()) // This is always placed at the beginning of every growSignal function.
        {
            this.branchOut(level, pos, signal); // When a growth signal hits a leaf block it attempts to become a tree branch.}
        }
        return signal;
    }

    /**
     * Will place a leaves block if the position is air and it's possible to create one there. Otherwise it will check
     * to see if the block is already there.
     *
     * @param level            The {@link Level} instance.
     * @param pos              The {@link BlockPos} to check.
     * @param leavesProperties The {@link LeavesProperties} required.
     * @return {@code true} if the leaves are at the given {@link BlockPos}; {@code false} otherwise.
     */
    public boolean needLeaves(Level level, BlockPos pos, LeavesProperties leavesProperties, Species species) {
        if (level.isEmptyBlock(pos)) { // Place leaves if air.
            return 0 != this.growLeavesIfLocationIsSuitable(level, leavesProperties, pos, leavesProperties.getCellKit().getDefaultHydration());
        } else { // Otherwise check if there's already this type of leaves there.
            BlockState state = level.getBlockState(pos);
            final TreePart treePart = TreeHelper.getTreePart(state);
            return treePart instanceof DynamicLeavesBlock && species.isValidLeafBlock((DynamicLeavesBlock) treePart);
        }
    }

    public GrowSignal branchOut(Level level, BlockPos pos, GrowSignal signal) {

        Species species = signal.getSpecies();
        LeavesProperties leavesProperties = species.getLeavesProperties();

        //Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
        if (!needLeaves(level, pos, leavesProperties, species)) {
            signal.success = false;
            return signal;
        }

        //Check to see if there's neighboring branches and abort if there's any found.
        if (BranchBlock.isNextToBranch(level, pos, signal.dir.getOpposite())) {
            signal.success = false;
            return signal;
        }

        boolean hasLeaves = false;
        //Check leaves conditions before expanding the canopy, otherwise it will always be true
        for (Direction dir : Direction.values()) {
            if (needLeaves(level, pos.relative(dir), leavesProperties, species)) {
                hasLeaves = true;
                break;
            }
        }

        //Pulse through the leaves to update the canopy shape and their hydro values
        boolean survived = updateAllLeaves(level, pos, level.getBlockState(pos), signal.rand, false);
        //if hydro was 0 then the leaves have been removed
//        if (!survived){
//            signal.success = false;
//            return signal;
//        }

        if (hasLeaves) {
            //Finally set the leaves block to a branch
            Family family = species.getFamily();
            family.getBranchForPlacement(level, species, pos).ifPresent(branch ->
                    branch.setRadius(level, pos, family.getPrimaryThickness(), null)
            );
            signal.radius = family.getSecondaryThickness();//For the benefit of the parent branch

        }

        signal.success = hasLeaves;

        return signal;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return from.getFamily().isCompatibleDynamicLeaves(from.getFamily().getCommonSpecies(), state, level, pos) ? 2 : 0;
    }

    ///////////////////////////////////////////
    // ENTITY INTERACTIONS
    ///////////////////////////////////////////

    /**
     * We will disable landing effects because we crush the blocks on landing and create our own particles in crushBlock()
     * NeoForge Override */
    @SuppressWarnings("unused")
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (this.isEntityPassable(pContext)) {
            return Shapes.empty();
        }
        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isLeavesPassable() || this.isEntityPassable(context)) {
            return Shapes.empty();
        } else if (isMovementVanilla()) {
            return Shapes.block();
        } else {
            return Shapes.create(new AABB(0.125, 0, 0.125, 0.875, 0.50, 0.875));
        }
    }

    /**
     * This support shape allows for placement on the sides (vines) but not on top
     */
    protected static final VoxelShape SUPPORT_SHAPE = Shapes.join(Shapes.block(), box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);
    @Override
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return SUPPORT_SHAPE;
    }

    protected boolean isMovementVanilla(){
        return Services.CONFIG.isServerConfigLoaded() && Services.CONFIG.getBoolConfig(IConfigHelper.VANILLA_LEAVES_COLLISION);
    }

    protected boolean isLeavesPassable() {
        return (Services.CONFIG.isServerConfigLoaded() && Services.CONFIG.getBoolConfig(IConfigHelper.IS_LEAVES_PASSABLE))
                || Services.PLATFORM.isModLoaded(DynamicTrees.PASSABLE_FOLIAGE);
    }

    public boolean isEntityPassable(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityCollisionContext) {
            return isEntityPassable(entityCollisionContext.getEntity());
        }
        return false;
    }

    public boolean isEntityPassable(@Nullable Entity entity) {
        if (entity instanceof Projectile) //Projectiles such as arrows fly through leaves
            return true;
        if (entity instanceof ItemEntity itemEntity) //Seed items fall through leaves
            return itemEntity.getItem().getItem() instanceof Seed;

        //Bees fly through leaves, otherwise they get stuck :(
        return entity != null && entity.getType().is(DTEntityTypeTags.CAN_PASS_THROUGH_LEAVES);
    }

    /**
     * Only here so that subclasses can override {@link #fallOn(Level, BlockState, BlockPos, Entity, float)}
     * and return it to the default behavior in {@link Block#fallOn(Level, BlockState, BlockPos, Entity, float)}.
     */
    protected void superFallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, blockState, pos, entity, fallDistance);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // We are only interested in Living things crashing through the canopy.
        if (!Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_CANOPY_CRASH) || !(entity instanceof LivingEntity)) {
            return;
        }

        entity.fallDistance--;

        final AABB aabb = entity.getBoundingBox();

        final int minX = Mth.floor(aabb.minX + 0.001D);
        final int minZ = Mth.floor(aabb.minZ + 0.001D);
        final int maxX = Mth.floor(aabb.maxX - 0.001D);
        final int maxZ = Mth.floor(aabb.maxZ - 0.001D);

        boolean crushing = true;
        boolean hasLeaves = true;

        final SoundType stepSound = this.getSoundType(level.getBlockState(pos));
        final float volume = Mth.clamp(stepSound.getVolume() / 16.0f * fallDistance, 0, 3.0f);
        level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), stepSound.getBreakSound(), SoundSource.BLOCKS, volume, stepSound.getPitch(), false);

        for (int iy = 0; (entity.fallDistance > 3.0f) && crushing && ((pos.getY() - iy) >= level.getMinBuildHeight()); iy++) {
            if (hasLeaves) { // This layer has leaves that can help break our fall
                entity.fallDistance *= 0.66f; // For each layer we are crushing break the momentum
                hasLeaves = false;
            }

            for (int ix = minX; ix <= maxX; ix++) {
                for (int iz = minZ; iz <= maxZ; iz++) {
                    BlockPos iPos = new BlockPos(ix, pos.getY() - iy, iz);
                    BlockState crashState = level.getBlockState(iPos);
                    if (TreeHelper.isLeaves(crashState)) {
                        hasLeaves = true; // This layer has leaves
//                        DTClient.crushLeavesBlock(level, iPos, crashState, entity);
                        level.removeBlock(iPos, false);
                    } else if (!level.isEmptyBlock(iPos)) {
                        crushing = false; // We hit something solid thus no longer crushing leaves layers
                    }
                }
            }
        }
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isMovementVanilla() || isEntityPassable(entity)) {
            super.entityInside(state, level, pos, entity);
        } else {
            if (entity.getDeltaMovement().y < 0.0D && (isLeavesPassable() || entity.fallDistance < 2.0f)) {
                entity.fallDistance = 0.0f;
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y * 0.5D, entity.getDeltaMovement().z); // Slowly sink into the block
            } else if (!isLeavesPassable() && entity.getDeltaMovement().y > 0 && entity.getDeltaMovement().y < 0.25D) {
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y + 0.025, entity.getDeltaMovement().z); // Allow a little climbing
            }

            entity.setSprinting(false); // One cannot sprint upon tree tops
            entity.setDeltaMovement(entity.getDeltaMovement().x * 0.25D, entity.getDeltaMovement().y, entity.getDeltaMovement().z * 0.25D); // Make travel slow and laborious
        }
    }

    //////////////////////////////
    // DROPS
    //////////////////////////////

//    @Override
//    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
//        return true; // We'll handle this in #getDrops as some leave drops may not require a tool.
//    }
//
//    @Override
//    public boolean isShearable(@Nonnull ItemStack item, Level level, BlockPos pos) {
//        return this.getProperties().doRequireShears();
//    }

//    @Override
//    public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
//        return this.getDrops(player, item, level, pos, fortune);
//    }

    /**
     * Gets the drops for this {@link DynamicLeavesBlock}.
     *
     * @param state The current block state of the leaves block.
     * @param builder The loot param builder.
     * @return The {@link List} of {@link ItemStack}s to drop.
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        final Vec3 originPos = builder.getOptionalParameter(LootContextParams.ORIGIN);
        final LootTable lootTable;
        Species species = Species.NULL_SPECIES;
        BlockPos pos = BlockPos.ZERO;
        ServerLevel level = builder.getLevel();

        if (originPos == null) {
            lootTable = level.getServer().reloadableRegistries().getLootTable(getLootTable());
        } else {
            pos = BlockPos.containing(originPos.x, originPos.y, originPos.z);
            LeavesProperties leavesProperties = getLeavesProperties();
            species = getExactSpecies(level, pos, leavesProperties);
            lootTable = leavesProperties.getBlockLootTable(level.getServer().reloadableRegistries(), species);
        }

        if (lootTable == LootTable.EMPTY) {
            return Collections.emptyList();
        } else {
            LootParams context = builder
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(DTLootContextParams.SPECIES, species)
                    .withParameter(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR,
                            species.seasonalSeedDropFactor(LevelContext.create(level), pos))
                    .create(LootContextParamSets.BLOCK);
            return lootTable.getRandomItems(context);
        }
    }

    /**
     * Gets the exact {@link Species} for these leaves (if able to find branches nearby). Warning! Resource intensive
     * algorithm. Use only for interactions like breaking blocks.
     *
     * @param level            The {@link Level} instance.
     * @param pos              The {@link BlockPos} of the {@link DynamicLeavesBlock}.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @return The {@link Species} for the given leaves; otherwise {@link Species#NULL_SPECIES} if branches could not be
     * found nearby.
     */
    Species getExactSpecies(@Nullable final Level level, final BlockPos pos, final LeavesProperties leavesProperties) {
        if (level == null) {
            return Species.NULL_SPECIES;
        }

        final List<BlockPos> branchList = new ArrayList<>();

        // Find all of the branches that are nearby
        for (BlockPos dPos : leavesProperties.getCellKit().getLeafCluster().getAllNonZero()) {
            dPos = pos.offset(BlockPos.ZERO.subtract(dPos));//Becomes immutable at this point
            final BlockState state = level.getBlockState(dPos);

            if (!TreeHelper.isBranch(state)) {
                continue;
            }

            final BranchBlock branch = TreeHelper.getBranch(state);

            if (branch.getFamily() == leavesProperties.getFamily() && branch.getRadius(state) == branch.getFamily().getPrimaryThickness()) {
                branchList.add(dPos);
            }
        }

        if (branchList.isEmpty()) {
            return Species.NULL_SPECIES;
        }

        // Find the closest one
        BlockPos closest = branchList.getFirst();
        double minDist = 999;

        for (BlockPos dPos : branchList) {
            final double d = pos.distSqr(dPos);

            if (d < minDist) {
                minDist = d;
                closest = dPos;
            }
        }

        return TreeHelper.getExactSpecies(level, closest);
    }

    //////////////////////////////
    // RENDERING FUNCTIONS
    //////////////////////////////

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return getLeavesProperties().getRadiusForConnection(state, level, pos, from, side, fromRadius);
    }

    @Override
    public int getRadius(BlockState state) {
        return 0;
    }

    /**
     * Generally Leaves blocks should not be analyzed
     */
    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir, MapSignal signal) {
        return signal; // Shouldn't need to run analysis on leaf blocks.
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        // Leaves are only support for "twigs".
        return radius == branch.getFamily().getPrimaryThickness() && branch.getFamily() == getFamily(state, level, pos) ? BranchBlock.setSupport(0, 1) : 0;
    }

    @Override
    public final TreePartType getTreePartType() {
        return TreePartType.LEAVES;
    }

    @Override
    public boolean isRayTraceCollidable() {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0.2F;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (properties.hasTickParticles && properties.getPrimitiveLeavesBlock().isPresent()) {
            properties.getPrimitiveLeavesBlock().ifPresent((b)->b.animateTick(state,level,pos,random));
        } else {
            super.animateTick(state, level, pos, random);
        }
    }

}