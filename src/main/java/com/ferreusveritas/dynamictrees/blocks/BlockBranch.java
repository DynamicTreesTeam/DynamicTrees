package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.IBurningListener;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDestroyer;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeSpecies;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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
	
	public void setTree(TreeFamily tree) {
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
	
	@Override
	public abstract int getRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos);
	
	public abstract void setRadius(World world, BlockPos pos, int radius, EnumFacing dir, int flags);
	
	public void setRadius(World world, BlockPos pos, int radius, EnumFacing dir) {
		setRadius(world, pos, radius, dir, 2);
	}
	
	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////
	
	/**
	 * Destroys all branches recursively not facing the branching direction with the root node
	 * 
	 * @param world The world
	 * @param pos The position of the branch being lobbed
	 * @return The volume of the portion of the tree that was destroyed
	 */
	public int destroyTreeFromNode(World world, BlockPos pos) {//, float fortuneFactor) {
		IBlockState blockState = world.getBlockState(pos);
		NodeSpecies nodeSpecies = new NodeSpecies();
		MapSignal signal = analyse(blockState, world, pos, null, new MapSignal(nodeSpecies));// Analyze entire tree network to find root node and species
		Species species = nodeSpecies.getSpecies();//Get the species from the root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		// Analyze only part of the tree beyond the break point and calculate it's volume
		analyse(blockState, world, pos, signal.localRootDir, new MapSignal(volumeSum, new NodeDestroyer(species)));
		return volumeSum.getVolume();// Drop an amount of wood calculated from the body of the tree network
	}
	
	/**
	 * Destroys all branches recursively in the entire tree
	 * 
	 * @param world The world
	 * @param pos The position of the branch being lobbed
	 * @return The volume of the tree that was destroyed
	 */
	public int destroyEntireTree(World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		NodeSpecies nodeSpecies = new NodeSpecies();
		analyse(blockState, world, pos, null, new MapSignal(nodeSpecies));// Analyze entire tree network to find the species
		Species species = nodeSpecies.getSpecies();//Get the species from the root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		// Analyze the entire tree and calculate it's volume
		analyse(blockState, world, pos, null, new MapSignal(volumeSum, new NodeDestroyer(species)));
		return volumeSum.getVolume();// Drop an amount of wood calculated from the body of the tree network
	}
	
	///////////////////////////////////////////
	// DROPS AND HARVESTING
	///////////////////////////////////////////
	
	public List<ItemStack> getLogDrops(World world, BlockPos pos, int volume) {
		List<ItemStack> ret = new java.util.ArrayList<ItemStack>();//A list for storing all the dead tree guts
		volume *= ModConfigs.treeHarvestMultiplier;// For cheaters.. you know who you are.
		return getFamily().getCommonSpecies().getLogsDrops(world, pos, ret, volume);
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
		int woodVolume = destroyTreeFromNode(world, pos);
		List<ItemStack> items = getLogDrops(world, pos, (int)(woodVolume * fortuneFactor));
		
		//For An-Sar's PrimalCore mod :)
		float chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, fortune, 1.0f, false, player);
		
		for (ItemStack item : items) {
			if (world.rand.nextFloat() <= chance) {
				spawnAsEntity(world, pos, item);
			}
		}
		
		return true;// Function returns true if Block was destroyed
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
		int woodVolume = destroyTreeFromNode(world, pos);
		for (ItemStack item : getLogDrops(world, pos, woodVolume)) {
			spawnAsEntity(world, pos, item);
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
