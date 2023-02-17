package com.ferreusveritas.dynamictrees.block.branch;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.FutureBreakable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity.DestroyType;
import com.ferreusveritas.dynamictrees.event.FutureBreak;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.nodemapper.DestroyerNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.SpeciesNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.StateNode;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.*;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public abstract class BranchBlock extends BlockWithDynamicHardness implements TreePart, FutureBreakable {

    public static final int MAX_RADIUS = 8;
    public static final String NAME_SUFFIX = "_branch";

    public static DynamicTrees.DestroyMode destroyMode = DynamicTrees.DestroyMode.SLOPPY;

    /**
     * The {@link Family} for this {@link BranchBlock}.
     */
    private Family family = Family.NULL_FAMILY;
    private ItemStack[] primitiveLogDrops = new ItemStack[]{};
    private boolean canBeStripped;

    /**
     * @param name name of branch, without a {@code _branch} suffix
     */
    public BranchBlock(ResourceLocation name, Material material) {
        this(name, Properties.of(material));
    }

    /**
     * @param name name of branch, without a {@code _branch} suffix
     */
    public BranchBlock(ResourceLocation name, Properties properties) {
        super(properties); //removes drops from block
        lootTableSupplier = new LootTableSupplier("trees/branches/", name);
    }

    public BranchBlock setCanBeStripped(boolean truth) {
        canBeStripped = truth;
        return this;
    }

    ///////////////////////////////////////////
    // TREE INFORMATION
    ///////////////////////////////////////////

    public void setFamily(Family tree) {
        this.family = tree;
    }

    public Family getFamily() {
        return family;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos pos) {
        return getFamily();
    }

    public boolean isSameTree(TreePart treepart) {
        return isSameTree(TreeHelper.getBranch(treepart));
    }

    public boolean isSameTree(BlockState state) {
        return TreeHelper.getBranchOpt(state).map(branch -> this.getFamily() == branch.getFamily()).orElse(false);
    }

    /**
     * Branches are considered the same if they have the same tree.
     *
     * @param branch The {@link BranchBlock} to compare with.
     * @return {@code true} if this and the given {@link BranchBlock} are from the same {@link Family}; {@code false}
     * otherwise.
     */
    public boolean isSameTree(@Nullable final BranchBlock branch) {
        return branch != null && this.getFamily() == branch.getFamily();
    }

    public Optional<Block> getPrimitiveLog() {
        return this.isStrippedBranch() ? this.family.getPrimitiveStrippedLog() : this.family.getPrimitiveLog();
    }

    public boolean isStrippedBranch() {
        return this.getFamily().getStrippedBranch().map(other -> other == this).orElse(false);
    }

    @Override
    public abstract int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius);

    ///////////////////////////////////////////
    // WORLD UPDATE
    ///////////////////////////////////////////

    /**
     * @param level     The level
     * @param pos       The branch block position
     * @param fertility The fertility of the tree.
     * @param radius    The radius of the branch that's the subject of rotting
     * @param rand      A random number generator for convenience
     * @param rapid     If true then unsupported branch postRot will occur regardless of chance value. This will also
     *                  postRot the entire unsupported branch at once. True if this postRot is happening under a
     *                  generation scenario as opposed to natural tree updates
     * @return true if the branch was destroyed because of postRot
     */
    public abstract boolean checkForRot(LevelAccessor level, BlockPos pos, Species species, int fertility, int radius, Random rand, float chance, boolean rapid);

    public static int setSupport(int branches, int leaves) {
        return ((branches & 0xf) << 4) | (leaves & 0xf);
    }

    public static int getBranchSupport(int support) {
        return (support >> 4) & 0xf;
    }

    public static int getLeavesSupport(int support) {
        return support & 0xf;
    }

    public static boolean isNextToBranch(Level level, BlockPos pos, Direction originDir) {
        for (Direction dir : Direction.values()) {
            if (!dir.equals(originDir)) {
                if (TreeHelper.isBranch(level.getBlockState(pos.relative(dir)))) {
                    return true;
                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Deprecated
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        final ItemStack heldItem = player.getItemInHand(hand);
        return TreeHelper.getTreePart(state).getFamily(state, level, pos).onTreeActivated(
                new Family.TreeActivationContext(
                        level, TreeHelper.findRootNode(level, pos), pos, state, player, hand, heldItem, hitResult
                )
        ) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public boolean canBeStripped(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        final int stripRadius = DTConfigs.MIN_RADIUS_FOR_STRIP.get();
        return stripRadius != 0 && stripRadius <= this.getRadius(state) && this.canBeStripped && heldItem.canPerformAction(ToolActions.AXE_STRIP);
    }


    public void stripBranch(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        final int radius = this.getRadius(state);
        this.damageAxe(player, heldItem, radius / 2, new NetVolumeNode.Volume((radius * radius * 64) / 2), false);

        this.stripBranch(state, level, pos, radius);
    }

    public void stripBranch(BlockState state, LevelAccessor level, BlockPos pos) {
        this.stripBranch(state, level, pos, this.getRadius(state));
    }

    public void stripBranch(BlockState state, LevelAccessor level, BlockPos pos, int radius) {
        this.getFamily().getStrippedBranch().ifPresent(strippedBranch ->
                strippedBranch.setRadius(
                        level,
                        pos,
                        Math.max(1, radius - (DTConfigs.ENABLE_STRIP_RADIUS_REDUCTION.get() ? 1 : 0)),
                        null
                )
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return this.getFamily().getBranchItem().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }


    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    public Connections getConnectionData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        final Connections connections = new Connections();

        if (state.getBlock() != this) {
            return connections;
        }

        final int coreRadius = this.getRadius(state);
        for (final Direction dir : Direction.values()) {
            final BlockPos deltaPos = pos.relative(dir);
            final BlockState neighborBlockState = level.getBlockState(deltaPos);
            final int sideRadius = TreeHelper.getTreePart(neighborBlockState).getRadiusForConnection(neighborBlockState, level, deltaPos, this, dir, coreRadius);
            connections.setRadius(dir, Mth.clamp(sideRadius, 0, coreRadius));
        }

        return connections;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    ///////////////////////////////////////////
    // GROWTH
    ///////////////////////////////////////////

    @Override
    public int getRadius(BlockState state) {
        return 1;
    }

    public abstract int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags);

    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir) {
        return setRadius(level, pos, radius, originDir, 2);
    }

    public abstract BlockState getStateForRadius(int radius);

    public int getMaxRadius() {
        return MAX_RADIUS;
    }

    ///////////////////////////////////////////
    // NODE ANALYSIS
    ///////////////////////////////////////////

    /**
     * Generally, all branch blocks should be analyzed.
     */
    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /**
     * Holds an {@link ItemStack} and the {@link BlockPos} in which it should be dropped.
     */
    public static class ItemStackPos {
        public final ItemStack stack;
        public final BlockPos pos;

        public ItemStackPos(ItemStack stack, BlockPos pos) {
            this.stack = stack;
            this.pos = pos;
        }
    }

    /**
     * Destroys all branches recursively not facing the branching direction with the root node
     *
     * @param level     The {@link Level} instance.
     * @param cutPos    The {@link BlockPos} of the branch being destroyed.
     * @param toolDir   The face that was pounded on when breaking the block at the given {@code cutPos}.
     * @param wholeTree {@code true} if the whole tree should be destroyed; otherwise {@code false} if only the branch
     *                  should.
     * @return The {@link BranchDestructionData} {@link Object} created.
     */
    public BranchDestructionData destroyBranchFromNode(Level level, BlockPos cutPos, Direction toolDir, boolean wholeTree, @Nullable final LivingEntity entity) {
        final BlockState blockState = level.getBlockState(cutPos);
        final SpeciesNode speciesNode = new SpeciesNode();
        final MapSignal signal = analyse(blockState, level, cutPos, null, new MapSignal(speciesNode)); // Analyze entire tree network to find root node and species.
        final Species species = speciesNode.getSpecies(); // Get the species from the root node.

        // Analyze only part of the tree beyond the break point and map out the extended block states.
        // We can't destroy the branches during this step since we need accurate extended block states that include connections.
        StateNode stateMapper = new StateNode(cutPos);
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(stateMapper));

        // Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches.
        final NetVolumeNode volumeSum = new NetVolumeNode();
        final DestroyerNode destroyer = new DestroyerNode(species).setPlayer(entity instanceof Player ? (Player) entity : null);
        destroyMode = DynamicTrees.DestroyMode.HARVEST;
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;

        // Destroy all the leaves on the branch, store them in a map and convert endpoint coordinates from absolute to relative.
        List<BlockPos> endPoints = destroyer.getEnds();
        final Map<BlockPos, BlockState> destroyedLeaves = new HashMap<>();
        final List<ItemStackPos> leavesDropsList = new ArrayList<>();
        this.destroyLeaves(level, cutPos, species, entity == null ? ItemStack.EMPTY : entity.getMainHandItem(), endPoints, destroyedLeaves, leavesDropsList);
        endPoints = endPoints.stream().map(p -> p.subtract(cutPos)).collect(Collectors.toList());

        // Calculate main trunk height.
        int trunkHeight = 1;
        for (BlockPos iter = new BlockPos(0, 1, 0); stateMapper.getBranchConnectionMap().containsKey(iter); iter = iter.above()) {
            trunkHeight++;
        }

        Direction cutDir = signal.localRootDir;
        if (cutDir == null) {
            cutDir = Direction.DOWN;
        }

        return new BranchDestructionData(species, stateMapper.getBranchConnectionMap(), destroyedLeaves, leavesDropsList, endPoints, volumeSum.getVolume(), cutPos, cutDir, toolDir, trunkHeight);
    }

    /**
     * Performs rot action. Default implementation simply breaks the block.
     *
     * @param level The {@link Level} instance.
     * @param pos   The {@link BlockPos} of the block to rot.
     */
    public void rot(LevelAccessor level, BlockPos pos) {
        this.breakDeliberate(level, pos, DynamicTrees.DestroyMode.ROT);
    }

    /**
     * Destroyed all leaves on the {@link BranchBlock} at the {@code cutPos} into the given {@code destroyedLeaves}
     * {@link Map} that can be safely destroyed without harming surrounding leaves.
     *
     * <p>Drops are not handled by this method, but instead put into the given {@code drops} {@link List}.</p>
     *
     * @param level           The {@link Level} instance.
     * @param cutPos          The {@link BlockPos} of the {@link Block} that was initially destroyed.
     * @param species         The {@link Species} of the tree that being modified.
     * @param endPoints       A {@link List} of absolute {@link BlockPos} {@link Object}s of the branch endpoints.
     * @param destroyedLeaves A {@link Map} for collecting the {@link BlockPos} and {@link BlockState}s for all of the
     *                        {@link DynamicLeavesBlock} that are destroyed.
     * @param drops           A {@link List} for collecting the {@link ItemStack}s and their {@link BlockPos} relative
     *                        to the cut {@link BlockPos}.
     */
    public void destroyLeaves(final Level level, final BlockPos cutPos, final Species species, final ItemStack tool, final List<BlockPos> endPoints, final Map<BlockPos, BlockState> destroyedLeaves, final List<ItemStackPos> drops) {
        if (level.isClientSide || endPoints.isEmpty()) {
            return;
        }

        // Make a bounding volume that holds all of the endpoints and expand the volume for the leaves radius.
        final BlockBounds bounds = getFamily().expandLeavesBlockBounds(new BlockBounds(endPoints));

        // Create a voxmap to store the leaf destruction map.
        final SimpleVoxmap leafMap = new SimpleVoxmap(bounds);

        // For each of the endpoints add an expanded destruction volume around it.
        for (final BlockPos endPos : endPoints) {
            for (final BlockPos leafPos : getFamily().expandLeavesBlockBounds(new BlockBounds(endPos))) {
                leafMap.setVoxel(leafPos, (byte) 1); // Flag this position for destruction.
            }
            leafMap.setVoxel(endPos, (byte) 0); // We know that the endpoint does not have a leaves block in it because it was a branch.
        }

        final Family family = species.getFamily();
        final BranchBlock familyBranch = family.getBranch().get();
        final int primaryThickness = family.getPrimaryThickness();

        // Expand the volume yet again in all directions and search for other non-destroyed endpoints.
        for (final BlockPos findPos : getFamily().expandLeavesBlockBounds(bounds)) {
            final BlockState findState = level.getBlockState(findPos);
            if (familyBranch.getRadius(findState) == primaryThickness) { // Search for endpoints of the same tree family.
                final Iterable<BlockPos.MutableBlockPos> leaves = species.getLeavesProperties().getCellKit().getLeafCluster().getAllNonZero();
                for (BlockPos.MutableBlockPos leafPos : leaves) {
                    leafMap.setVoxel(findPos.getX() + leafPos.getX(), findPos.getY() + leafPos.getY(), findPos.getZ() + leafPos.getZ(), (byte) 0);
                }
            }
        }

        final List<ItemStack> dropList = new ArrayList<>();

        // Destroy all family compatible leaves.
        for (final Cell cell : leafMap.getAllNonZeroCells()) {
            final BlockPos.MutableBlockPos pos = cell.getPos();
            final BlockState state = level.getBlockState(pos);
            if (family.isCompatibleGenericLeaves(species, state, level, pos)) {
                dropList.clear();
                LeavesProperties leaves = Optional.ofNullable(TreeHelper.getLeaves(state))
                        .map(block -> block.getProperties(state))
                        .orElse(LeavesProperties.NULL);
                dropList.addAll(leaves.getDrops(level, pos, tool, species));
                final BlockPos imPos = pos.immutable(); // We are storing this so it must be immutable
                final BlockPos relPos = imPos.subtract(cutPos);
                level.setBlock(imPos, BlockStates.AIR, 3);
                destroyedLeaves.put(relPos, state);
                dropList.forEach(i -> drops.add(new ItemStackPos(i, relPos)));
            }
        }
    }

    public boolean canFall() {
        return false;
    }

    ///////////////////////////////////////////
    // DROPS AND HARVESTING
    ///////////////////////////////////////////

    private final LootTableSupplier lootTableSupplier;

    public boolean shouldGenerateBranchDrops() {
        return getPrimitiveLog().isPresent();
    }

    public ResourceLocation getLootTableName() {
        return lootTableSupplier.getName();
    }

    public LootTable getLootTable(LootTables lootTables, Species species) {
        return lootTableSupplier.get(lootTables, species);
    }

    /**
     * @deprecated use {@link Species#getBranchesDrops(Level, NetVolumeNode.Volume)}
     */
    @Deprecated(forRemoval = true)
    public List<ItemStack> getLogDrops(Level level, BlockPos pos, Species species, NetVolumeNode.Volume volume) {
        return species.getBranchesDrops(level, volume);
    }

    public LootTable.Builder createBranchDrops() {
        return DTLootTableProvider.createBranchDrops(getPrimitiveLog().get(), family.getStick(1).getItem());
    }

    public float getPrimitiveLogs(float volumeIn, List<ItemStack> drops) {
        int numLogs = (int) volumeIn;
        for (ItemStack stack : primitiveLogDrops) {
            int num = numLogs * stack.getCount();
            while (num > 0) {
                ItemStack drop = stack.copy();
                drop.setCount(Math.min(num, stack.getMaxStackSize()));
                drops.add(drop);
                num -= stack.getMaxStackSize();
            }
        }
        return volumeIn - numLogs;
    }

    public BranchBlock setPrimitiveLogDrops(ItemStack... drops) {
        primitiveLogDrops = drops;
        return this;
    }

    @Override
    public void futureBreak(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        // Tries to get the face being pounded on.
        final double reachDistance = entity instanceof Player ? entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5.0D;
        final BlockHitResult ragTraceResult = this.playerRayTrace(entity, reachDistance, 1.0F);
        final Direction toolDir = ragTraceResult != null ? (entity.isShiftKeyDown() ? ragTraceResult.getDirection().getOpposite() : ragTraceResult.getDirection()) : Direction.DOWN;

        // Play and render block break sound and particles (must be done before block is broken).
        level.levelEvent(null, 2001, cutPos, getId(state));

        // Do the actual destruction.
        final BranchDestructionData destroyData = this.destroyBranchFromNode(level, cutPos, toolDir, false, entity);

        // Get all of the wood drops.
        final ItemStack heldItem = entity.getMainHandItem();
        final int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
        final float fortuneFactor = 1.0f + 0.25f * fortune;
        final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
        woodVolume.multiplyVolume(fortuneFactor);
        final List<ItemStack> woodItems = destroyData.species.getBranchesDrops(level, woodVolume, heldItem);

        final float chance = 1.0f;

        // Build the final wood drop list taking chance into consideration.
        final List<ItemStack> woodDropList = woodItems.stream().filter(i -> level.random.nextFloat() <= chance).collect(Collectors.toList());

        // Drop the FallingTreeEntity into the level.
        FallingTreeEntity.dropTree(level, destroyData, woodDropList, DestroyType.HARVEST);

        // Damage the axe by a prescribed amount.
        this.damageAxe(entity, heldItem, this.getRadius(state), woodVolume, true);
    }


    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return this.removedByEntity(state, level, pos, player);
    }

    public boolean removedByEntity(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        FutureBreak.add(new FutureBreak(state, level, cutPos, entity, 0));
        return false;
    }

    protected void sloppyBreak(Level level, BlockPos cutPos, DestroyType destroyType) {
        // Do the actual destruction.
        final BranchDestructionData destroyData = this.destroyBranchFromNode(level, cutPos, Direction.DOWN, false, null);

        // Get all of the wood drops.
        final List<ItemStack> woodDropList = destroyData.species.getBranchesDrops(level, destroyData.woodVolume);

        // If sloppy break drops are off clear all drops.
        if (!DTConfigs.SLOPPY_BREAK_DROPS.get()) {
            destroyData.leavesDrops.clear();
            woodDropList.clear();
        }

        // This will drop the EntityFallingTree into the level.
        FallingTreeEntity.dropTree(level, destroyData, woodDropList, destroyType);
    }

    /**
     * This is a copy of Entity.rayTrace which is client side only. There's no reason for this function to be
     * client-side only as all of it's calls are client/server compatible.
     *
     * @param entity             The {@link LivingEntity} to ray trace from.
     * @param blockReachDistance The {@code reachDistance} of the entity.
     * @param partialTick        The partial ticks.
     * @return The {@link BlockHitResult} created.
     */
    @Nullable
    public BlockHitResult playerRayTrace(LivingEntity entity, double blockReachDistance, float partialTick) {
        Vec3 vec3d = entity.getEyePosition(partialTick);
        Vec3 vec3d1 = entity.getViewVector(partialTick);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return entity.level.clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
    }


    public void damageAxe(final LivingEntity entity, @Nullable final ItemStack heldItem, final int radius, final NetVolumeNode.Volume woodVolume, final boolean forBlockBreak) {
        if (heldItem == null || !heldItem.canPerformAction(ToolActions.AXE_DIG)) {
            return;
        }

        int damage;

        switch (DTConfigs.AXE_DAMAGE_MODE.get()) {
            default:
            case VANILLA:
                damage = 1;
                break;
            case THICKNESS:
                damage = Math.max(1, radius) / 2;
                break;
            case VOLUME:
                damage = (int) woodVolume.getVolume();
                break;
        }

        if (forBlockBreak) {
            damage--; // Minecraft already damaged the tool by one unit
        }

        if (damage > 0) {
            heldItem.hurtAndBreak(damage, entity, LivingEntity::tick);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
        if (level.isClientSide || destroyMode != DynamicTrees.DestroyMode.SLOPPY) {
            super.onRemove(state, level, pos, newState, flag);
            return;
        }

        // LogManager.getLogger().debug("Sloppy break detected at: " + pos);
        final BlockState toBlockState = level.getBlockState(pos);
        final Block toBlock = toBlockState.getBlock();

        if (toBlock instanceof BranchBlock) //if the toBlock is a branch it probably was probably replaced by the debug stick, therefore we do nothing
        {
            return;
        }

        if (toBlock == Blocks.AIR) { // Block was set to air improperly.
            level.setBlock(pos, state, 0); // Set the block back and attempt a proper breaking.
            this.sloppyBreak(level, pos, DestroyType.VOID);
            this.setBlockStateIgnored(level, pos, BlockStates.AIR, 2); // Set back to air in case the sloppy break failed to do so.
            return;
        }
        if (toBlock == Blocks.FIRE) { // Block has burned.
            level.setBlock(pos, state, 0); // Set the branch block back and attempt a proper breaking.
            this.sloppyBreak(level, pos, DestroyType.FIRE); // Applies fire effects to falling branches.
            //this.setBlockStateIgnored(level, pos, Blocks.FIRE.getDefaultState(), 2); // Disabled because the fire is too aggressive.
            this.setBlockStateIgnored(level, pos, BlockStates.AIR, 2); // Set back to air instead.
            return;
        }
        if (/*!toBlock.entity(toBlockState) && */level.getBlockEntity(pos) == null) { // Block seems to be a pure BlockState based block.
            level.setBlock(pos, state, 0); // Set the branch block back and attempt a proper breaking.
            this.sloppyBreak(level, pos, DestroyType.VOID);
            this.setBlockStateIgnored(level, pos, toBlockState, 2); // Set back to whatever block caused this problem.
            return;
        }

        // There's a tile entity block that snuck in.  Don't touch it!
        for (final Direction dir : Direction.values()) { // Let's just play it safe and destroy all surrounding branch block networks.
            final BlockPos offPos = pos.relative(dir);
            final BlockState offState = level.getBlockState(offPos);

            if (offState.getBlock() instanceof BranchBlock) {
                this.sloppyBreak(level, offPos, DestroyType.VOID);
            }
        }

        super.onRemove(state, level, pos, newState, flag);
    }

    /**
     * Provides a means to set a blockState over a branch block without triggering sloppy breaking.
     */
    public void setBlockStateIgnored(Level level, BlockPos pos, BlockState state, int flags) {
        destroyMode = DynamicTrees.DestroyMode.IGNORE; // Set the state machine to ignore so we don't accidentally recurse with breakBlock.
        level.setBlock(pos, state, flags);
        destroyMode = DynamicTrees.DestroyMode.SLOPPY; // Ready the state machine for sloppy breaking again.
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    }

    /**
     * Breaks the {@link BranchBlock} deliberately.
     *
     * @param level The {@link LevelAccessor} instance.
     * @param pos   The {@link BlockPos} of the {@link BranchBlock} to destroy.
     * @param mode  The {@link DynamicTrees.DestroyMode} to destroy it with.
     */
    public void breakDeliberate(LevelAccessor level, BlockPos pos, DynamicTrees.DestroyMode mode) {
        destroyMode = mode;
        level.removeBlock(pos, false);
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;
    }

    /**
     * Gets the {@link PushReaction} for this {@link Block}. By default, {@link BranchBlock}s use {@link
     * PushReaction#BLOCK} in order to prevent tree branches from being pushed by a piston. This is done for reasons
     * that should be obvious if you are paying any attention.
     *
     * @param state The {@link BlockState} of the {@link BranchBlock}.
     * @return {@link PushReaction#BLOCK} to prevent {@link BranchBlock}s being pushed.
     */
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    ///////////////////////////////////////////
    // EXPLOSIONS AND FIRE
    ///////////////////////////////////////////

    /**
     * Handles destroying the {@link BranchBlock} when it's exploded. This is likely to result in mostly sticks but that
     * kind of makes sense anyway.
     *
     * @param state     The {@link BlockState} of the {@link BranchBlock} being exploded.
     * @param level     The {@link Level} instance.
     * @param pos       The {@link BlockPos} of the {@link BranchBlock} being exploded.
     * @param explosion The {@link Explosion} destroying the {@link BranchBlock}.
     */
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        final Species species = TreeHelper.getExactSpecies(level, pos);
        final BranchDestructionData destroyData = destroyBranchFromNode(level, pos, Direction.DOWN, false, null);
        final NetVolumeNode.Volume woodVolume = destroyData.woodVolume;
        final List<ItemStack> woodDropList = species.getBranchesDrops(level, woodVolume, ItemStack.EMPTY, explosion.radius);
        final FallingTreeEntity treeEntity = FallingTreeEntity.dropTree(level, destroyData, woodDropList, DestroyType.BLAST);

        if (treeEntity != null) {
            final Vec3 expPos = explosion.getPosition();
            final double distance = Math.sqrt(treeEntity.distanceToSqr(expPos.x, expPos.y, expPos.z));

            if (distance / explosion.radius <= 1.0D && distance != 0.0D) {
                treeEntity.push((treeEntity.getX() - expPos.x) / distance, (treeEntity.getY() - expPos.y) / distance,
                        (treeEntity.getZ() - expPos.z) / distance);
            }
        }

        this.wasExploded(level, pos, explosion);
    }

    @Override
    public final TreePartType getTreePartType() {
        return TreePartType.BRANCH;
    }

}
