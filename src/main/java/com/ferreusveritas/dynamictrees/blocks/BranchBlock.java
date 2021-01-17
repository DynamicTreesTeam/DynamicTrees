package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IFutureBreakable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree.DestroyType;
import com.ferreusveritas.dynamictrees.event.FutureBreak;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDestroyer;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeExtState;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeSpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.Connections;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;

public abstract class BranchBlock extends BlockWithDynamicHardness implements ITreePart, IFutureBreakable {

	public static final int RADMAX_NORMAL = 8;
	public static DynamicTrees.EnumDestroyMode destroyMode = DynamicTrees.EnumDestroyMode.SLOPPY;
	
	private TreeFamily tree = TreeFamily.NULLFAMILY; //The tree this branch type creates
	
	public BranchBlock(Properties properties, String name){
		super(properties.noDrops().harvestTool(ToolType.AXE).harvestLevel(0)); //removes drops from block
		setRegistryName(name);
	}
	
	public BranchBlock(Material material, String name) {
		this(Properties.create(material), name);
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	public void setFamily(TreeFamily tree) {
		this.tree = tree;
	}
	
	public TreeFamily getFamily() {
		return tree;
	}
	
	@Override
	public TreeFamily getFamily(BlockState state, IBlockReader blockAccess, BlockPos pos) {
		return getFamily();
	}
	
	public boolean isSameTree(ITreePart treepart) {
		return isSameTree(TreeHelper.getBranch(treepart));
	}
	
	/**
	 * Branches are considered the same if they have the same tree
	 *
	 * @param branch
	 * @return
	 */
	public boolean isSameTree(BranchBlock branch) {
		return branch != null && getFamily() == branch.getFamily();
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
	 * @param rapid If true then unsupported branch rot will occur regardless of chance value.
	 * 		This will also rot the entire unsupported branch at once.
	 * 		True if this rot is happening under a generation scenario as opposed to natural tree updates
	 * @return true if the branch was destroyed because of rot
	 */
	public abstract boolean checkForRot(World world, BlockPos pos, Species species, int radius, Random rand, float chance, boolean rapid);
	
	public static int setSupport(int branches, int leaves) {
		return ((branches & 0xf) << 4) | (leaves & 0xf);
	}
	
	public static int getBranchSupport(int support) {
		return (support >> 4) & 0xf;
	}
	
	public static int getLeavesSupport(int support) {
		return support & 0xf;
	}
	
	//
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Deprecated
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		//		if (getRadius(state) > 8){
		//			setRadius(world, pos, getRadius(state), Direction.DOWN);
		//		}
		net.minecraft.item.ItemStack heldItem = player.getHeldItem(hand);
		return TreeHelper.getTreePart(state).getFamily(state, world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, hit) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	public Connections getConnectionData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		
		Connections connections = new Connections();
		
		if(state.getBlock().equals(this)) {
			int coreRadius = getRadius(state);
			for(Direction dir: Direction.values()) {
				BlockPos deltaPos = pos.offset(dir);
				BlockState neighborBlockState = world.getBlockState(deltaPos);
				int sideRadius = TreeHelper.getTreePart(neighborBlockState).getRadiusForConnection(neighborBlockState, world, deltaPos, this, dir, coreRadius);
				connections.setRadius(dir, MathHelper.clamp(sideRadius, 0, coreRadius));
			}
		}
		
		return connections;
	}
	
//	@Override
//	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
//		return false;
//	}
	
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	@Override
	public int getRadius(BlockState blockState) {
		return 1;
	}
	
	public abstract int setRadius(World world, BlockPos pos, int radius, Direction originDir, int flags);
	
	public int setRadius(World world, BlockPos pos, int radius, Direction originDir) {
		return setRadius(world, pos, radius, originDir, 2);
	}
	
	public abstract BlockState getStateForRadius(int radius);
	
	public int getMaxRadius() {
		return RADMAX_NORMAL;
	}
	
	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////
	
	/**
	 * Generally all branch blocks should be analyzed.
	 */
	@Override
	public boolean shouldAnalyse() {
		return true;
	}
	
	public class ItemStackPos {
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
	 * @param world The world
	 * @param cutPos The position of the branch being lobbed
	 * @param toolDir The face that was pounded on when breaking the block at cutPos
	 * @param wholeTree Indicates if the whole tree should be destroyed or just the branch
	 * @return The volume of the portion of the tree that was destroyed
	 */
	public BranchDestructionData destroyBranchFromNode(World world, BlockPos cutPos, Direction toolDir, boolean wholeTree) {
		
		BlockState blockState = world.getBlockState(cutPos);
		NodeSpecies nodeSpecies = new NodeSpecies();
		MapSignal signal = analyse(blockState, world, cutPos, null, new MapSignal(nodeSpecies));// Analyze entire tree network to find root node and species
		Species species = nodeSpecies.getSpecies();//Get the species from the root node
		
		// Analyze only part of the tree beyond the break point and map out the extended block states
		// We can't destroy the branches during this step since we need accurate extended block states that include connections
		NodeExtState extStateMapper = new NodeExtState(cutPos);
		analyse(blockState, world, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(extStateMapper));
		
		// Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches
		NodeNetVolume volumeSum = new NodeNetVolume();
		NodeDestroyer destroyer = new NodeDestroyer(species);
		destroyMode = DynamicTrees.EnumDestroyMode.HARVEST;
		analyse(blockState, world, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
		destroyMode = DynamicTrees.EnumDestroyMode.SLOPPY;
		
		//Destroy all the leaves on the branch, store them in a map and convert endpoint coordinates from absolute to relative
		List<BlockPos> endPoints = destroyer.getEnds();
		Map<BlockPos, BlockState> destroyedLeaves = new HashMap<>();
		List<ItemStackPos> leavesDropsList = new ArrayList<>();
		destroyLeaves(world, cutPos, species, endPoints, destroyedLeaves, leavesDropsList);
		endPoints = endPoints.stream().map(p -> p.subtract(cutPos)).collect(Collectors.toList());
		
		//Calculate main trunk height
		int trunkHeight = 1;
		for(BlockPos iter = new BlockPos(0, 1, 0); extStateMapper.getBranchConnectionMap().containsKey(iter); iter = iter.up()) {
			trunkHeight++;
		}
		
		Direction cutDir = signal.localRootDir;
		if(cutDir == null) {
			cutDir = Direction.DOWN;
		}
		
		return new BranchDestructionData(species, extStateMapper.getBranchConnectionMap(), destroyedLeaves, leavesDropsList, endPoints, volumeSum.getVolume(), cutPos, cutDir, toolDir, trunkHeight);
	}
	
	/**
	 * Sets the branch block to air. To be used when the block rots
	 *
	 * @param world
	 * @param pos
	 */
	public void rot(World world, BlockPos pos) {
		breakDeliberate(world, pos, DynamicTrees.EnumDestroyMode.ROT);
	}
	
	/**
	 * Attempt to destroy all of the leaves on the branch while leaving the other leaves unharmed.
	 *
	 * @param world The world
	 * @param cutPos The position of the block that was initially destroyed
	 * @param species The species of the tree that is being modified
	 * @param endPoints The absolute positions of the branch endpoints
	 * @param destroyedLeaves A map for collecting the positions and blockstates for all of the leaves blocks that will be destroyed.
	 * @param drops A list for collecting the ItemStacks and their positions relative to the cut position
	 */
	protected void destroyLeaves(World world, BlockPos cutPos, Species species, List<BlockPos> endPoints, Map<BlockPos, BlockState> destroyedLeaves, List<ItemStackPos> drops) {
		
		if (!world.isRemote && !endPoints.isEmpty()) {
			
			//Make a bounding volume that holds all of the endpoints and expand the volume by 3 blocks for the leaves radius
			BlockBounds bounds = new BlockBounds(endPoints).expand(3);
			
			//Create a voxmap to store the leaf destruction map
			SimpleVoxmap vmap = new SimpleVoxmap(bounds);
			
			//For each of the endpoints add a 7x7 destruction volume around it
			for(BlockPos endPos : endPoints) {
				for(BlockPos leafPos : BlockPos.getAllInBoxMutable(endPos.add(-3, -3, -3), endPos.add(3, 3, 3)) ) {
					vmap.setVoxel(leafPos, (byte) 1);//Flag this position for destruction
				}
				vmap.setVoxel(endPos, (byte) 0);//We know that the endpoint does not have a leaves block in it because it was a branch
			}
			
			TreeFamily family = species.getFamily();
			BranchBlock familyBranch = family.getDynamicBranch();
			int primaryThickness = (int) family.getPrimaryThickness();
			
			//Expand the volume yet again by 3 blocks in all directions and search for other non-destroyed endpoints
			for(BlockPos findPos : bounds.expand(3).iterate() ) {
				BlockState findState = world.getBlockState(findPos);
				if( familyBranch.getRadius(findState) == primaryThickness ) { //Search for endpoints of the same tree family
					Iterable<BlockPos.Mutable> leaves = species.getLeavesProperties().getCellKit().getLeafCluster().getAllNonZero();
					for(BlockPos.Mutable leafpos : leaves) {
						vmap.setVoxel(findPos.getX() + leafpos.getX(), findPos.getY() + leafpos.getY(), findPos.getZ() + leafpos.getZ(), (byte) 0);
					}
				}
			}
			
			List<ItemStack> dropList = new ArrayList<>();
			
			//Destroy all family compatible leaves
			for(Cell cell: vmap.getAllNonZeroCells()) {
				BlockPos.Mutable pos = cell.getPos();
				BlockState blockState = world.getBlockState(pos);
				if( family.isCompatibleGenericLeaves(blockState, world, pos) ) {
					dropList.clear();
					species.getTreeHarvestDrops(world, pos, dropList, world.rand);
					BlockPos imPos = pos.toImmutable();//We are storing this so it must be immutable
					BlockPos relPos = imPos.subtract(cutPos);
					world.setBlockState(imPos, DTRegistries.blockStates.air, 3);
					destroyedLeaves.put(relPos, blockState);
					dropList.forEach(i -> drops.add(new ItemStackPos(i, relPos)) );
				}
			}
		}
		
	}
	
	public boolean canFall() {
		return false;
	}
	
	///////////////////////////////////////////
	// DROPS AND HARVESTING
	///////////////////////////////////////////
	
	public List<net.minecraft.item.ItemStack> getLogDrops(World world, BlockPos pos, Species species, float volume) {
		List<net.minecraft.item.ItemStack> ret = new ArrayList<net.minecraft.item.ItemStack>();//A list for storing all the dead tree guts
		volume *= DTConfigs.treeHarvestMultiplier.get();// For cheaters.. you know who you are.
		return species.getLogsDrops(world, pos, ret, volume);
	}
	
	//	/*
	//	1.10.2 Simplified Block Harvesting Logic Flow(for no silk touch)
	//
	//	tryHarvestBlock {
	//		canHarvest = canHarvestBlock() <- (ForgeHooks.canHarvestBlock occurs in here)
	//		removed = removeBlock(canHarvest) {
	//			removedByPlayer() {
	//				onBlockHarvested()
	//				world.setBlockState() <- block is set to air here
	//			}
	//		}
	//
	//		if (removed) harvestBlock() {
	//			fortune = getEnchantmentLevel(FORTUNE)
	//			dropBlockAsItem(fortune) {
	//				dropBlockAsItemWithChance(fortune) {
	//					items = getDrops(fortune) {
	//						getItemDropped(fortune) {
	//							Item.getItemFromBlock(this) <- (Standard block behavior)
	//						}
	//					}
	//					ForgeEventFactory.fireBlockHarvesting(items) <- (BlockEvent.HarvestDropsEvent)
	//					(for all items) -> spawnAsEntity(item)
	//				}
	//			}
	//		}
	//	}
	//	*/
	
	@Override
	public void futureBreak(BlockState state, World world, BlockPos cutPos, LivingEntity entity) {
		
		//Try to get the face being pounded on
		// TODO: Check method of getting player reach distance (ForgeMod.REACH_DISTANCE.get()?)
		final double reachDistance = entity instanceof PlayerEntity ? entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5.0D;
		BlockRayTraceResult rtResult = playerRayTrace(entity, reachDistance, 1.0F);
		Direction toolDir = rtResult != null ? (entity.isSneaking() ? rtResult.getFace().getOpposite() : rtResult.getFace()) : Direction.DOWN;
		
		//Do the actual destruction
		BranchDestructionData destroyData = destroyBranchFromNode(world, cutPos, toolDir, false);
		
		//Get all of the wood drops
		ItemStack heldItem = entity.getHeldItemMainhand();
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, heldItem);
		float fortuneFactor = 1.0f + 0.25f * fortune;
		float woodVolume = destroyData.woodVolume;// The amount of wood calculated from the body of the tree network
		List<ItemStack> woodItems = getLogDrops(world, cutPos, destroyData.species, woodVolume * fortuneFactor);
		
		if(entity.getActiveHand() == null) {//What the hell man? I trusted you!
			entity.setActiveHand(Hand.MAIN_HAND);//Players do things with hands.
		}
		
		float chance = 1.0f;
		//Fire the block harvesting event.  For An-Sar's PrimalCore mod :)
		if (entity instanceof PlayerEntity)
		{
			NonNullList<ItemStack> woodItemsNonNull = NonNullList.create();
			woodItemsNonNull.addAll(woodItems);
			// TODO: No block harvest event any more
//			chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(woodItemsNonNull, world, cutPos, state, fortune, chance, false, (PlayerEntity) entity);
		}
		final float finalChance = chance;
		
		//Build the final wood drop list taking chance into consideration
		List<ItemStack> woodDropList = woodItems.stream().filter(i -> world.rand.nextFloat() <= finalChance).collect(Collectors.toList());
		
		//This will drop the EntityFallingTree into the world
		EntityFallingTree.dropTree(world, destroyData, woodDropList, DestroyType.HARVEST);
		
		//Damage the axe by a prescribed amount
		damageAxe(entity, heldItem, getRadius(state), woodVolume);
	}
	
	// We override the standard behavior because we need to preserve the tree network structure to calculate
	// the wood volume for drops.  The standard removedByPlayer() call will set this block to air before we get
	// a chance to make a summation.  Because we have done this we must re-implement the entire drop logic flow.
	
	
	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		return removedByEntity(state, world, pos, player);
	}
	
	public boolean removedByEntity(BlockState state, World world, BlockPos cutPos, LivingEntity entity) {
		FutureBreak.add(new FutureBreak(state, world, cutPos, entity, 0));
		return false;
	}
	
	protected void sloppyBreak(World world, BlockPos cutPos, DestroyType destroyType) {
		//Do the actual destruction
		BranchDestructionData destroyData = destroyBranchFromNode(world, cutPos, Direction.DOWN, false);
		
		//Get all of the wood drops
		List<ItemStack> woodDropList = getLogDrops(world, cutPos, destroyData.species, destroyData.woodVolume);
		
		if(!DTConfigs.sloppyBreakDrops.get()) {
			destroyData.leavesDrops.clear();
			woodDropList.clear();
		}
		
		//This will drop the EntityFallingTree into the world
		EntityFallingTree.dropTree(world, destroyData, woodDropList, destroyType);
	}
	
	/**
	 * This is a copy of Entity.rayTrace which is client side only.  There's no
	 * reason for this function to be client side only as all of it's calls are
	 * client/server compatible.
	 *
	 * @param entity
	 * @param blockReachDistance
	 * @param partialTicks
	 * @return
	 */
	@Nullable
	public BlockRayTraceResult playerRayTrace(LivingEntity entity, double blockReachDistance, float partialTicks) {
		Vector3d vec3d = entity.getEyePosition(partialTicks);
		Vector3d vec3d1 = entity.getLook(partialTicks);
		Vector3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
		return entity.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
	}
	
	
	public void damageAxe(LivingEntity entity, net.minecraft.item.ItemStack heldItem, int radius, float woodVolume) {
		
		if(heldItem != null && (heldItem.getItem() instanceof AxeItem || heldItem.getItem().getToolTypes(heldItem).contains(ToolType.AXE))) {
			
			int damage;
			
			switch(DTConfigs.axeDamageMode.get()) {
				default:
				case VANILLA:
					damage = 1;
					break;
				case THICKNESS:
					damage = Math.max(1, radius) / 2;
					break;
				case VOLUME:
					damage = (int) woodVolume;
					break;
			}
			
			damage--;//Minecraft already damaged the tool by one unit
			if(damage > 0) {
				heldItem.damageItem(damage, entity, LivingEntity::tick);
			}
		}
		
	}
	
	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
		if(!world.isRemote && destroyMode == DynamicTrees.EnumDestroyMode.SLOPPY) {
			//System.out.println("Sloppy break detected at: " + pos);
			BlockState toBlockState = world.getBlockState(pos);
			Block toBlock = toBlockState.getBlock();
			if(toBlock == Blocks.AIR) {
				world.setBlockState(pos, state, 0);//Set the block back and attempt a proper breaking
				sloppyBreak(world, pos, DestroyType.VOID);
			} else
				if(toBlock == Blocks.FIRE) { //Block has burned
					world.setBlockState(pos, state, 0);//Set the block back and attempt a proper breaking
					sloppyBreak(world, pos, DestroyType.FIRE);
					//world.setBlockState(pos, Blocks.FIRE.getDefaultState());  <-- FIXME: Causes overflow
				} else
					if(toBlock == Blocks.STONE) { //Likely destroyed by the Pyroclasm mod's volcanic lava
						world.setBlockState(pos, state, 0);//Set the block back and attempt a proper breaking
						sloppyBreak(world, pos, DestroyType.VOID);
						world.setBlockState(pos, toBlockState);//Set back to stone
					} else {
						if(DTConfigs.worldGenDebug.get()) {
							System.err.println("Warning: Sloppy break with unusual block: " + toBlockState);
						}
					}
		}
	}
	
	// Super member also does nothing
	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
	}
	
	/**
	 * Sometimes you need to break the block deliberately.
	 *
	 * @param world
	 * @param pos
	 */
	public void breakDeliberate(World world, BlockPos pos, DynamicTrees.EnumDestroyMode mode) {
		destroyMode = mode;
		world.removeBlock(pos, false);
		destroyMode = DynamicTrees.EnumDestroyMode.SLOPPY;
	}
	
	// We do not allow the tree branches to be pushed by a piston for reasons that should be obvious if you
	// are paying attention.
	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}
	
	
	///////////////////////////////////////////
	// EXPLOSIONS AND FIRE
	///////////////////////////////////////////
	
	// Explosive harvesting methods will likely result in mostly sticks but I'm okay with that since it kinda makes sense.
	@Override
	public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() == this) {
			Species species = TreeHelper.getExactSpecies(world, pos);
			BranchDestructionData destroyData = destroyBranchFromNode(world, pos, Direction.DOWN, false);
			float woodVolume = destroyData.woodVolume;
			List<ItemStack> woodDropList = getLogDrops(world, pos, species, woodVolume);
			EntityFallingTree treeEntity = EntityFallingTree.dropTree(world, destroyData, woodDropList, DestroyType.BLAST);
			
			if(treeEntity != null) {
				Vector3d expPos = explosion.getPosition();
				LivingEntity placer = explosion.getExplosivePlacedBy();
				//Since the size of an explosion is private we have to make some assumptions.. TNT: 4, Creeper: 3, Creeper+: 6
				float size = (placer instanceof CreeperEntity) ? (((CreeperEntity)placer).isCharged() ? 6 : 3) : 4;
				double distance = Math.sqrt(treeEntity.getDistanceSq(expPos.x, expPos.y, expPos.z));
				if (distance / size <= 1.0D && distance != 0.0D) {
					treeEntity.addVelocity((treeEntity.getPosX() - expPos.x) / distance, (treeEntity.getPosY() - expPos.y) / distance, (treeEntity.getPosZ() - expPos.z) / distance);
				}
			}
		}
	}
	
	@Override
	public final TreePartType getTreePartType() {
		return TreePartType.BRANCH;
	}

}
