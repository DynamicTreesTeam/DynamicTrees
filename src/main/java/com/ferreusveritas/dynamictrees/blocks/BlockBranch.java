package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.inspectors.NodeDestroyer;
import com.ferreusveritas.dynamictrees.inspectors.NodeNetVolume;
import com.ferreusveritas.dynamictrees.renderers.RendererBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockBranch extends BlockBackport implements ITreePart, IAgeable {

	private DynamicTree tree;//The tree this branch type creates

	public BlockBranch(String name) {
		super(Material.wood); //Trees are made of wood. Brilliant.
		setStepSound(soundTypeWood); //aaaaand they also sound like wood.
		setHarvestLevel("axe", 0);
		setTickRandomly(true); //We need this to facilitate decay when supporting neighbors are lacking
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	public int radiusToMeta(int radius) {
		return MathHelper.clamp(radius, 1, 8) - 1;
	}
	
	public int metaToRadius(int meta) {
		return (meta & 7) + 1;
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public void setTree(DynamicTree tree) {
		this.tree = tree;
	}
	
	public DynamicTree getTree() {
		return tree;
	}
	
	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return getTree();
	}
	
	@Override
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return true;
	}
	
	public boolean isSameWood(ITreePart treepart) {
		return isSameWood(TreeHelper.getBranch(treepart));
	}
	
	public boolean isSameWood(BlockBranch branch) {
		return branch != null && getTree() == branch.getTree();
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return isSameWood(branch) ? 0x11 : 0;// Other branches of the same type are always valid support.
	}

	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		age(world, pos, state, random, false);
	}

	@Override
	public boolean age(World world, BlockPos pos, IBlockState state, Random rand, boolean fast) {
		int radius = getRadius(world, pos);
		if (fast || rand.nextInt(radius * 2) == 0) {// Thicker branches take longer to rot
			return checkForRot(world, pos, radius, rand, fast);
		}
		
		return false;
	}

	public boolean checkForRot(World world, BlockPos pos, int radius, Random rand, boolean fast) {
		// Rooty dirt below the block counts as a branch in this instance
		// Rooty dirt below for saplings counts as 2 neighbors if the soil is not infertile
		int neigh = 0;// High Nybble is count of branches, Low Nybble is any reinforcing treepart(including branches)

		for (EnumFacing dir : EnumFacing.VALUES) {
			BlockPos deltaPos = pos.offset(dir);
			neigh += TreeHelper.getSafeTreePart(world, deltaPos).branchSupport(world, this, deltaPos, dir, radius);
			if (neigh >= 0x10 && (neigh & 0x0F) >= 2) {// Need two neighbors.. one of which must be another branch
				return false;// We've proven that this branch is reinforced so there is no need to continue
			}
		}
		return getTree().rot(world, pos, neigh & 0x0F, radius, rand);// Unreinforced branches are destroyed
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
		DynamicTree tree = TreeHelper.getSafeTreePart(world, pos).getTree(world, pos);
		if (tree != null && tree.onTreeActivated(world, pos, player, facing, hitX, hitY, hitZ)) {
			return true;
		}

		ItemStack heldItem = player.getCurrentEquippedItem();
		if (heldItem != null) {
			return applyItemSubstance(world, pos, player, heldItem);
		}
		return false;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, ItemStack itemStack) {

		BlockPos down = pos.down();

		if(down.getBlock(world) != this) { // Make sure the below block is not another branch block
			// This is most likely rooty soil.
			return TreeHelper.getSafeTreePart(world, down).applyItemSubstance(world, down, player, itemStack);
		}
		return false;
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos) {
		int radius = getRadius(world, pos);
		return getTree().getPrimitiveLog().getBlock().getBlockHardness(world, pos.getX(), pos.getY(), pos.getZ()) * (radius * radius) / 64.0f * 8.0f;
	};

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		// return 300;
		return getTree().getPrimitiveLog().getBlock().getFlammability(world, pos.getX(), pos.getY(), pos.getZ(), face.toForgeDirection());
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		// return 4096;
		return getTree().getPrimitiveLog().getBlock().getFireSpreadSpeed(world, pos.getX(), pos.getY(), pos.getZ(), face.toForgeDirection());
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		if(RendererBranch.renderFaceFlags == RendererBranch.faceAll) {// Behave like a regular block
			return super.shouldSideBeRendered(access, x, y, z, side);
		}
		return (1 << side & RendererBranch.renderFaceFlags) != 0;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	//Bark or wood Ring texture for branches
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return getTree().getPrimitiveLog().getIcon((1 << side & RendererBranch.renderRingSides) != 0 ? 0 : 2);//0:Ring, 2:Bark
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
	}

	@Override
	public int getRenderType() {
		return RendererBranch.id;
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
		DynamicTree thisTree = getTree();

		if(leavesTree == thisTree) {// The requesting leaves must match the tree for hydration to occur
			return thisTree.getCellForBranch(blockAccess, pos, blockState, dir, this);
		} else {
			return Cells.nullCell;
		}
	}
	
	@Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return getRadius(pos.getBlockState(blockAccess));
	}

	public int getRadius(IBlockState blockState) {
		if (blockState.getBlock() == this) {
			return metaToRadius(blockState.getMeta());
		} else {
			return 0;
		}
	}

	public void setRadius(World world, BlockPos pos, int radius) {
		world.setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), radiusToMeta(radius), 2);
	}

	// Directionless probability grabber
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return isSameWood(from) ? getRadius(blockAccess, pos) + 2 : 0;
	}

	public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
		BlockDynamicLeaves leaves = getTree().getDynamicLeaves();
		if (leaves != null) {
			if (fromRadius == 1) {// If we came from a twig then just make some leaves
				signal.success = leaves.growLeaves(world, getTree(), pos, 0);
			} else {// Otherwise make a proper branch
				return leaves.branchOut(world, pos, signal);
			}
		}
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {

		if (signal.step()) {// This is always placed at the beginning of every growSignal function
			EnumFacing originDir = signal.dir.getOpposite();// Direction this signal originated from
			EnumFacing targetDir = getTree().selectNewDirection(world, pos, this, signal);// This must be cached on stack for proper recursion
			signal.doTurn(targetDir);

			{
				BlockPos deltaPos = pos.offset(targetDir);

				// Pass grow signal to next block in path
				ITreePart treepart = TreeHelper.getTreePart(world, deltaPos);
				if (treepart != null) {
					signal = treepart.growSignal(world, deltaPos, signal);//Recurse
				} else if (deltaPos.isAirBlock(world)) {
					signal = growIntoAir(world, deltaPos, signal, getRadius(world, pos));
				}
			}

			// Calculate Branch Thickness based on neighboring branches
			float areaAccum = signal.radius * signal.radius;// Start by accumulating the branch we just came from

			for (EnumFacing dir : EnumFacing.VALUES) {
				if (!dir.equals(originDir) && !dir.equals(targetDir)) {// Don't count where the signal originated from or the branch we just came back from
					BlockPos deltaPos = pos.offset(dir);

					// If it is decided to implement a special block(like a squirrel hole, tree
					// swing, rotting, burned or infested branch, etc) then this new block could be
					// derived from BlockBranch and this works perfectly. Should even work with
					// tileEntity blocks derived from BlockBranch.
					ITreePart treepart = TreeHelper.getTreePart(world, deltaPos);
					if (isSameWood(treepart)) {
						int branchRadius = treepart.getRadius(world, deltaPos);
						areaAccum += branchRadius * branchRadius;
					}
				}
			}

			// The new branch should be the square root of all of the sums of the areas of the branches coming into it.
			// But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
			signal.radius = MathHelper.clamp((float) Math.sqrt(areaAccum) + getTree().getTapering(), getRadius(world, pos), 8);// WOW!
			setRadius(world, pos, (int) Math.floor(signal.radius));
		}

		return signal;
	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	// This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	// The result is that only thin branches and trunks can be climbed
	@Override
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return true;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		int radius = getRadius(blockAccess, pos);

		if(radius > 0) {
			float rad = radius / 16.0f;
			float minx = 0.5f - rad;
			float miny = 0.5f - rad;
			float minz = 0.5f - rad;
			float maxx = 0.5f + rad;
			float maxy = 0.5f + rad;
			float maxz = 0.5f + rad;

			boolean connectionMade = false;
			
			for(EnumFacing dir: EnumFacing.VALUES) {
				if(getSideConnectionRadius(blockAccess, pos, radius, dir) > 0) {
					connectionMade = true;
					switch(dir){
						case DOWN: miny = 0.0f; break;
						case UP: maxy = 1.0f; break;
						case NORTH: minz = 0.0f; break;
						case SOUTH: maxz = 1.0f; break;
						case WEST: minx = 0.0f; break;
						case EAST: maxx = 1.0f; break;
						default: break;
					}
				}
			}

			if(!connectionMade) {
				miny = 0.0f;
				maxy = 1.0f;
			}

			this.setBlockBounds(minx, miny, minz, maxx, maxy, maxz);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	public int getRadiusForConnection(IBlockAccess world, BlockPos pos, BlockBranch from, int fromRadius) {
		return getRadius(world, pos);
	}

	public int getSideConnectionRadius(IBlockAccess blockAccess, BlockPos pos, int radius, EnumFacing side) {
		BlockPos deltaPos = pos.offset(side);
		return TreeHelper.getSafeTreePart(blockAccess, deltaPos).getRadiusForConnection(blockAccess, deltaPos, this, radius);
	}

	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node
		if (signal.depth++ < 32) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			signal.run(world, this, pos, fromDir);// Run the inspectors of choice
			for (EnumFacing dir : EnumFacing.VALUES) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.offset(dir);

					signal = TreeHelper.getSafeTreePart(world, deltaPos).analyse(world, deltaPos, dir.getOpposite(), signal);

					// This should only be true for the originating block when the root node is found
					if (signal.found && signal.localRootDir == null && fromDir == null) {
						signal.localRootDir = dir;
					}
				}
			}
			signal.returnRun(world, this, pos, fromDir);
		} else {
			world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());// Destroy one of the offending nodes
			signal.overflow = true;
		}
		signal.depth--;

		return signal;
	}

	// Destroys all branches recursively not facing the branching direction with the root node
	public int destroyTreeFromNode(World world, BlockPos pos) {
		MapSignal signal = analyse(world, pos, null, new MapSignal());// Analyze entire tree network to find root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		// Analyze only part of the tree beyond the break point and calculate it's volume
		analyse(world, pos, signal.localRootDir, new MapSignal(volumeSum, new NodeDestroyer(getTree())));
		return volumeSum.getVolume();// Drop an amount of wood calculated from the body of the tree network
	}

	public int destroyEntireTree(World world, BlockPos pos) {
		NodeNetVolume volumeSum = new NodeNetVolume();
		// Analyze the entire tree and calculate it's volume
		analyse(world, pos, null, new MapSignal(volumeSum, new NodeDestroyer(getTree())));
		return volumeSum.getVolume();// Drop an amount of wood calculated from the body of the tree network
	}

	///////////////////////////////////////////
	// DROPS AND HARVESTING
	///////////////////////////////////////////

	public List<ItemStack> getWoodDrops(World world, BlockPos pos, int volume) {
		List<ItemStack> ret = new java.util.ArrayList<ItemStack>();//A list for storing all the dead tree guts

		volume *= ConfigHandler.treeHarvestMultiplier;// For cheaters.. you know who you are.
		DynamicTree tree = getTree();
		ItemStack logStack = tree.getPrimitiveLogItemStack(volume / 4096);// A log contains 4096 voxels of wood material(16x16x16 pixels)
		ItemStack stickStack = tree.getStick((volume % 4096) / 512);// A stick contains 512 voxels of wood (1/8th log) (1 log = 4 planks, 2 planks = 4 sticks)
		ret.add(logStack);// Drop vanilla logs or whatever
		ret.add(stickStack);// Give him the stick!
		return ret;
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
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean canHarvest) {
		BlockPos pos = new BlockPos(x, y, z);
		int fortune = EnchantmentHelper.getFortuneModifier(player);
		float fortuneFactor = 1.0f + 0.25f * fortune;
		int woodVolume = destroyTreeFromNode(world, pos);
		List<ItemStack> items = getWoodDrops(world, pos, (int)(woodVolume * fortuneFactor));
		
		//For An-Sar's PrimalCore mod :)
		float chance = ForgeEventFactory.fireBlockHarvesting(new ArrayList(items), world, pos.getBlock(world), x, y, z, pos.getMeta(world), fortune, 1.0f, false, player);
		
		for (ItemStack item : items) {
			if (world.rand.nextFloat() <= chance) {
				CompatHelper.spawnItemStackAsEntity(world, pos, item);
			}
		}
		
		return true;// Function returns true if Block was destroyed
	}

	// Super member also does nothing
	@Override
	public void onBlockHarvested(World world, BlockPos pos, int localMeta, EntityPlayer player) {
	}
	
	// Since we already created drops in removedByPlayer() we must disable this.
	// Also we should definitely not return BlockBranch itemBlocks and here's why:
	//	* Players can use these blocks to make branch network loops that will grow artificially large in a short time.
	//	* Players can create invalid networks with more than one root node.
	//  * Players can exploit fortune enchanted tools by building a tree with parts and cutting it down for more wood.
	//	* Players can attach the wrong kind of branch to a tree leading to undefined behavior.
	// If a player in creative wants to do these things then that's their prerogative. 
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}

	// Similar to above.. We already created drops in removedByPlayer() so no quantity should be expressed
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	// We do not allow silk harvest for all the reasons listed in getItemDropped
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	// We do not allow the tree branches to be pushed by a piston for reasons that should be obvious if you
	// are paying attention.
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	// Explosive harvesting methods will likely result in mostly sticks but i'm okay with that since it kinda makes sense.
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		int woodVolume = destroyTreeFromNode(world, pos);
		for (ItemStack item : getWoodDrops(world, pos, woodVolume)) {
			CompatHelper.spawnItemStackAsEntity(world, pos, item);
		}
	}
	
	
	///////////////////////////////////////////
	// IRRELEVANT
	///////////////////////////////////////////

	@Override
	public boolean isRootNode() {
		return false;
	}

}
