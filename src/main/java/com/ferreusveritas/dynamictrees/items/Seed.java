package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
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

		if(entityItem.ticksExisted >= ConfigHandler.seedTimeToLive) {//1 minute by default(helps with lag)
			if(!entityItem.worldObj.isRemote) {//Server side only
				BlockPos pos = new BlockPos(entityItem);
				if(entityItem.worldObj.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ())) {
					Random rand = new Random();
					ItemStack seedStack = entityItem.getEntityItem();
					int count = seedStack.stackSize;
					while(count-- > 0) {
						if( rand.nextFloat() * (1f/ConfigHandler.seedPlantRate) <= getTree(seedStack).biomeSuitability(entityItem.worldObj, pos) ){//1 in 16 chance if ideal
							if(plantSapling(entityItem.worldObj, pos, seedStack)) {
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
	public boolean onItemUse(ItemStack heldItem, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz) {

		BlockPos pos = new BlockPos(x, y, z);
		
		//Handle Flower Pot interaction
		IBlockState blockState = pos.getBlockState(world);
		if(blockState.equals(new BlockAndMeta(Blocks.flower_pot, 0))) { //Empty Flower Pot
			DynamicTree tree = getTree(heldItem);
			BlockBonsaiPot bonzaiPot = tree.getBonzaiPot();
			bonzaiPot.setTree(world, tree, pos);
			heldItem.stackSize--;
			return true;
		}
		
		if (side == 1) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(x, y, z, side, heldItem) && player.canPlayerEdit(x, y + 1, z, side, heldItem)) {//Ensure permissions to edit block
				if(plantSapling(world, pos.up(), heldItem)) {//Do the planting
					heldItem.stackSize--;
					return true;
				}
			}
		}

		return false;
	}

	public boolean plantSapling(World world, BlockPos pos, ItemStack seedStack) {
		if(pos.getBlock(world).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()) && BlockDynamicSapling.canSaplingStay(world, getTree(seedStack), pos)) {
			getTree(seedStack).getDynamicSapling().setInWorld(world, pos);
			return true;
		}

		return false;
	}

	public boolean isAcceptableSoil(IBlockState soilBlock, ItemStack seedStack) {
		return getTree(seedStack).isAcceptableSoil(soilBlock);
	}
}
