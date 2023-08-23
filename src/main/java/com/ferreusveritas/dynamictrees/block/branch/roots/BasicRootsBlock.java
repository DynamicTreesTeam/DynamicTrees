package com.ferreusveritas.dynamictrees.block.branch.roots;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.OffsetablePodBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.RootsDestroyerNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.SpeciesNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.StateNode;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.LootTableSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

public class BasicRootsBlock extends BranchBlock implements SimpleWaterloggedBlock {
    public static final String NAME_SUFFIX = "_roots";

    public static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
    public static final EnumProperty<Layer> LAYER = EnumProperty.create("layer", Layer.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public enum Layer implements StringRepresentable {
        EXPOSED (MangroveFamily::getPrimitiveRoots),
        FILLED (MangroveFamily::getPrimitiveFilledRoots),
        COVERED (MangroveFamily::getPrimitiveCoveredRoots);
        final Function<MangroveFamily, Optional<Block>> primitiveFunc;
        Layer(Function<MangroveFamily, Optional<Block>> primitiveFunc){
            this.primitiveFunc = primitiveFunc;
        }
        @Override public @NotNull String getSerializedName() {
            return toString().toLowerCase();
        }

        public Optional<Block> getPrimitive (MangroveFamily family){
            return primitiveFunc.apply(family);
        }
    }

    private int flammability = 5; // Mimic vanilla logs
    private int fireSpreadSpeed = 5; // Mimic vanilla logs

    public BasicRootsBlock(ResourceLocation name, BlockBehaviour.Properties properties) {
        super(name, properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(LAYER, Layer.EXPOSED));
        rootLootTableSupplier = new LootTableSupplier("trees/roots/", name);
    }

    public boolean isFullBlock (BlockState state){
        return state.getValue(LAYER) == Layer.COVERED;
    }
    public MangroveFamily getFamily() {
        return (MangroveFamily) super.getFamily();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        int radius = getRadius(level.getBlockState(pos));
        return (fireSpreadSpeed * radius) / 8;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return flammability;
    }

    public BasicRootsBlock setFlammability(int flammability) {
        this.flammability = flammability;
        return this;
    }

    public BasicRootsBlock setFireSpreadSpeed(int fireSpreadSpeed) {
        this.fireSpreadSpeed = fireSpreadSpeed;
        return this;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, LAYER, WATERLOGGED);
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return 0;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return getRadius(state);
    }

    @Override
    public int getRadius(BlockState state) {
        return state.getValue(RADIUS);
    }

    protected int getMaxSignalDepth() {
        return getFamily().getMaxSignalDepth();
    }
    @Override
    public MapSignal analyse(BlockState blockState, LevelAccessor level, BlockPos pos, @javax.annotation.Nullable Direction fromDir, MapSignal signal) {
        // Note: fromDir will be null in the origin node

        if (signal.overflow || (signal.trackVisited && signal.doTrackingVisited(pos))) {
            return signal;
        }

        if (signal.depth++ < getMaxSignalDepth()) {// Prevents going too deep into large networks, or worse, being caught in a network loop
            signal.run(blockState, level, pos, fromDir);// Run the inspectors of choice
            for (Direction dir : Direction.values()) {// Spread signal in various directions
                if (dir != fromDir) {// don't count where the signal originated from
                    BlockPos deltaPos = pos.relative(dir);

                    BlockState deltaState = level.getBlockState(deltaPos);
                    TreePart treePart = TreeHelper.getTreePart(deltaState);

                    if (treePart.shouldAnalyse(deltaState, level, deltaPos)) {
                        signal = treePart.analyse(deltaState, level, deltaPos, dir.getOpposite(), signal);

                        // This should only be true for the originating block when the root node is found
                        if (signal.foundRoot && signal.localRootDir == null && fromDir == null) {
                            signal.localRootDir = dir;
                        }
                    }
                }
            }
            signal.returnRun(blockState, level, pos, fromDir);
        } else {
            BlockState state = level.getBlockState(pos);
            if (signal.destroyLoopedNodes && state.getBlock() instanceof BranchBlock) {
                BranchBlock branch = (BranchBlock) state.getBlock();
                branch.breakDeliberate(level, pos, DynamicTrees.DestroyMode.OVERFLOW);// Destroy one of the offending nodes
            }
            signal.overflow = true;
        }
        signal.depth--;

        return signal;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public boolean checkForRot(LevelAccessor level, BlockPos pos, Species species, int fertility, int radius, RandomSource rand, float chance, boolean rapid) {
        return false;
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @javax.annotation.Nullable Direction originDir, int flags) {
        destroyMode = DynamicTrees.DestroyMode.SET_RADIUS;
        BlockState currentState = level.getBlockState(pos);
        boolean replacingWater = currentState.getFluidState() == Fluids.WATER.getSource(false);
        boolean replacingGround = getFamily().isReplaceableByRoots(currentState.getBlock());
        boolean setWaterlogged = replacingWater && !replacingGround;
        level.setBlock(pos, getStateForRadius(radius)
                        .setValue(LAYER, replacingGround?Layer.COVERED:Layer.EXPOSED)
                        .setValue(WATERLOGGED, setWaterlogged),
                flags);
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;
        return radius;
    }

    @Override
    public BlockState getStateForRadius(int radius) {
        return defaultBlockState().setValue(RADIUS, radius);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (isFullBlock(state) && getFamily().getPrimitiveCoveredRoots().isPresent()){
            return new ItemStack(getFamily().getPrimitiveCoveredRoots().get());
        }
        return new ItemStack(asItem());
    }

    //////////////////////////////
    // SOUNDS
    //////////////////////////////

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<Block> primitive = state.getValue(LAYER).getPrimitive(getFamily());
        if (primitive.isPresent()){
            return primitive.get().getSoundType(state, level, pos, entity);
        } else
            return super.getSoundType(state, level, pos, entity);
    }


    //////////////////////////////
    // WATER LOGGING
    //////////////////////////////

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
    public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        return !isFullBlock(pState)
                && !pState.getValue(BlockStateProperties.WATERLOGGED)
                && pFluid == Fluids.WATER;
    }

    @Override
    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (canPlaceLiquid(pLevel, pPos, pState, pFluidState.getType())) {
            if (!pLevel.isClientSide()) {
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, true).setValue(LAYER, Layer.EXPOSED), 3);
                pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            }

            return true;
        } else {
            return false;
        }
    }

    //////////////////////////////
    // DROPS
    //////////////////////////////

    private final LootTableSupplier rootLootTableSupplier;

    public ResourceLocation getLootTableName() {
        return rootLootTableSupplier.getName();
    }

    public LootTable getLootTable(LootTables lootTables, Species species) {
        return rootLootTableSupplier.get(lootTables, species);
    }

    //////////////////////////////
    // INTERACTION
    //////////////////////////////

    protected boolean canPlace(Player player, Level level, BlockPos clickedPos, BlockState pState) {
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return pState.canSurvive(level, clickedPos) && level.isUnobstructed(pState, clickedPos, collisioncontext);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isFullBlock(state)) {
            return InteractionResult.PASS;
        }
        ItemStack handStack = player.getItemInHand(hand);
        Block coverBlock = getFamily().getPrimitiveCoveredRoots().orElse(null);
        if (coverBlock != null && handStack.getItem() == coverBlock.asItem()){
            BlockState newState = state.setValue(LAYER, Layer.COVERED).setValue(WATERLOGGED, false);
            if (canPlace(player, level, pos, newState)){
                level.setBlock(pos, newState, 3);
                if (!player.isCreative()) handStack.shrink(1);
                level.playSound(null, pos, coverBlock.getSoundType(state, level, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1f, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {

        if (isFullBlock(state)){
            level.setBlock(pos, state.setValue(LAYER, Layer.FILLED), level.isClientSide ? 11 : 3);
            this.spawnDestroyParticles(level, player, pos, state);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            Block primitive = state.getValue(LAYER).getPrimitive(getFamily()).orElse(null);
            if (!player.isCreative() && primitive != null) dropResources(primitive.defaultBlockState(), level, pos);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void futureBreak(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        // Tries to get the face being pounded on.
        final double reachDistance = entity instanceof Player ? Objects.requireNonNull(entity.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue() : 5.0D;
        final BlockHitResult ragTraceResult = this.playerRayTrace(entity, reachDistance, 1.0F);
        final Direction toolDir = ragTraceResult != null ? (entity.isShiftKeyDown() ? ragTraceResult.getDirection().getOpposite() : ragTraceResult.getDirection()) : Direction.DOWN;

        // Play and render block break sound and particles (must be done before block is broken).
        level.levelEvent(null, 2001, cutPos, getId(state));

        // Do the actual destruction.
        final BranchDestructionData destroyData = this.destroyBranchFromNode(level, cutPos, toolDir, false, entity);

        // Get all the wood drops.
        final ItemStack heldItem = entity.getMainHandItem();
        final int fortune = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
        final float fortuneFactor = 1.0f + 0.25f * fortune;
        final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
        woodVolume.multiplyVolume(fortuneFactor);
        final List<ItemStack> woodItems = destroyData.species.getBranchesDrops(level, woodVolume, heldItem);

        // Drop the FallingTreeEntity into the level.
        FallingTreeEntity.dropTree(level, destroyData, woodItems, FallingTreeEntity.DestroyType.HARVEST);

        // Damage the axe by a prescribed amount.
        this.damageAxe(entity, heldItem, this.getRadius(state), woodVolume, true);
    }

    public BranchDestructionData destroyBranchFromNode(Level level, BlockPos cutPos, Direction toolDir, boolean wholeTree, @javax.annotation.Nullable final LivingEntity entity) {
        final BlockState blockState = level.getBlockState(cutPos);
        final SpeciesNode speciesNode = new SpeciesNode();
        final MapSignal signal = analyse(blockState, level, cutPos, null, new MapSignal(speciesNode)); // Analyze entire tree network to find root node and species.
        final Species species = speciesNode.getSpecies(); // Get the species from the root node.

        // Analyze only part of the tree beyond the break point and map out the extended block states.
        // We can't destroy the branches during this step since we need accurate extended block states that include connections.
        StateNode stateMapper = new StateNode(cutPos);
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(stateMapper));

        // Analyze only part of the tree beyond the break point and calculate its volume, then destroy the branches.
        final NetVolumeNode volumeSum = new NetVolumeNode();
        final RootsDestroyerNode destroyer = new RootsDestroyerNode(getFamily());
        destroyMode = DynamicTrees.DestroyMode.HARVEST;
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;

        // Calculate main trunk height.
        int trunkHeight = 1;
        for (BlockPos iter = new BlockPos(0, 1, 0); stateMapper.getBranchConnectionMap().containsKey(iter); iter = iter.above()) {
            trunkHeight++;
        }

        Direction cutDir = signal.localRootDir;
        if (cutDir == null) {
            cutDir = Direction.DOWN;
        }

        return new BranchDestructionData(species, stateMapper.getBranchConnectionMap(), new HashMap<>(), new ArrayList<>(), destroyer.getEnds(), volumeSum.getVolume(), cutPos, cutDir, toolDir, trunkHeight);
    }

    //////////////////////////////
    // SHAPE
    //////////////////////////////

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if (pState.getValue(LAYER) == Layer.EXPOSED) return Shapes.empty();
        return super.getOcclusionShape(pState, pLevel, pPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (isFullBlock(pState)) {
            VoxelShape fullShape = Shapes.block();
            if (getFamily().getPrimitiveCoveredRoots().isPresent())
                fullShape = getFamily().getPrimitiveCoveredRoots().get().getCollisionShape(pState, pLevel, pPos, pContext);
            return fullShape;
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if (isFullBlock(state)) {
            return Shapes.block();
        }
        int thisRadiusInt = getRadius(state);
        double radius = thisRadiusInt / 16.0;
        VoxelShape core = Shapes.box(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);

        for (Direction dir : Direction.values()) {
            int sideRadiusInt = Math.min(getSideConnectionRadius(level, pos, thisRadiusInt, dir), thisRadiusInt);
            double sideRadius = sideRadiusInt / 16.0f;
            if (sideRadius > 0.0f) {
                double gap = 0.5f - sideRadius;
                AABB aabb = new AABB(0.5 - sideRadius, 0.5 - sideRadius, 0.5 - sideRadius, 0.5 + sideRadius, 0.5 + sideRadius, 0.5 + sideRadius);
                aabb = aabb.expandTowards(dir.getStepX() * gap, dir.getStepY() * gap, dir.getStepZ() * gap);
                core = Shapes.or(core, Shapes.create(aabb));
            }
        }

        return core;
    }

    protected int getSideConnectionRadius(BlockGetter level, BlockPos pos, int radius, Direction side) {
        final BlockPos deltaPos = pos.relative(side);
        final BlockState blockState = CoordUtils.getStateSafe(level, deltaPos);

        // If adjacent block is not loaded assume there is no connection.
        return blockState == null ? 0 : TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, level, deltaPos, this, side, radius);
    }

    //////////////////////////////
    // GROWTH
    //////////////////////////////

    private boolean canGrowInto(Level level, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        return getFamily().isReplaceableByRoots(state.getBlock()) || state.getMaterial().isReplaceable();
    }

    public GrowSignal growIntoAir(Level level, BlockPos pos, GrowSignal signal, int fromRadius) {
        if (isNextToBranch(level, pos, signal.dir.getOpposite())) {
            signal.success = false;
            return signal;
        }
        setRadius(level, pos, getFamily().getPrimaryThickness(), null);
        signal.radius = getFamily().getSecondaryThickness();
        signal.success = true;

        return signal;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        // This is always placed at the beginning of every growSignal function
        if (!signal.step()) {
            return signal;
        }

        final BlockState currBlockState = level.getBlockState(pos);
        final Species species = signal.getSpecies();
        final boolean inTrunk = signal.isInTrunk();

        final Direction originDir = signal.dir.getOpposite();// Direction this signal originated from
        final Direction targetDir = species.getGrowthLogicKit().selectNewDirection( // This must be cached on the stack for proper recursion
                new DirectionSelectionContext(level, pos, species, this, signal)
        );
        signal.doTurn(targetDir);

        {
            final BlockPos deltaPos = pos.relative(targetDir);
            final BlockState deltaState = level.getBlockState(deltaPos);

            // Pass grow signal to next block in path
            final TreePart treepart = TreeHelper.getTreePart(deltaState);
            if (treepart != TreeHelper.NULL_TREE_PART) {
                signal = treepart.growSignal(level, deltaPos, signal);// Recurse
            } else if (canGrowInto(level, deltaPos)) {
                signal = growIntoAir(level, deltaPos, signal, getRadius(currBlockState));
            }
        }

        // Calculate Branch Thickness based on neighboring branches
        float areaAccum = signal.radius * signal.radius;// Start by accumulating the branch we just came from

        boolean theresPods = false;
        for (Direction dir : Direction.values()) {
            if (!dir.equals(originDir) && !dir.equals(targetDir)) {// Don't count where the signal originated from or the branch we just came back from
                BlockPos deltaPos = pos.relative(dir);

                // If it is decided to implement a special block(like a squirrel hole, tree
                // swing, rotting, burned or infested branch, etc) then this new block could be
                // derived from BlockBranch and this works perfectly. Should even work with
                // tileEntity blocks derived from BlockBranch.
                BlockState blockState = level.getBlockState(deltaPos);
                TreePart treepart = TreeHelper.getTreePart(blockState);
                if (isSameTree(treepart)) {
                    int branchRadius = treepart.getRadius(blockState);
                    areaAccum += branchRadius * branchRadius;
                }
                if (blockState.getBlock() instanceof OffsetablePodBlock) theresPods = true;
            }
        }

        //Only continue to set radii if the tree growth isn't choked out
        if (!signal.choked) {
            // Ensure that side branches are not thicker than the size of a block.  Also enforce species max thickness
            int maxRadius = inTrunk ? species.getMaxBranchRadius() : Math.min(species.getMaxBranchRadius(), MAX_RADIUS);

            // The new branch should be the square root of all of the sums of the areas of the branches coming into it.
            // But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
            signal.radius = Mth.clamp((float) Math.sqrt(areaAccum) + species.getTapering(), getRadius(currBlockState), maxRadius);// WOW!
            int targetRadius = (int) Math.floor(signal.radius);
            //if the tree has pods then growth needs to cause updates, otherwise don't bother (for performance)
            int flags = theresPods ? 3 : 2;
            int setRad = setRadius(level, pos, targetRadius, originDir, flags);
            if (setRad < targetRadius) { //We tried to set a radius but it didn't comply because something is in the way.
                signal.choked = true; //If something is in the way then it means that the tree growth is choked
            }
        }

        return signal;
    }
}
