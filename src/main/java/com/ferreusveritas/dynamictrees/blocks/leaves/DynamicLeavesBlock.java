package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.Ageable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.Cell;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RayTraceCollision;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class DynamicLeavesBlock extends LeavesBlock implements TreePart, Ageable, RayTraceCollision {

    public LeavesProperties properties = LeavesProperties.NULL_PROPERTIES;

    public DynamicLeavesBlock(final LeavesProperties leavesProperties, final Properties properties) {
        this(properties);
        this.setProperties(leavesProperties);
        leavesProperties.setDynamicLeavesState(defaultBlockState());
    }

    public DynamicLeavesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, LeavesProperties.maxHydro).setValue(PERSISTENT, false));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT);
    }

    public void setProperties(LeavesProperties properties) {
        this.properties = properties;
    }

    public LeavesProperties getProperties(BlockState blockState) {
        return properties;
    }

    @Override
    public Family getFamily(BlockState state, IBlockReader reader, BlockPos pos) {
        return getProperties(state).getFamily();
    }

    // Get Leaves-specific flammability
    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return this.getProperties(world.getBlockState(pos)).getFlammability();
    }

    // Get Leaves-specific fire spread speed
    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return this.getProperties(world.getBlockState(pos)).getFireSpreadSpeed();
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return this.getFlammability(state, world, pos, face) > 0 || face == Direction.UP;
    }

    @Nonnull
    public BlockState updateShape(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, @Nonnull IWorld worldIn, @Nonnull BlockPos currentPos, BlockPos facingPos) {
        return stateIn;
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState();
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (rand.nextInt(DTConfigs.TREE_GROWTH_FOLDING.get()) != 0) {
            return;
        }

        double attempts = DTConfigs.TREE_GROWTH_FOLDING.get() * DTConfigs.TREE_GROWTH_MULTIPLIER.get();

        if (attempts >= 1.0f || rand.nextFloat() < attempts) {
            doTick(world, pos, state, rand);
        }

        int start = rand.nextInt(26);

        while (--attempts > 0) {
            if (attempts >= 1.0f || rand.nextFloat() < attempts) {
                int r = (start++ % 26) + 14; // 14 - 39
                r = r > 26 ? r - 13 : r - 14; // 0 - 26 but Skip 13
                final BlockPos dPos = pos.offset((r % 3) - 1, ((r / 3) % 3) - 1, ((r / 9) % 3) - 1);// (-1, -1, -1) to (1, 1, 1) skipping (0, 0, 0)
                final BlockState dState = world.getBlockState(dPos);

                if (dState.getBlock() instanceof DynamicLeavesBlock) {
                    ((DynamicLeavesBlock) dState.getBlock()).doTick(world, dPos, dState, rand);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
    }

    protected void doTick(World world, BlockPos pos, BlockState state, Random rand) {
        if (canTickAt(world, pos) && getProperties(state).updateTick(world, pos, state, rand)) {
            age(world, pos, state, rand, SafeChunkBounds.ANY);
        }
    }

    protected boolean canTickAt(World world, BlockPos pos) {
        // Check 2 blocks away for loaded chunks
        int xm = pos.getX() - ((pos.getX() >> 4) << 4);
        int zm = pos.getZ() - ((pos.getZ() >> 4) << 4);
        if (xm > 1 && xm < 14 && zm > 1 && zm < 14) {
            return world.isLoaded(pos);
        }
        return world.isAreaLoaded(pos, 2);
    }

    public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
        return false;
    }

    @Override
    public int age(IWorld world, BlockPos pos, BlockState state, Random rand, SafeChunkBounds safeBounds) {
        final LeavesProperties leavesProperties = getProperties(state);
        final int oldHydro = state.getValue(DynamicLeavesBlock.DISTANCE);

        final boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        if (!getProperties(state).shouldAge(worldGen, state)) {
            return oldHydro;
        }

        // Check hydration level.  Dry leaves are dead leaves.
        final int newHydro = getHydrationLevelFromNeighbors(world, pos, leavesProperties);

        if (newHydro == 0 || (!worldGen && !hasAdequateLight(state, world, leavesProperties, pos))) { // Light doesn't work right during worldgen so we'll just disable it during worldgen for now.
            world.removeBlock(pos, false); // No water, no light .. no leaves.
            return -1; // Leaves were destroyed.
        } else {
            if (oldHydro != newHydro) { // Only update if the hydro has changed. A little performance gain.
                // We do not use the 0x02 flag(update client) for performance reasons.  The clients do not need to know the hydration level of the leaves blocks as it
                // does not affect appearance or behavior, unless appearanceChangesWithHydro.  For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
                world.setBlock(pos, getLeavesBlockStateForPlacement(world, pos, leavesProperties.getDynamicLeavesState(newHydro), oldHydro, worldGen), appearanceChangesWithHydro(oldHydro, newHydro) ? 2 : 4);
            }
        }

        // We should do this even if the hydro is only 1.  Since there could be adjacent branch blocks that could use a leaves block
        for (Direction dir : Direction.values()) { // Go on all 6 sides of this block
            if (newHydro > 1 || rand.nextInt(4) == 0) { // we'll give it a 1 in 4 chance to grow leaves if hydro is low to help performance
                BlockPos offpos = pos.relative(dir);
                if (safeBounds.inBounds(offpos, true) && isLocationSuitableForNewLeaves(world, leavesProperties, offpos)) { // Attempt to grow new leaves
                    int hydro = getHydrationLevelFromNeighbors(world, offpos, leavesProperties);
                    if (hydro > 0) {
                        world.setBlock(offpos, getLeavesBlockStateForPlacement(world, offpos, leavesProperties.getDynamicLeavesState(hydro), 0, worldGen), 2); // Removed Notify Neighbors Flag for performance
                    }
                }
            }
        }

        return newHydro; // Leaves were not destroyed
    }

    /**
     * Provides a method to add custom leaves properties besides the normal hydro.
     *
     * @param world                The world
     * @param pos                  Position of the new leaves blck
     * @param leavesStateWithHydro The state of the leaves with the hydro applied
     * @param oldHydro             the hydro value of the leaves before the palcement. Will be 0 if its a new leaf
     * @param worldGen             true if this is happening during worldgen
     * @return A provider for adding more blockstate properties
     */
    public BlockState getLeavesBlockStateForPlacement(IWorld world, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
        return leavesStateWithHydro; //by default just pass the blockstate along
    }

    @Override
    public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        return getProperties(state).getPrimitiveLeaves().getDestroyProgress(player, worldIn, pos);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getProperties(state).getPrimitiveLeavesItemStack();
    }

    /**
     * We will disable landing effects because we crush the blocks on landing and create our own particles in
     * crushBlock()
     */
    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld world, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // This is for blocks that check the shape, for example snow.
        if (context.getEntity() == null) {
            return VoxelShapes.block();
        }

        if (isLeavesPassable() || this.isEntityPassable(context)) {
            return VoxelShapes.empty();
        } else if (DTConfigs.VANILLA_LEAVES_COLLISION.get()) {
            return VoxelShapes.block();
        } else {
            return VoxelShapes.create(new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.50, 0.875));
        }
    }

    protected boolean isLeavesPassable() {
        return DTConfigs.IS_LEAVES_PASSABLE.get() || ModList.get().isLoaded(DynamicTrees.PASSABLE_FOLIAGE);
    }

    public boolean isEntityPassable(ISelectionContext context) {
        return isEntityPassable(context.getEntity());
    }

    public boolean isEntityPassable(@Nullable Entity entity) {
        if (entity instanceof ProjectileEntity) //Projectiles such as arrows fly through leaves
        {
            return true;
        }
        if (entity instanceof ItemEntity) //Seed items fall through leaves
        {
            return ((ItemEntity) entity).getItem().getItem() instanceof Seed;
        }
        if (entity instanceof LivingEntity) //Bees fly through leaves, otherwise they get stuck :(
        {
            return entity instanceof BeeEntity;
        }
        return false;
    }

    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float fallDistance) {
        // We are only interested in Living things crashing through the canopy.
        if (!DTConfigs.CANOPY_CRASH.get() || !(entity instanceof LivingEntity)) {
            return;
        }

        entity.fallDistance--;

        final AxisAlignedBB aabb = entity.getBoundingBox();

        final int minX = MathHelper.floor(aabb.minX + 0.001D);
        final int minZ = MathHelper.floor(aabb.minZ + 0.001D);
        final int maxX = MathHelper.floor(aabb.maxX - 0.001D);
        final int maxZ = MathHelper.floor(aabb.maxZ - 0.001D);

        boolean crushing = true;
        boolean hasLeaves = true;

        final SoundType stepSound = this.getSoundType(world.getBlockState(pos), world, pos, entity);
        final float volume = MathHelper.clamp(stepSound.getVolume() / 16.0f * fallDistance, 0, 3.0f);
        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), stepSound.getBreakSound(), SoundCategory.BLOCKS, volume, stepSound.getPitch(), false);

        for (int iy = 0; (entity.fallDistance > 3.0f) && crushing && ((pos.getY() - iy) > 0); iy++) {
            if (hasLeaves) { // This layer has leaves that can help break our fall
                entity.fallDistance *= 0.66f; // For each layer we are crushing break the momentum
                hasLeaves = false;
            }

            for (int ix = minX; ix <= maxX; ix++) {
                for (int iz = minZ; iz <= maxZ; iz++) {
                    BlockPos iPos = new BlockPos(ix, pos.getY() - iy, iz);
                    BlockState state = world.getBlockState(iPos);
                    if (TreeHelper.isLeaves(state)) {
                        hasLeaves = true; // This layer has leaves
                        DTClient.crushLeavesBlock(world, iPos, state, entity);
                        world.removeBlock(iPos, false);
                    } else if (!world.isEmptyBlock(iPos)) {
                        crushing = false; // We hit something solid thus no longer crushing leaves layers
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (isLeavesPassable() || isEntityPassable(entity)) {
            super.entityInside(state, world, pos, entity);
        } else {
            if (entity.getDeltaMovement().y < 0.0D && entity.fallDistance < 2.0f) {
                entity.fallDistance = 0.0f;
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y * 0.5D, entity.getDeltaMovement().z); // Slowly sink into the block
            } else if (entity.getDeltaMovement().y > 0 && entity.getDeltaMovement().y < 0.25D) {
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y + 0.025, entity.getDeltaMovement().z); // Allow a little climbing
            }

            entity.setSprinting(false); // One cannot sprint upon tree tops
            entity.setDeltaMovement(entity.getDeltaMovement().x * 0.25D, entity.getDeltaMovement().y, entity.getDeltaMovement().z * 0.25D); // Make travel slow and laborious
        }
    }

    /**
     * Checks to see if the location at pos is suitable for new leaves and if so set new leaves at pos with hydro value
     *
     * @param world      The world
     * @param leavesProp Properties of the leaves we are working with
     * @param pos        The position of interest
     * @param hydro      The hydration value for the resulting cell
     * @return {@code true} if the location was suitable (and so leaves were placed); {@code false} otherwise.
     */
    public boolean growLeavesIfLocationIsSuitable(IWorld world, LeavesProperties leavesProp, BlockPos pos, int hydro) {
        hydro = hydro == 0 ? leavesProp.getCellKit().getDefaultHydration() : hydro;
        if (isLocationSuitableForNewLeaves(world, leavesProp, pos)) {
            world.setBlock(pos, getLeavesBlockStateForPlacement(world, pos, leavesProp.getDynamicLeavesState(hydro), 0, false), 2); // Removed Notify Neighbors Flag for performance.
            return true;
        }
        return false;
    }

    /**
     * Checks the {@link Block} at the given {@link BlockPos} is suitable for growing new leaves.
     *
     * @param world            The {@link World} instance.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @param pos              The {@link BlockPos} for the new leaves.
     * @return {@code true} if the {@link BlockPos} is suitable for new leaves; {@code false} otherwise.
     */
    public boolean isLocationSuitableForNewLeaves(IWorld world, LeavesProperties leavesProperties, BlockPos pos) {
        final BlockState blockState = world.getBlockState(pos);
        final Block block = blockState.getBlock();

        if (block instanceof DynamicLeavesBlock) {
            return false;
        }

        final BlockState belowBlockState = world.getBlockState(pos.below());

        // Prevent leaves from growing on the ground or above liquids.
        if (!leavesProperties.canGrowOnGround() && ((belowBlockState.canOcclude() && !TreeHelper.isBranch(belowBlockState) && !(belowBlockState.getBlock() instanceof LeavesBlock)) || belowBlockState.getBlock() instanceof FlowingFluidBlock)) {
            return false;
        }

        // Help to grow into double tall grass and ferns in a more natural way.
        final BlockState stateDown = world.getBlockState(pos.below());
        if (block instanceof DoublePlantBlock && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER &&
                stateDown.getBlock() instanceof DoublePlantBlock && stateDown.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            if (block == Blocks.TALL_GRASS) {
                world.setBlock(pos.below(), Blocks.GRASS.defaultBlockState(), 3);
            } else if (block == Blocks.LARGE_FERN) {
                world.setBlock(pos.below(), Blocks.FERN.defaultBlockState(), 3);
            }
            world.removeBlock(pos, false);
        }

        return (world.isEmptyBlock(pos) || world.getBlockState(pos).getMaterial().isReplaceable()) && hasAdequateLight(blockState, world, leavesProperties, pos);
    }

    /**
     * Checks that the {@link DynamicLeavesBlock} at the given {@link BlockPos} has enough light to exist.
     *
     * @param blockState       The {@link BlockState} of the {@link DynamicLeavesBlock} to check.
     * @param world            The {@link IWorld} instance.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @param pos              The {@link BlockPos} of the {@link DynamicLeavesBlock}.
     * @return {@code true} if the {@link Block} has adequate light; {@code false otherwise}.
     */
    public boolean hasAdequateLight(BlockState blockState, IWorld world, LeavesProperties leavesProperties, BlockPos pos) {

        // If clear sky is above the block then we needn't go any further.
        if (world.canSeeSkyFromBelowWater(pos)) {
            return true;
        }

        final int smother = leavesProperties.getSmotherLeavesMax();

        // Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
        if (smother != 0) {
            if (isBottom(world, pos)) { // Only act on the bottom block of the Growable stack
                // Prevent leaves from growing where they would be "smothered" from too much above foliage

                int smotherLeaves = 0;

                for (int i = 0; i < smother; i++) {
                    smotherLeaves += TreeHelper.isTreePart(world, pos.above(i + 1)) ? 1 : 0;
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

        return world.getBrightness(LightType.SKY, pos) >= (TreeHelper.isLeaves(blockState) ? leavesProperties.getLightRequirement() - 2 : leavesProperties.getLightRequirement());
    }

    /**
     * Checks if the {@link DynamicLeavesBlock} at the given {@link BlockPos} is at the bottom of the stack.
     *
     * @param world The {@link World} instance.
     * @param pos   The {@link BlockPos} for the leaves {@link Block} to check.
     * @return {@code true} if the {@link Block} is at the bottom of the stack; {@code false} otherwise.
     */
    public static boolean isBottom(IWorld world, BlockPos pos) {
        final BlockState belowBlockState = world.getBlockState(pos.below());
        final TreePart belowTreepart = TreeHelper.getTreePart(belowBlockState);

        if (belowTreepart != TreeHelper.NULL_TREE_PART) {
            return belowTreepart.getRadius(belowBlockState) > 1; // False for leaves, twigs, and dirt. True for stocky branches.
        }

        return true; // Non-Tree parts below indicate the bottom of stack.
    }

    /**
     * Gathers hydration levels from neighbors before pushing the values into the solver.
     *
     * @param world            The {@link IWorld} instance.
     * @param pos              The {@link BlockPos} to get neighbors for.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @return The hydration from the solved cells.
     */
    public int getHydrationLevelFromNeighbors(IWorld world, BlockPos pos, LeavesProperties leavesProperties) {
        final Cell[] cells = new Cell[6];

        for (Direction dir : Direction.values()) {
            final BlockPos deltaPos = pos.relative(dir);
            final BlockState state = world.getBlockState(deltaPos);
            final TreePart part = TreeHelper.getTreePart(state);

            cells[dir.ordinal()] = part.getHydrationCell(world, deltaPos, state, dir, leavesProperties);
        }

        return leavesProperties.getCellKit().getCellSolver().solve(cells); // Find center cell's value from neighbors.
    }

    @Override
    public Cell getHydrationCell(IBlockReader reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return dir != null ? leavesProperties.getCellKit().getCellForLeaves(state.getValue(LeavesBlock.DISTANCE)) : CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
        if (signal.step()) // This is always placed at the beginning of every growSignal function.
        {
            this.branchOut(world, pos, signal); // When a growth signal hits a leaf block it attempts to become a tree branch.}
        }
        return signal;
    }

    /**
     * Will place a leaves block if the position is air and it's possible to create one there. Otherwise it will check
     * to see if the block is already there.
     *
     * @param world            The {@link World} instance.
     * @param pos              The {@link BlockPos} to check.
     * @param leavesProperties The {@link LeavesProperties} required.
     * @return {@code true} if the leaves are at the given {@link BlockPos}; {@code false} otherwise.
     */
    public boolean needLeaves(World world, BlockPos pos, LeavesProperties leavesProperties, Species species) {
        if (world.isEmptyBlock(pos)) { // Place leaves if air.
            return this.growLeavesIfLocationIsSuitable(world, leavesProperties, pos, leavesProperties.getCellKit().getDefaultHydration());
        } else { // Otherwise check if there's already this type of leaves there.
            final TreePart treePart = TreeHelper.getTreePart(world.getBlockState(pos));


            return treePart instanceof DynamicLeavesBlock && species.isValidLeafBlock((DynamicLeavesBlock) treePart); // Check if this leaves are valid for the species
        }
    }

    public GrowSignal branchOut(World world, BlockPos pos, GrowSignal signal) {

        Species species = signal.getSpecies();
        LeavesProperties leavesProperties = species.getLeavesProperties();

        //Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
        if (!needLeaves(world, pos, leavesProperties, species)) {
            signal.success = false;
            return signal;
        }

        //Check to see if there's neighboring branches and abort if there's any found.
        if (BranchBlock.isNextToBranch(world, pos, signal.dir.getOpposite())) {
            signal.success = false;
            return signal;
        }

        boolean hasLeaves = false;

        for (Direction dir : Direction.values()) {
            if (needLeaves(world, pos.relative(dir), leavesProperties, species)) {
                hasLeaves = true;
                break;
            }
        }

        if (hasLeaves) {
            //Finally set the leaves block to a branch
            Family family = species.getFamily();
            family.getBranchForPlacement(world, species, pos).ifPresent(branch ->
                    branch.setRadius(world, pos, family.getPrimaryThickness(), null)
            );
            signal.radius = family.getSecondaryThickness();//For the benefit of the parent branch
        }

        signal.success = hasLeaves;

        return signal;
    }

    @Override
    public int probabilityForBlock(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from) {
        return from.getFamily().isCompatibleDynamicLeaves(from.getFamily().getCommonSpecies(), state, reader, pos) ? 2 : 0;
    }

    //////////////////////////////
    // DROPS
    //////////////////////////////

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return true; // We'll handle this in #getDrops as some leave drops may not require a tool.
    }

    @Override
    public boolean isShearable(@Nonnull ItemStack item, World world, BlockPos pos) {
        return this.getProperties(world.getBlockState(pos)).canBeSheared();
    }

    @Override
    public List<ItemStack> onSheared(@Nullable PlayerEntity player, ItemStack item, World world, BlockPos pos, int fortune) {
        return this.getDrops(player, item, world, pos, fortune);
    }

    /**
     * Gets the drops for this {@link DynamicLeavesBlock}. Default implementation simply returns the primitive leaves
     * stack.
     *
     * @param player  The {@link PlayerEntity} who destroyed the {@link DynamicLeavesBlock}.
     * @param item    The {@link ItemStack} used to destroy the {@link DynamicLeavesBlock}.
     * @param world   The {@link World} instance.
     * @param pos     The {@link BlockPos} of the {@link DynamicLeavesBlock}.
     * @param fortune The {@code fortune} on the tool used.
     * @return The {@link List} of {@link ItemStack}s to drop.
     */
    public List<ItemStack> getDrops(@Nullable PlayerEntity player, ItemStack item, World world, BlockPos pos, int fortune) {
        return new ArrayList<>(Collections.singletonList(this.getProperties(world.getBlockState(pos)).getPrimitiveLeavesItemStack()));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        final Vector3d originPos = builder.getOptionalParameter(LootParameters.ORIGIN);
        final ResourceLocation lootTableName;
        Species species = Species.NULL_SPECIES;
        BlockPos pos = BlockPos.ZERO;

        if (originPos == null) {
            lootTableName = getLootTable();
        } else {
            pos = new BlockPos(originPos.x(), originPos.y(), originPos.z());
            species = getExactSpecies(builder.getLevel(), pos, getProperties(state));
            lootTableName = species.getLeavesBlockDropsPath();
        }

        if (lootTableName == LootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            ServerWorld world = builder.getLevel();
            LootContext context = builder
                    .withParameter(LootParameters.BLOCK_STATE, state)
                    .withParameter(DTLootParameters.SEASONAL_SEED_DROP_FACTOR,
                            species.seasonalSeedDropFactor(WorldContext.create(world), pos))
                    .create(LootParameterSets.BLOCK);
            LootTable table = world.getServer().getLootTables().get(lootTableName);
            return table.getRandomItems(context);
        }
    }

    /**
     * Gets the exact {@link Species} for these leaves (if able to find branches nearby). Warning! Resource intensive
     * algorithm. Use only for interactions like breaking blocks.
     *
     * @param world            The {@link World} instance.
     * @param pos              The {@link BlockPos} of the {@link DynamicLeavesBlock}.
     * @param leavesProperties The {@link LeavesProperties} instance.
     * @return The {@link Species} for the given leaves; otherwise {@link Species#NULL_SPECIES} if branches could not be
     * found nearby.
     */
    Species getExactSpecies(@Nullable final World world, final BlockPos pos, final LeavesProperties leavesProperties) {
        if (world == null) {
            return Species.NULL_SPECIES;
        }

        final List<BlockPos> branchList = new ArrayList<>();

        // Find all of the branches that are nearby
        for (BlockPos dPos : leavesProperties.getCellKit().getLeafCluster().getAllNonZero()) {
            dPos = pos.offset(BlockPos.ZERO.subtract(dPos));//Becomes immutable at this point
            final BlockState state = world.getBlockState(dPos);

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
        BlockPos closest = branchList.get(0);
        double minDist = 999;

        for (BlockPos dPos : branchList) {
            final double d = pos.distSqr(dPos);

            if (d < minDist) {
                minDist = d;
                closest = dPos;
            }
        }

        return TreeHelper.getExactSpecies(world, closest);
    }

    //////////////////////////////
    // RENDERING FUNCTIONS
    //////////////////////////////

    @Override
    public int getRadiusForConnection(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return getProperties(state).getRadiusForConnection(state, reader, pos, from, side, fromRadius);
    }

    @Override
    public int getRadius(BlockState state) {
        return 0;
    }

    /**
     * Generally Leaves blocks should not be analyzed
     */
    @Override
    public boolean shouldAnalyse(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public MapSignal analyse(BlockState state, IWorld world, BlockPos pos, Direction fromDir, MapSignal signal) {
        return signal; // Shouldn't need to run analysis on leaf blocks.
    }

    @Override
    public int branchSupport(BlockState state, IBlockReader reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        // Leaves are only support for "twigs".
        return radius == branch.getFamily().getPrimaryThickness() && branch.getFamily() == getFamily(state, reader, pos) ? BranchBlock.setSupport(0, 1) : 0;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
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
    public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0.2F;
    }

}