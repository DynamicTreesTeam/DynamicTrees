package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IFutureBreakable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity.DestroyType;
import com.ferreusveritas.dynamictrees.event.FutureBreak;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.nodemappers.DestroyerNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.SpeciesNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.StateNode;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.*;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public abstract class BranchBlock extends BlockWithDynamicHardness implements ITreePart, IFutureBreakable {

	public static final int MAX_RADIUS = 8;
	public static DynamicTrees.DestroyMode destroyMode = DynamicTrees.DestroyMode.SLOPPY;

	/** The {@link Family} for this {@link BranchBlock}. */
	private Family family = Family.NULL_FAMILY;
	private ItemStack[] primitiveLogDrops = new ItemStack[]{};
	private boolean canBeStripped;

	public BranchBlock(Material material) {
		this(Properties.of(material));
	}

	public BranchBlock(Properties properties) {
		super(properties); //removes drops from block
	}

	public BranchBlock setCanBeStripped(boolean truth){
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
	public Family getFamily(BlockState state, IBlockReader blockAccess, BlockPos pos) {
		return getFamily();
	}
	
	public boolean isSameTree(ITreePart treepart) {
		return isSameTree(TreeHelper.getBranch(treepart));
	}
	public boolean isSameTree(BlockState state) {
		return isSameTree(TreeHelper.getBranch(state));
	}
	
	/**
	 * Branches are considered the same if they have the same tree.
	 *
	 * @param branch The {@link BranchBlock} to compare with.
	 * @return {@code true} if this and the given {@link BranchBlock} are from the same
	 * 		   {@link Family}; {@code false} otherwise.
	 */
	public boolean isSameTree(@Nullable final BranchBlock branch) {
		return branch != null && this.getFamily() == branch.getFamily();
	}

	public boolean isStrippedBranch () {
		return this.getFamily().hasStrippedBranch() && this.getFamily().getStrippedBranch() == this;
	}

	@Override
	public abstract int branchSupport(BlockState blockState, IBlockReader blockAccess, BranchBlock branch, BlockPos pos, Direction dir, int radius);
	
	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////
	
	/**
	 *
	 * @param world The world
	 * @param pos The branch block position
	 * @param radius The radius of the branch that's the subject of rotting
	 * @param rand A random number generator for convenience
	 * @param rapid If true then unsupported branch postRot will occur regardless of chance value.
	 * 		This will also postRot the entire unsupported branch at once.
	 * 		True if this postRot is happening under a generation scenario as opposed to natural tree updates
	 * @return true if the branch was destroyed because of postRot
	 */
	public abstract boolean checkForRot(IWorld world, BlockPos pos, Species species, int radius, Random rand, float chance, boolean rapid);
	
	public static int setSupport(int branches, int leaves) {
		return ((branches & 0xf) << 4) | (leaves & 0xf);
	}
	
	public static int getBranchSupport(int support) {
		return (support >> 4) & 0xf;
	}
	
	public static int getLeavesSupport(int support) {
		return support & 0xf;
	}

	public static boolean isNextToBranch (World world, BlockPos pos, Direction originDir){
		for(Direction dir: Direction.values()) {
			if(!dir.equals(originDir)) {
				if(TreeHelper.isBranch(world.getBlockState(pos.relative(dir)))) {
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
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		final ItemStack heldItem = player.getItemInHand(hand);
		return TreeHelper.getTreePart(state).getFamily(state, world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, hit) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
	}

	public boolean canBeStripped(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem) {
		final int stripRadius = DTConfigs.MIN_RADIUS_FOR_STRIP.get();
		return stripRadius != 0 && stripRadius <= this.getRadius(state) && this.canBeStripped && this.isAxe(heldItem);
	}

	/**
	 * Strips the {@link BranchBlock}. This should only be called if {@link Family#hasStrippedBranch()}
	 * evaluates to {@code true}.
	 *
	 * @param state The {@link BlockState} for the {@link BranchBlock} to strip.
	 * @param world The {@link World} instance.
	 * @param pos The {@link BlockPos} for the {@link BranchBlock} to strip.
	 * @param player The {@link PlayerEntity} stripping the branch.
	 * @param heldItem The {@link ItemStack} the given {@code player} used to strip the branch.
	 * @throws AssertionError if the {@link Family} does not have a stripped branch.
	 */
	public void stripBranch (BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem) {
		final int radius = this.getRadius(state);
		this.damageAxe(player, heldItem, radius / 2, new NetVolumeNode.Volume((radius * radius * 64) / 2), false);

		this.stripBranch(state, world, pos, radius);
	}

	public void stripBranch (BlockState state, IWorld world, BlockPos pos) {
		this.stripBranch(state, world, pos, this.getRadius(state));
	}

	public void stripBranch (BlockState state, IWorld world, BlockPos pos, int radius) {
		assert this.getFamily().getStrippedBranch() != null;
		this.getFamily().getStrippedBranch().setRadius(world, pos, Math.max(1, radius - (DTConfigs.ENABLE_STRIP_RADIUS_REDUCTION.get() ? 1 : 0)), null);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(getFamily().getBranchItem());
	}

	/**
	 * Checks if the block is path-findable. This prevents mobs like bees from getting stuck
	 * in branches.
	 *
	 * @param state The {@link BlockState} for the {@link BranchBlock}.
	 * @param worldIn The {@link IBlockReader} instance.
	 * @param pos The {@link BlockPos} of the {@link BranchBlock}.
	 * @param type The {@link PathType} to check.
	 * @return {@code false} to prevent mobs from getting stuck in ranches.
	 */
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	public Connections getConnectionData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		final Connections connections = new Connections();

		if (state.getBlock() != this)
			return connections;

		final int coreRadius = this.getRadius(state);
		for (final Direction dir: Direction.values()) {
			final BlockPos deltaPos = pos.relative(dir);
			final BlockState neighborBlockState = world.getBlockState(deltaPos);
			final int sideRadius = TreeHelper.getTreePart(neighborBlockState).getRadiusForConnection(neighborBlockState, world, deltaPos, this, dir, coreRadius);
			connections.setRadius(dir, MathHelper.clamp(sideRadius, 0, coreRadius));
		}

		return connections;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	@Override
	public int getRadius(BlockState blockState) {
		return 1;
	}
	
	public abstract int setRadius(IWorld world, BlockPos pos, int radius, @Nullable Direction originDir, int flags);
	
	public int setRadius(IWorld world, BlockPos pos, int radius, @Nullable Direction originDir) {
		return setRadius(world, pos, radius, originDir, 2);
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
	public boolean shouldAnalyse(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
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
	 * @param world The {@link World} instance.
	 * @param cutPos The {@link BlockPos} of the branch being destroyed.
	 * @param toolDir The face that was pounded on when breaking the block at the given
	 * 				  {@code cutPos}.
	 * @param wholeTree {@code true} if the whole tree should be destroyed; otherwise {@code false}
	 *                  if only the branch should.
	 * @return The {@link BranchDestructionData} {@link Object} created.
	 */
	public BranchDestructionData destroyBranchFromNode(World world, BlockPos cutPos, Direction toolDir, boolean wholeTree, @Nullable final LivingEntity entity) {
		final BlockState blockState = world.getBlockState(cutPos);
		final SpeciesNode speciesNode = new SpeciesNode();
		final MapSignal signal = analyse(blockState, world, cutPos, null, new MapSignal(speciesNode)); // Analyze entire tree network to find root node and species.
		final Species species = speciesNode.getSpecies(); // Get the species from the root node.
		
		// Analyze only part of the tree beyond the break point and map out the extended block states.
		// We can't destroy the branches during this step since we need accurate extended block states that include connections.
		StateNode stateMapper = new StateNode(cutPos);
		this.analyse(blockState, world, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(stateMapper));
		
		// Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches.
		final NetVolumeNode volumeSum = new NetVolumeNode();
		final DestroyerNode destroyer = new DestroyerNode(species).setPlayer(entity instanceof PlayerEntity ? (PlayerEntity) entity : null);
		destroyMode = DynamicTrees.DestroyMode.HARVEST;
		this.analyse(blockState, world, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
		destroyMode = DynamicTrees.DestroyMode.SLOPPY;
		
		// Destroy all the leaves on the branch, store them in a map and convert endpoint coordinates from absolute to relative.
		List<BlockPos> endPoints = destroyer.getEnds();
		final Map<BlockPos, BlockState> destroyedLeaves = new HashMap<>();
		final List<ItemStackPos> leavesDropsList = new ArrayList<>();
		this.destroyLeaves(world, cutPos, species, endPoints, destroyedLeaves, leavesDropsList);
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
	 * @param world The {@link World} instance.
	 * @param pos The {@link BlockPos} of the block to rot.
	 */
	public void rot(IWorld world, BlockPos pos) {
		this.breakDeliberate(world, pos, DynamicTrees.DestroyMode.ROT);
	}

	/**
	 * Destroyed all leaves on the {@link BranchBlock} at the {@code cutPos} into the given {@code destroyedLeaves}
	 * {@link Map} that can be safely destroyed without harming surrounding leaves.
	 *
	 * <p>Drops are not handled by this method, but instead put into the given {@code drops} {@link List}.</p>
	 *
	 * @param world The {@link World} instance.
	 * @param cutPos The {@link BlockPos} of the {@link Block} that was initially destroyed.
	 * @param species The {@link Species} of the tree that being modified.
	 * @param endPoints A {@link List} of absolute {@link BlockPos} {@link Object}s of the branch endpoints.
	 * @param destroyedLeaves A {@link Map} for collecting the {@link BlockPos} and {@link BlockState}s for all
	 *                        of the {@link DynamicLeavesBlock} that are destroyed.
	 * @param drops A {@link List} for collecting the {@link ItemStack}s and their {@link BlockPos} relative to
	 *              the cut {@link BlockPos}.
	 */
	public void destroyLeaves(final World world, final BlockPos cutPos, final Species species, final List<BlockPos> endPoints, final Map<BlockPos, BlockState> destroyedLeaves, final List<ItemStackPos> drops) {
		if (world.isClientSide || endPoints.isEmpty())
			return;

		// Make a bounding volume that holds all of the endpoints and expand the volume for the leaves radius.
		final BlockBounds bounds = getFamily().expandLeavesBlockBounds(new BlockBounds(endPoints));

		// Create a voxmap to store the leaf destruction map.
		final SimpleVoxmap leafMap = new SimpleVoxmap(bounds);

		// For each of the endpoints add an expanded destruction volume around it.
		for (final BlockPos endPos : endPoints) {
			for (final BlockPos leafPos : getFamily().expandLeavesBlockBounds(new BlockBounds(endPos)) ) {
				leafMap.setVoxel(leafPos, (byte) 1); // Flag this position for destruction.
			}
			leafMap.setVoxel(endPos, (byte) 0); // We know that the endpoint does not have a leaves block in it because it was a branch.
		}

		final Family family = species.getFamily();
		final BranchBlock familyBranch = family.getBranch();
		final int primaryThickness = family.getPrimaryThickness();

		// Expand the volume yet again in all directions and search for other non-destroyed endpoints.
		for (final BlockPos findPos : getFamily().expandLeavesBlockBounds(bounds)) {
			final BlockState findState = world.getBlockState(findPos);
			if (familyBranch.getRadius(findState) == primaryThickness) { // Search for endpoints of the same tree family.
				final Iterable<BlockPos.Mutable> leaves = species.getLeavesProperties().getCellKit().getLeafCluster().getAllNonZero();
				for (BlockPos.Mutable leafPos : leaves) {
					leafMap.setVoxel(findPos.getX() + leafPos.getX(), findPos.getY() + leafPos.getY(), findPos.getZ() + leafPos.getZ(), (byte) 0);
				}
			}
		}

		final List<ItemStack> dropList = new ArrayList<>();

		// Destroy all family compatible leaves.
		for (final Cell cell: leafMap.getAllNonZeroCells()) {
			final BlockPos.Mutable pos = cell.getPos();
			final BlockState blockState = world.getBlockState(pos);
			if (family.isCompatibleGenericLeaves(species, blockState, world, pos) ) {
				dropList.clear();
				species.getTreeHarvestDrops(world, pos, dropList, world.random);
				final BlockPos imPos = pos.immutable(); // We are storing this so it must be immutable
				final BlockPos relPos = imPos.subtract(cutPos);
				world.setBlock(imPos, BlockStates.AIR, 3);
				destroyedLeaves.put(relPos, blockState);
				dropList.forEach(i -> drops.add(new ItemStackPos(i, relPos)) );
			}
		}
	}
	
	public boolean canFall() {
		return false;
	}
	
	///////////////////////////////////////////
	// DROPS AND HARVESTING
	///////////////////////////////////////////

	public List<ItemStack> getLogDrops(World world, BlockPos pos, Species species, NetVolumeNode.Volume volume) {
		return this.getLogDrops(world, pos, species, volume, ItemStack.EMPTY);
	}

	public List<ItemStack> getLogDrops(World world, BlockPos pos, Species species, NetVolumeNode.Volume volume, ItemStack handStack) {
		volume.multiplyVolume(DTConfigs.TREE_HARVEST_MULTIPLIER.get()); // For cheaters.. you know who you are.
		return species.getLogsDrops(world, pos, new ArrayList<>(), volume, handStack);
	}

	public float getPrimitiveLogs(float volumeIn, List<ItemStack> drops){
		int numLogs = (int)volumeIn;
		for (ItemStack stack : primitiveLogDrops) {
			int num = numLogs * stack.getCount();
			while(num > 0) {
				drops.add(new ItemStack(stack.getItem(), Math.min(num, 64)));
				num -= 64;
			}
		}
		return volumeIn - numLogs;
	}

	public void setPrimitiveLogDrops (ItemStack... drops){
		primitiveLogDrops = drops;
	}

	@Override
	public void futureBreak(BlockState state, World world, BlockPos cutPos, LivingEntity entity) {
		// Tries to get the face being pounded on.
		final double reachDistance = entity instanceof PlayerEntity ? entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5.0D;
		final BlockRayTraceResult ragTraceResult = this.playerRayTrace(entity, reachDistance, 1.0F);
		final Direction toolDir = ragTraceResult != null ? (entity.isShiftKeyDown() ? ragTraceResult.getDirection().getOpposite() : ragTraceResult.getDirection()) : Direction.DOWN;

		// Play and render block break sound and particles (must be done before block is broken).
		world.levelEvent(null, 2001, cutPos, getId(state));

		// Do the actual destruction.
		final BranchDestructionData destroyData = this.destroyBranchFromNode(world, cutPos, toolDir, false, entity);
		
		// Get all of the wood drops.
		final ItemStack heldItem = entity.getMainHandItem();
		final int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
		final float fortuneFactor = 1.0f + 0.25f * fortune;
		final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
		woodVolume.multiplyVolume(fortuneFactor);
		final List<ItemStack> woodItems = getLogDrops(world, cutPos, destroyData.species, woodVolume, heldItem);
		
		final float chance = 1.0f;

		// Build the final wood drop list taking chance into consideration.
		final List<ItemStack> woodDropList = woodItems.stream().filter(i -> world.random.nextFloat() <= chance).collect(Collectors.toList());
		
		// Drop the FallingTreeEntity into the world.
		FallingTreeEntity.dropTree(world, destroyData, woodDropList, DestroyType.HARVEST);

		// Damage the axe by a prescribed amount.
		this.damageAxe(entity, heldItem, this.getRadius(state), woodVolume, true);
	}

	/**
	 * We override the standard behavior because we need to preserve the tree network structure to calculate
	 * the wood volume for drops. {@link super#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean, FluidState)}
	 * will set this block to air before we get a chance to make a summation. Because we have done this we must
	 * re-implement the entire drop logic flow.
	 *
	 * @param state The {@link BlockState} removed.
	 * @param world The {@link World} instance.
	 * @param pos The {@link BlockPos} of the {@link Block} removed.
	 * @param player The {@link PlayerEntity} damaging the {@link Block}, or {@code null}.
	 * @param willHarvest {@code true} if {@link #playerDestroy(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)}
	 *                                will be called after this, if the return is {@code true}.
	 * @param fluid The current {@link FluidState} for the position in the {@link World}.
	 * @return {@code true} if {@link #destroy(IWorld, BlockPos, BlockState)} should be called; {@code false} otherwise.
	 */
	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		return this.removedByEntity(state, world, pos, player);
	}
	
	public boolean removedByEntity(BlockState state, World world, BlockPos cutPos, LivingEntity entity) {
		FutureBreak.add(new FutureBreak(state, world, cutPos, entity, 0));
		return false;
	}

	protected void sloppyBreak(World world, BlockPos cutPos, DestroyType destroyType) {
		// Do the actual destruction.
		final BranchDestructionData destroyData = this.destroyBranchFromNode(world, cutPos, Direction.DOWN, false, null);
		
		// Get all of the wood drops.
		final List<ItemStack> woodDropList = this.getLogDrops(world, cutPos, destroyData.species, destroyData.woodVolume);

		// If sloppy break drops are off clear all drops.
		if (!DTConfigs.SLOPPY_BREAK_DROPS.get()) {
			destroyData.leavesDrops.clear();
			woodDropList.clear();
		}
		
		// This will drop the EntityFallingTree into the world.
		FallingTreeEntity.dropTree(world, destroyData, woodDropList, destroyType);
	}
	
	/**
	 * This is a copy of Entity.rayTrace which is client side only. There's no
	 * reason for this function to be client-side only as all of it's calls are
	 * client/server compatible.
	 *
	 * @param entity The {@link LivingEntity} to ray trace from.
	 * @param blockReachDistance The {@code reachDistance} of the entity.
	 * @param partialTicks The partial ticks.
	 * @return The {@link BlockRayTraceResult} created.
	 */
	@Nullable
	public BlockRayTraceResult playerRayTrace(LivingEntity entity, double blockReachDistance, float partialTicks) {
		Vector3d vec3d = entity.getEyePosition(partialTicks);
		Vector3d vec3d1 = entity.getViewVector(partialTicks);
		Vector3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
		return entity.level.clip(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
	}
	
	
	public void damageAxe(final LivingEntity entity, @Nullable final ItemStack heldItem, final int radius, final NetVolumeNode.Volume woodVolume, final boolean forBlockBreak) {
		if (heldItem == null || !this.isAxe(heldItem))
			return;

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

		if (forBlockBreak)
			damage--; // Minecraft already damaged the tool by one unit

		if (damage > 0) {
			heldItem.hurtAndBreak(damage, entity, LivingEntity::tick);
		}
	}

	protected boolean isAxe (ItemStack stack) {
		return stack.getItem() instanceof AxeItem || stack.getItem().getToolTypes(stack).contains(ToolType.AXE);
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean flag) {
		if (world.isClientSide || destroyMode != DynamicTrees.DestroyMode.SLOPPY) {
			super.onRemove(state, world, pos, newState, flag);
			return;
		}

		// LogManager.getLogger().debug("Sloppy break detected at: " + pos);
		final BlockState toBlockState = world.getBlockState(pos);
		final Block toBlock = toBlockState.getBlock();

		if (toBlock instanceof BranchBlock) //if the toBlock is a branch it probably was probably replaced by the debug stick, therefore we do nothing
			return;

		if (toBlock == Blocks.AIR) { // Block was set to air improperly.
			world.setBlock(pos, state, 0); // Set the block back and attempt a proper breaking.
			this.sloppyBreak(world, pos, DestroyType.VOID);
			this.setBlockStateIgnored(world, pos, BlockStates.AIR, 2); // Set back to air in case the sloppy break failed to do so.
			return;
		}
		if (toBlock == Blocks.FIRE) { // Block has burned.
			world.setBlock(pos, state, 0); // Set the branch block back and attempt a proper breaking.
			this.sloppyBreak(world, pos, DestroyType.FIRE); // Applies fire effects to falling branches.
			//this.setBlockStateIgnored(world, pos, Blocks.FIRE.getDefaultState(), 2); // Disabled because the fire is too aggressive.
			this.setBlockStateIgnored(world, pos, BlockStates.AIR, 2); // Set back to air instead.
			return;
		}
		if (!toBlock.hasTileEntity(toBlockState) && world.getBlockEntity(pos) == null) { // Block seems to be a pure BlockState based block.
			world.setBlock(pos, state, 0); // Set the branch block back and attempt a proper breaking.
			this.sloppyBreak(world, pos, DestroyType.VOID);
			this.setBlockStateIgnored(world, pos, toBlockState, 2); // Set back to whatever block caused this problem.
			return;
		}

		// There's a tile entity block that snuck in.  Don't touch it!
		for (final Direction dir : Direction.values()) { // Let's just play it safe and destroy all surrounding branch block networks.
			final BlockPos offPos = pos.relative(dir);
			final BlockState offState = world.getBlockState(offPos);

			if (offState.getBlock() instanceof BranchBlock)
				this.sloppyBreak(world, offPos, DestroyType.VOID);
		}

		super.onRemove(state, world, pos, newState, flag);
	}

	/**
	 * Provides a means to set a blockState over a branch block without triggering sloppy breaking.
	 */
	public void setBlockStateIgnored(World world, BlockPos pos, BlockState state, int flags) {
		destroyMode = DynamicTrees.DestroyMode.IGNORE; // Set the state machine to ignore so we don't accidentally recurse with breakBlock.
		world.setBlock(pos, state, flags);
		destroyMode = DynamicTrees.DestroyMode.SLOPPY; // Ready the state machine for sloppy breaking again.
	}

	@Override
	public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
	}
	
	/**
	 * Breaks the {@link BranchBlock} deliberately.
	 *
	 * @param world The {@link IWorld} instance.
	 * @param pos The {@link BlockPos} of the {@link BranchBlock} to destroy.
	 * @param mode The {@link DynamicTrees.DestroyMode} to destroy it with.
	 */
	public void breakDeliberate(IWorld world, BlockPos pos, DynamicTrees.DestroyMode mode) {
		destroyMode = mode;
		world.removeBlock(pos, false);
		destroyMode = DynamicTrees.DestroyMode.SLOPPY;
	}

	/**
	 * Gets the {@link PushReaction} for this {@link Block}. By default, {@link BranchBlock}s
	 * use {@link PushReaction#BLOCK} in order to prevent tree branches from being pushed by
	 * a piston. This is done for reasons that should be obvious if you are paying any attention.
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
	 * Handles destroying the {@link BranchBlock} when it's exploded. This is likely to result
	 * in mostly sticks but that kind of makes sense anyway.
	 *
	 * @param state The {@link BlockState} of the {@link BranchBlock} being exploded.
	 * @param world The {@link World} instance.
	 * @param pos The {@link BlockPos} of the {@link BranchBlock} being exploded.
	 * @param explosion The {@link Explosion} destroying the {@link BranchBlock}.
	 */
	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		final Species species = TreeHelper.getExactSpecies(world, pos);
		final BranchDestructionData destroyData = destroyBranchFromNode(world, pos, Direction.DOWN, false, null);
		final NetVolumeNode.Volume woodVolume = destroyData.woodVolume;
		final List<ItemStack> woodDropList = getLogDrops(world, pos, species, woodVolume);
		final FallingTreeEntity treeEntity = FallingTreeEntity.dropTree(world, destroyData, woodDropList, DestroyType.BLAST);

		if (treeEntity != null) {
			final Vector3d expPos = explosion.getPosition();
			final double distance = Math.sqrt(treeEntity.distanceToSqr(expPos.x, expPos.y, expPos.z));

			if (distance / explosion.radius <= 1.0D && distance != 0.0D)
				treeEntity.push((treeEntity.getX() - expPos.x) / distance, (treeEntity.getY() - expPos.y) / distance,
						(treeEntity.getZ() - expPos.z) / distance);
		}

		this.wasExploded(world, pos, explosion);
	}

	@Override
	public final TreePartType getTreePartType() {
		return TreePartType.BRANCH;
	}

}
