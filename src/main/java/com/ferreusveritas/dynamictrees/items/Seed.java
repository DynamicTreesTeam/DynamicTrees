package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class Seed extends ItemReg {

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
		WorldDec world = new WorldDec(entityItem.worldObj);

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
	public boolean onItemUse(ItemStack heldItem, EntityPlayer player, World _world, int x, int y, int z, int side, float px, float py, float pz) {
		WorldDec world = new WorldDec(_world);
		
		BlockPos pos = new BlockPos(x, y, z);
		EnumFacing facing = EnumFacing.getFront(side);
		
		//Handle Flower Pot interaction
		IBlockState blockState = world.getBlockState(pos);
		if(blockState.equals(new BlockAndMeta(Blocks.flower_pot, 0))) { //Empty Flower Pot
			DynamicTree tree = getTree(heldItem);
			BlockBonsaiPot bonzaiPot = tree.getBonzaiPot();
			bonzaiPot.setTree(world, tree, pos);
			heldItem.stackSize--;
			return true;
		}
		
		if (facing == EnumFacing.UP) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(x, y, z, side, heldItem) && player.canPlayerEdit(x, y + 1, z, side, heldItem)) {//Ensure permissions to edit block
				if(plantSapling(world, pos.up(), heldItem)) {//Do the planting
					heldItem.stackSize--;
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Checks surroundings and places a dynamic sapling block.
	 * 
	 * @param world
	 * @param pos
	 * @param seedStack
	 * @return
	 */
	public boolean plantSapling(WorldDec world, BlockPos pos, ItemStack seedStack) {
		DynamicTree tree = getTree(seedStack);
		
		if(world.getBlock(pos).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()) && BlockDynamicSapling.canSaplingStay(world, tree, pos)) {
			world.setBlockState(pos, tree.getDynamicSapling());
			return true;
		}

		return false;
	}
	
}
