package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.IBurningListener;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDestroyer;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeExtState;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeSpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class BlockBranch extends Block implements ITreePart, IBurningListener {
	
	private TreeFamily tree; //The tree this branch type creates
	
	public BlockBranch(Material material, String name) {
		super(material); //Trees are made of wood. Brilliant.
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	public IProperty<?>[] getIgnorableProperties() {
		return new IProperty<?>[]{};
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
	public TreeFamily getFamily(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
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
	public boolean isSameTree(BlockBranch branch) {
		return branch != null && getFamily() == branch.getFamily();
	}
	
	@Override
	public abstract int branchSupport(IBlockState blockState, IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius);
	
	@Override
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return getFamily().isWood();
	}
	
	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////
	
	/**
	 * 
	 * @param world
	 * @param pos
	 * @param radius
	 * @param rand 
	 * @param rapid if true then unsupported branch rot will occur regardless of chance value.  will also rot the entire unsupported branch at once
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
	

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		return TreeHelper.getTreePart(state).getFamily(state, world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);
	}
	
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,	EnumFacing side) {
		return true;
	}
	
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	//TODO: This will eventually be abstract once add-ons have caught up.
	@Override
	public int getRadius(IBlockState blockState) {
		return 1;
	}
	
	/**
	 * Do not use this in new code.  Migrate existing calls to getRadius(IBlockState) whenever
	 * possible.  This will be dropped from the API at my discretion.
	 * 
	 * @param blockState
	 * @param blockAccess
	 * @param pos
	 * @return
	 */
	@Deprecated
	public int getRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		if(apiBother < 100000 && (apiBother++ % 500) == 0) { System.err.println("Please have mod author update API calls for getRadius()"); }
		return getRadius(blockState);
	}
	
	static int apiBother = 0;
	
	public abstract void setRadius(World world, BlockPos pos, int radius, EnumFacing dir, int flags);
	
	public void setRadius(World world, BlockPos pos, int radius, EnumFacing dir) {
		setRadius(world, pos, radius, dir, 2);
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
	
	/**
	 * Destroys all branches recursively not facing the branching direction with the root node
	 * 
	 * @param world The world
	 * @param pos The position of the branch being lobbed
	 * @param wholeTree Indicates if the whole tree should be destroyed or just the branch
	 * @return The volume of the portion of the tree that was destroyed
	 */
	public int destroyBranchFromNode(World world, BlockPos pos, boolean wholeTree) {
		IBlockState blockState = world.getBlockState(pos);
		NodeSpecies nodeSpecies = new NodeSpecies();
		MapSignal signal = analyse(blockState, world, pos, null, new MapSignal(nodeSpecies));// Analyze entire tree network to find root node and species
		Species species = nodeSpecies.getSpecies();//Get the species from the root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		NodeDestroyer destroyer = new NodeDestroyer(species);
		// Analyze only part of the tree beyond the break point and calculate it's volume
		analyse(blockState, world, pos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
		destroyLeaves(world, species, destroyer.getEnds());//Destroy all the leaves on the branch
		return volumeSum.getVolume();// Drop an amount of wood calculated from the body of the tree network
	}
	
	/**
	 * This will get a map of all of the ExtendedBlockStates for use with the falling tree renderer.
	 * 
	 * @param world
	 * @param pos
	 * @param wholeTree
	 * @return
	 */
	public Map<BlockPos, IExtendedBlockState> getExtStateMapFromNode(World world, BlockPos pos, boolean wholeTree) {
		IBlockState blockState = world.getBlockState(pos);
		MapSignal signal = analyse(blockState, world, pos, null, new MapSignal());// Analyze entire tree network to find root node and species
		NodeExtState extStateMapper = new NodeExtState();
		// Analyze only part of the tree beyond the break point
		BlockPos savePos = pos.offset(signal.localRootDir);
		IBlockState saveState = world.getBlockState(savePos);
		world.setBlockState(savePos, Blocks.AIR.getDefaultState(), 4);
		analyse(blockState, world, pos, wholeTree ? null : signal.localRootDir, new MapSignal(extStateMapper));
		world.setBlockState(savePos, saveState, 4);
		return extStateMapper.getExtStateMap();
	}
	
	/*
	 * Attempt to destroy all of the leaves on the branch while leaving the other
	 * leaves unharmed.
	 */
	protected void destroyLeaves(World world, Species species, List<BlockPos> endPoints) {
		
		if (!world.isRemote && !world.restoringBlockSnapshots && !endPoints.isEmpty()) { // do not drop items while restoring blockstates, prevents item dupe
			
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
			BlockBranch familyBranch = family.getDynamicBranch();
			int primaryThickness = (int) family.getPrimaryThickness();
			
			//Expand the volume yet again by 3 blocks in all directions and search for other non-destroyed endpoints
			for(MutableBlockPos findPos : bounds.expand(3).iterate() ) {
				IBlockState findState = world.getBlockState(findPos);
				if( familyBranch.getRadius(findState) == primaryThickness ) { //Search for endpoints of the same tree family
					Iterable<MutableBlockPos> leaves = species.getLeavesProperties().getCellKit().getLeafCluster().getAllNonZero();
					for(MutableBlockPos leafpos : leaves) {
						vmap.setVoxel(findPos.getX() + leafpos.getX(), findPos.getY() + leafpos.getY(), findPos.getZ() + leafpos.getZ(), (byte) 0);
					}
				}
			}
			
			ArrayList<ItemStack> dropList = new ArrayList<ItemStack>();
			
			//Destroy all family compatible leaves
			for(Cell cell: vmap.getAllNonZeroCells()) {
				MutableBlockPos pos = cell.getPos();
				if( family.isCompatibleGenericLeaves(world.getBlockState(pos), world, pos) ) {
					dropList.clear();
					species.getTreeHarvestDrops(world, pos, dropList, world.rand);
					world.destroyBlock(pos.toImmutable(), false);
					for(ItemStack stack : dropList) {
						EntityItem itemEntity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
						world.spawnEntity(itemEntity);
					}
				}
			}
		}
		
	}
	
	
	///////////////////////////////////////////
	// DROPS AND HARVESTING
	///////////////////////////////////////////
	
	public List<ItemStack> getLogDrops(World world, BlockPos pos, Species species, int volume) {
		List<ItemStack> ret = new java.util.ArrayList<ItemStack>();//A list for storing all the dead tree guts
		volume *= ModConfigs.treeHarvestMultiplier;// For cheaters.. you know who you are.
		return species.getLogsDrops(world, pos, ret, volume);
	}
	
	/*
	1.10.2 Simplified Block Harvesting Logic Flow(for no silk touch)
	
	tryHarvestBlock {
		canHarvest = canHarvestBlock() <- (ForgeHooks.canHarvestBlock occurs in here)
		removed = removeBlock(canHarvest) {
			removedByPlayer() {
				onBlockHarvested()
				world.setBlockState() <- block is set to air here
			}
		}
		
		if (removed) harvestBlock() {
			fortune = getEnchantmentLevel(FORTUNE)
			dropBlockAsItem(fortune) {
				dropBlockAsItemWithChance(fortune) {
					items = getDrops(fortune) {
						getItemDropped(fortune) {
							Item.getItemFromBlock(this) <- (Standard block behavior)
						}
					}
					ForgeEventFactory.fireBlockHarvesting(items) <- (BlockEvent.HarvestDropsEvent)
					(for all items) -> spawnAsEntity(item)
				}
			}
		}
	}
	*/
	
	// We override the standard behaviour because we need to preserve the tree network structure to calculate
	// the wood volume for drops.  The standard removedByPlayer() call will set this block to air before we get
	// a chance to make a summation.  Because we have done this we must re-implement the entire drop logic flow.
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean canHarvest) {
		ItemStack heldItem = player.getHeldItemMainhand();
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, heldItem);
		float fortuneFactor = 1.0f + 0.25f * fortune;
		int radius = getRadius(state);
		Species species = TreeHelper.getExactSpecies(state, world, pos);
		Map<BlockPos, IExtendedBlockState> extStateMap = ModConfigs.enableFallingTrees ? getExtStateMapFromNode(world, pos, false) : null;
		int woodVolume = destroyBranchFromNode(world, pos, false);
		List<ItemStack> items = getLogDrops(world, pos, species, (int)(woodVolume * fortuneFactor));
		
		//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		IBlockState belowState = world.getBlockState(pos.down());
		if(TreeHelper.isRooty(belowState)) {
			belowState.getBlock().updateTick(world, pos.down(), state, world.rand);
		}
		
		//Damage the axe by a prescribed amount
		damageAxe(player, heldItem, radius, woodVolume);
		
		//For An-Sar's PrimalCore mod :)
		float chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, fortune, 1.0f, false, player);
		
		//Build the final drop list taking chance into consideration
		Stream<ItemStack> dropList = items.stream().filter(i -> world.rand.nextFloat() <= chance);
		
		if(ModConfigs.enableFallingTrees) {
			EntityFallingTree fallingTree = new EntityFallingTree(world);
			fallingTree.setData(species, pos, dropList.collect(Collectors.toList()), extStateMap);
			world.spawnEntity(fallingTree);
		} else {
			dropList.forEach(i -> spawnAsEntity(world, pos, i));
		}
		
		return true;// Function returns true if Block was destroyed
	}
	
	public enum EnumAxeDamage {
		VANILLA,
		THICKNESS,
		VOLUME
	}
	
	public void damageAxe(EntityPlayer player, ItemStack heldItem, int radius, int woodVolume) {
		
		if(heldItem.getItem() instanceof ItemAxe) {
			
			int damage;
			
			switch(ModConfigs.axeDamageMode) {
				default:
				case VANILLA:
					damage = 1;
					break;
				case THICKNESS:
					damage = Math.max(1, radius) / 2;
					break;
				case VOLUME:
					damage = woodVolume / 4096;//There are 4096 "texel sized voxels" of wood in a log
					break;
			}
			
			damage--;//Minecraft already damaged the tool by one unit
			if(damage > 0) {
				heldItem.damageItem(damage, player);
			}
		}
		
	}
	
	// Super member also does nothing
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
	}
	
	// Since we already created drops in removedByPlayer() we must disable this.
	// Also we should definitely not return BlockBranch itemBlocks and here's why:
	//	* Players can use these blocks to make branch network loops that will grow artificially large in a short time.
	//	* Players can create invalid networks with more than one root node.
	//  * Players can exploit fortune enchanted tools by building a tree with parts and cutting it down for more wood.
	//	* Players can attach the wrong kind of branch to a tree leading to undefined behavior.
	// If a player in creative wants to do these things then that's their prerogative. 
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	// Similar to above.. We already created drops in removedByPlayer() so no quantity should be expressed
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	// We do not allow silk harvest for all the reasons listed in getItemDropped
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}
	
	// We do not allow the tree branches to be pushed by a piston for reasons that should be obvious if you
	// are paying attention.
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	///////////////////////////////////////////
	// EXPLOSIONS AND FIRE
	///////////////////////////////////////////
	
	// Explosive harvesting methods will likely result in mostly sticks but i'm okay with that since it kinda makes sense.
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() == this) {
			Species species = TreeHelper.getExactSpecies(state, world, pos);
			int woodVolume = destroyBranchFromNode(world, pos, false);
			for (ItemStack item : getLogDrops(world, pos, species, woodVolume)) {
				spawnAsEntity(world, pos, item);
			}
		}
	}
	
	@Override
	public void onBurned(World world, IBlockState oldState, BlockPos burnedPos) {		
		//possible supporting branch was destroyed by fire.
		if(oldState.getBlock() == this) {
			for(EnumFacing dir: EnumFacing.VALUES) {
				BlockPos neighPos = burnedPos.offset(dir);
				IBlockState neighState = world.getBlockState(neighPos);
				if(TreeHelper.isBranch(neighState)) {
					BlockPos rootPos = TreeHelper.findRootNode(neighState, world, neighPos);
					if(rootPos == BlockPos.ORIGIN) {
						analyse(neighState, world, neighPos, null, new MapSignal(new NodeDestroyer(getFamily().getCommonSpecies())));
					}
				}
			}
		}
		
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos neighbor) {		
		IBlockState neighBlockState = world.getBlockState(neighbor);
		
		if(neighBlockState.getMaterial() == Material.FIRE && neighBlockState.getBlock() != ModBlocks.blockVerboseFire) {
			int age = neighBlockState.getBlock() == Blocks.FIRE ? neighBlockState.getValue(BlockFire.AGE).intValue() : 0;
			world.setBlockState(neighbor, ModBlocks.blockVerboseFire.getDefaultState().withProperty(BlockFire.AGE, age));
		}
		
	}
	
	@Override
	public final TreePartType getTreePartType() {
		return TreePartType.BRANCH;
	}
	
}
