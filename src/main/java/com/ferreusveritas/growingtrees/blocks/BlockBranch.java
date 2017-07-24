package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.inspectors.NodeDestroyer;
import com.ferreusveritas.growingtrees.inspectors.NodeNetVolume;
import com.ferreusveritas.growingtrees.renderers.RendererBranch;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockBranch extends Block implements ITreePart {

	private GrowingTree tree;

	public BlockBranch() {
		super(Material.wood);//Trees are made of wood. Brilliant.
		this.setTickRandomly(true);//We need this to facilitate decay when supporting neighbors are lacking
	}

	public void setTree(GrowingTree tree) {
		this.tree = tree;
	}

	public GrowingTree getTree() {
		return tree;
	}

	@Override
	public GrowingTree getTree(IBlockAccess blockAccess, int x, int y, int z) {
		return getTree();
	}

	public boolean isSameWood(ITreePart treepart) {
		return isSameWood(TreeHelper.getBranch(treepart));
	}

	public boolean isSameWood(BlockBranch branch) {
		return branch != null && getTree() == branch.getTree();
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, int x, int y, int z, ForgeDirection dir, int radius) {
		return isSameWood(branch) ? 0x11 : 0;//Other branches of the same type are always valid support.
	}

	@Override
    public void updateTick(World world, int x, int y, int z, Random random) {
		int radius = getRadius(world, x, y, z);
		if(random.nextInt(radius * 2) == 0){//Thicker branches take longer to rot
			checkForRot(world, x, y, z, radius, random);
		}
	}

	public boolean checkForRot(World world, int x, int y, int z, int radius, Random random) {
		//Rooty dirt below the block counts as a branch in this instance
		//Rooty dirt below for saplings counts as 2 neighbors if the soil is not infertile
		int neigh = 0;//High Nybble is count of branches, Low Nybble is any reinforcing treepart(including branches)

		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			neigh += TreeHelper.getSafeTreePart(world, dx, dy, dz).branchSupport(world, this, dx, dy, dz, dir, radius);
			if(neigh >= 0x10 && (neigh & 0x0F) >= 2){//Need two neighbors..  one of which must be another branch
				return false;//We've proven that this branch is reinforced so there is no need to continue
			}
		}
		return getTree().rot(world, x, y, z, neigh & 0x0F, radius, random);//Unreinforced branches are destroyed
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz) {
		GrowingTree tree = TreeHelper.getSafeTreePart(world, x, y, z).getTree(world, x, y, z);
		if(tree != null && tree.onTreeActivated(world, x, y, z, player, side, px, py, pz)) {
			return true;
		}

		ItemStack equippedItem = player.getCurrentEquippedItem();

		if(equippedItem != null) {
			return applyItemSubstance(world, x, y, z, player, equippedItem);
		}
		return false;
	}

	@Override
	public boolean applyItemSubstance(World world, int x, int y, int z, EntityPlayer player, ItemStack itemStack) {
		if(world.getBlock(x, y - 1, z) != this) {
			return TreeHelper.getSafeTreePart(world, x, y - 1, z).applyItemSubstance(world, x, y - 1, z, player, itemStack);
		}
		return false;
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		int radius = getRadius(world, x, y, z);
		return getTree().getPrimitiveLog().getBlock().getBlockHardness(world, x, y, z) * (radius * radius) / 64.0f * 8.0f;
	};

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		//return 300;
		return getTree().getPrimitiveLog().getBlock().getFlammability(world, x, y, z, face);
	}
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		//return 4096;
		return getTree().getPrimitiveLog().getBlock().getFireSpreadSpeed(world, x, y, z, face);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean isOpaqueCube() {
		return false;
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

	//Leaf texture for Saplings
	@SideOnly(Side.CLIENT)
	public IIcon getLeavesIcon() {
		return getTree().getPrimitiveLeaves().getIcon(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		if(RendererBranch.renderFaceFlags == RendererBranch.faceAll){//Behave like a regular block
			return super.shouldSideBeRendered(access, x, y, z, side);
		}
		return (1 << side & RendererBranch.renderFaceFlags) != 0;
	}

	@Override
	public int getRenderType() {
		return RendererBranch.id;
	}

	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, GrowingTree leavesTree) {
		return getTree().getBranchHydrationLevel(blockAccess, x, y, z, dir, this, leavesTree.getGrowingLeaves(), leavesTree.getGrowingLeavesSub());
	}

	public boolean isSapling(IBlockAccess blockAccess, int x, int y, int z) {
		return
			TreeHelper.getSafeTreePart(blockAccess, x, y - 1, z).isRootNode() && //Below is rooty dirt
			getRadius(blockAccess, x, y, z) == 1 && //Is a branch has a radius of 1
			TreeHelper.getTreePart(blockAccess, x, y + 1, z) == null; //Above is a non-tree block(hopefully air)
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public int getRadius(IBlockAccess blockAccess, int x, int y, int z) {
		return (blockAccess.getBlockMetadata(x, y, z) & 7) + 1;
	}

	public void setRadius(World world, int x, int y, int z, int radius) {
		radius = MathHelper.clamp_int(radius, 0, 8);
		world.setBlockMetadataWithNotify(x, y, z, (radius - 1) & 7, 2);
	}

	//Directionless probability grabber
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return isSameWood(from) ? getRadius(blockAccess, x, y, z) + 2 : 0;
	}

	public GrowSignal growIntoAir(World world, int x, int y, int z, GrowSignal signal, int fromRadius) {
		BlockGrowingLeaves leaves = getTree().getGrowingLeaves();
		if(leaves != null) {
			if(fromRadius == 1) {//If we came from a twig then just make some leaves
				signal.success = leaves.growLeaves(world, getTree(), x, y, z, 0);
			} else {//Otherwise make a proper branch
				return leaves.branchOut(world, x, y, z, signal);
			}
		}
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, int x, int y, int z, GrowSignal signal) {

		if(signal.step()) {//This is always placed at the beginning of every growSignal function
			ForgeDirection originDir = signal.dir.getOpposite();//Direction this signal originated from
			ForgeDirection targetDir = getTree().selectNewDirection(world, x, y, z, this, signal);//This must be cached on stack for proper recursion
			signal.doTurn(targetDir);

			{
				int dx = x + targetDir.offsetX;
				int dy = y + targetDir.offsetY;
				int dz = z + targetDir.offsetZ;

				//Pass grow signal to next block in path
				ITreePart treepart = TreeHelper.getTreePart(world, dx, dy, dz);
				if(treepart != null) {
					signal = treepart.growSignal(world, dx, dy, dz, signal);//Recurse
				} else if(world.isAirBlock(dx, dy, dz)) {
					signal = growIntoAir(world, dx, dy, dz, signal, getRadius(world, x, y, z));
				}
			}
			
			//Calculate Branch Thickness based on neighboring branches
			float areaAccum = signal.radius * signal.radius;//Start by accumulating the branch we just came from

			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
				if(!dir.equals(originDir) && !dir.equals(targetDir)) {//Don't count where the signal originated from or the branch we just came back from
					int dx = x + dir.offsetX;
					int dy = y + dir.offsetY;
					int dz = z + dir.offsetZ;

					//If it is decided to implement a special block(like a squirrel hole, tree swing, rotting, burned or infested branch, etc) then this new block could be
					//derived from BlockBranch and this works perfectly.  Should even work with tileEntity blocks derived from BlockBranch.
					ITreePart treepart = TreeHelper.getTreePart(world, dx, dy, dz);
					if(isSameWood(treepart)) {
						int branchRadius = treepart.getRadius(world, dx, dy, dz);
						areaAccum += branchRadius * branchRadius;
					}
				}
			}

			//The new branch should be the square root of all of the sums of the areas of the branches coming into it.
			//But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
			signal.radius = MathHelper.clamp_float((float)Math.sqrt(areaAccum) + getTree().getTapering(), getRadius(world, x, y, z), 8);// WOW!
			setRadius(world, x, y, z, (int)Math.floor(signal.radius));
		}

		return signal;
	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	//This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	//The result is that only thin branches and trunks can be climbed
	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return true;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		int radius = getRadius(blockAccess, x, y, z);

		if(radius == 1 && isSapling(blockAccess, x, y, z)) {
			this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
		}
		else
		if(radius > 0) {
			float rad = radius / 16.0f;
			float minx = 0.5f - rad;
			float miny = 0.5f - rad;
			float minz = 0.5f - rad;
			float maxx = 0.5f + rad;
			float maxy = 0.5f + rad;
			float maxz = 0.5f + rad;

			boolean connectionMade = false;
			
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
				if(getSideConnectionRadius(blockAccess, x, y, z, radius, dir) > 0) {
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
	public int getRadiusForConnection(IBlockAccess world, int x, int y, int z, BlockBranch from, int fromRadius) {
		return getRadius(world, x, y, z);
	}

	public int getSideConnectionRadius(IBlockAccess blockAccess, int x, int y, int z, int radius, ForgeDirection side) {
		int dx = x + side.offsetX;
		int dy = y + side.offsetY;
		int dz = z + side.offsetZ;
		return TreeHelper.getSafeTreePart(blockAccess, dx, dy, dz).getRadiusForConnection(blockAccess, dx, dy, dz, this, radius);
	}

	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////

	@Override
	public MapSignal analyse(World world, int x, int y, int z, ForgeDirection fromDir, MapSignal signal) {
		//Note: fromDir will be ForgeDirection.UNKNOWN in the origin node
		if(signal.depth++ < 32) {//Prevents going too deep into large networks, or worse, being caught in a network loop
			signal.run(world, this, x, y, z, fromDir);//Run the inspectors of choice
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {//Spread signal in various directions
				if(dir != fromDir) {//don't count where the signal originated from
					int dx = x + dir.offsetX;
					int dy = y + dir.offsetY;
					int dz = z + dir.offsetZ;

					signal = TreeHelper.getSafeTreePart(world, dx, dy, dz).analyse(world, dx, dy, dz, dir.getOpposite(), signal);

					//This should only be true for the originating block when the root node is found
					if(signal.found && signal.localRootDir == ForgeDirection.UNKNOWN && fromDir == ForgeDirection.UNKNOWN) {
						signal.localRootDir = dir;
					}
				}
			}
			signal.returnRun(world, this, x, y, z, fromDir);
		} else {
			world.setBlockToAir(x, y, z);//Destroy one of the offending nodes
			signal.overflow = true;
		}
		signal.depth--;

		return signal;
	}

	//Destroys all branches recursively not facing the branching direction with the root node
	public void destroyTreeFromNode(World world, int x, int y, int z, float fortuneFactor) {
		MapSignal signal = analyse(world, x, y, z, ForgeDirection.UNKNOWN, new MapSignal());//Analyze entire tree network to find root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		analyse(world, x, y, z, signal.localRootDir, new MapSignal(volumeSum, new NodeDestroyer(getTree())));//Analyze only part of the tree beyond the break point and calculate it's volume
		dropWood(world, x, y, z, (int)(volumeSum.getVolume() * fortuneFactor));//Drop an amount of wood calculated from the body of the tree network
	}

	public void destroyEntireTree(World world, int x, int y, int z) {
		NodeNetVolume volumeSum = new NodeNetVolume();
		analyse(world, x, y, z, ForgeDirection.UNKNOWN, new MapSignal(volumeSum, new NodeDestroyer(getTree())));
		dropWood(world, x, y, z, volumeSum.getVolume());//Drop an amount of wood calculated from the body of the tree network
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////

	public void dropWood(World world, int x, int y, int z, int volume) {
		volume *= ConfigHandler.treeHarvestMultiplier;//For cheaters..  you know who you are.
		int logs = volume / 4096;//A log contains 4096 voxels of wood material(16x16x16 pixels)
		ItemStack stickItem = getTree().getStick().copy();
		stickItem.stackSize = (volume % 4096) / 512;//A stick contains 512 voxels of wood (1/8th log) (1 log = 4 planks, 2 planks = 4 sticks)
		dropBlockAsItem(world, x, y, z, getTree().getPrimitiveLog().toItemStack(logs));//Drop vanilla logs or whatever
		dropBlockAsItem(world, x, y, z, stickItem);//Give him the stick!
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int localMeta, EntityPlayer player) {
		int fortune = EnchantmentHelper.getFortuneModifier(player);
		destroyTreeFromNode(world, x, y, z, 1.0f + 0.25f * fortune);
	}

	//Explosive harvesting methods will likely result in mostly sticks but i'm okay with that since it kinda makes sense.
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		destroyTreeFromNode(world, x, y, z, 1.0f);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		//Normally just sets the block to air but we've already done that.
		return false;//False prevents block harvest as we've already done that also.
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	///////////////////////////////////////////
	// IRRELEVANT
	///////////////////////////////////////////

	@Override
	public boolean isRootNode() {
		return false;
	}

}
