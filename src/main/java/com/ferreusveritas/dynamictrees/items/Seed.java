package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumActionResult;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.ItemBackport;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;


public class Seed extends ItemBackport {

	private DynamicTree tree;//The tree this seed creates

	public Seed(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}
	
	public void setTree(DynamicTree tree, ItemStack seedStack) {
		this.tree = tree;
	}
	
	public DynamicTree getTree(ItemStack seedStack) {
		return tree;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		World world = new World(entityItem.worldObj);

		if(entityItem.ticksExisted >= ConfigHandler.seedTimeToLive) {//1 minute by default(helps with lag)
			if(!world.isRemote()) {//Server side only
				BlockPos pos = new BlockPos(entityItem);
				if(world.canBlockSeeSky(pos)) {
					Random rand = new Random();
					ItemStack seedStack = entityItem.getEntityItem();
					int count = seedStack.stackSize;
					while(count-- > 0) {
						if( rand.nextFloat() * (1f/ConfigHandler.seedPlantRate) <= getTree(seedStack).biomeSuitability(world, pos) ){//1 in 16 chance if ideal
							if(plantSapling(world, pos, seedStack)) {
								break;
							}
						}
					}
					entityItem.getEntityItem().stackSize = 0;;
				}
			}
			entityItem.setDead();
		}

		return false;
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack heldItem, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		//Handle Flower Pot interaction
		IBlockState blockState = world.getBlockState(pos);
		if(blockState.equals(new BlockState(Blocks.flower_pot, 0))) { //Empty Flower Pot
			DynamicTree tree = getTree(heldItem);
			BlockBonsaiPot bonzaiPot = tree.getBonzaiPot();
			bonzaiPot.setTree(world, tree, pos);
			heldItem.stackSize--;
			return EnumActionResult.SUCCESS;
		}
		
		if (facing == EnumFacing.UP) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(pos.getX(), pos.getY(), pos.getZ(), facing.getIndex(), heldItem) && player.canPlayerEdit(pos.getX(), pos.getY() + 1, pos.getZ(), facing.getIndex(), heldItem)) {//Ensure permissions to edit block
				if(plantSapling(world, pos.up(), heldItem)) {//Do the planting
					heldItem.stackSize--;
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.FAIL;
	}
	
	/**
	 * Checks surroundings and places a dynamic sapling block.
	 * 
	 * @param world
	 * @param pos
	 * @param seedStack
	 * @return
	 */
	public boolean plantSapling(World world, BlockPos pos, ItemStack seedStack) {
		DynamicTree tree = getTree(seedStack);
		
		if(world.getBlock(pos).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()) && BlockDynamicSapling.canSaplingStay(world, tree, pos)) {
			world.setBlockState(pos, tree.getDynamicSapling());
			return true;
		}

		return false;
	}
	
}
