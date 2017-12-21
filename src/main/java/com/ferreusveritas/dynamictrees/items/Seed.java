package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModConfigs;
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
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;


public class Seed extends ItemBackport {

	private Species species;//The tree this seed creates

	public Seed(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}
	
	public void setSpecies(Species species, ItemStack seedStack) {
		this.species = species;
	}
	
	public Species getSpecies(ItemStack seedStack) {
		return species;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		World world = CompatHelper.getEntityWorld(entityItem);
		
		if(entityItem.ticksExisted >= ModConfigs.seedTimeToLive) {//1 minute by default(helps with lag)
			if(!world.isRemote) {//Server side only
				BlockPos pos = new BlockPos(entityItem);
				if(world.canBlockSeeSky(pos)) {
					Random rand = new Random();
					ItemStack seedStack = CompatHelper.getEntityItem(entityItem);
					int count = CompatHelper.getStackCount(seedStack);
					while(count-- > 0) {
						if( getSpecies(seedStack).biomeSuitability(world, pos) * ModConfigs.seedPlantRate > rand.nextFloat()){
							if(plantSapling(world, pos, seedStack)) {
								break;
							}
						}
					}
					CompatHelper.setStackCount(CompatHelper.getEntityItem(entityItem), 0);
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
			Species species = getSpecies(heldItem);
			BlockBonsaiPot bonzaiPot = species.getTree().getBonzaiPot();//FIXME: Species need their own bonsai pots.. or find another solution
			if(bonzaiPot.setSpecies(world, species, pos)) {
				CompatHelper.shrinkStack(heldItem, 1);
				return EnumActionResult.SUCCESS;
			}
		}
		
		if (facing == EnumFacing.UP) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(pos.getX(), pos.getY(), pos.getZ(), facing.getIndex(), heldItem) && player.canPlayerEdit(pos.getX(), pos.getY() + 1, pos.getZ(), facing.getIndex(), heldItem)) {//Ensure permissions to edit block
				if(plantSapling(world, pos.up(), heldItem)) {//Do the planting
					CompatHelper.shrinkStack(heldItem, 1);
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
		Species species = getSpecies(seedStack);
		
		if(world.getBlockState(pos).getBlock().isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()) && BlockDynamicSapling.canSaplingStay(world, species, pos)) {
			return species.placeSaplingBlock(world, pos);
		}

		return false;
	}
	
}
