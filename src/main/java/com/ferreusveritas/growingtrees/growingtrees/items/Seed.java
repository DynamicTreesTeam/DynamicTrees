package com.ferreusveritas.growingtrees.items;

import java.util.Random;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Seed extends Item {

	private GrowingTree tree;//The tree this seed creates
	
	public Seed(){
	}
	
	public void setTree(GrowingTree tree){
		this.tree = tree;
	}
	
	public GrowingTree getTree(){
		return tree;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem){

		if(entityItem.age >= ConfigHandler.seedTimeToLive){//1 minute(helps with lag)
			if(!entityItem.worldObj.isRemote){//Server side only
				int tileX = (int)Math.floor(entityItem.posX);
				int tileY = (int)Math.floor(entityItem.posY);
				int tileZ = (int)Math.floor(entityItem.posZ);
				if(entityItem.worldObj.canBlockSeeTheSky(tileX, tileY, tileZ)){
					Random rand = new Random();
					while(entityItem.getEntityItem().stackSize-- > 0){
						if( rand.nextFloat() * (1f/ConfigHandler.seedPlantRate) <= getTree().biomeSuitability(entityItem.worldObj, tileX, tileY, tileZ) ){//1 in 16 chance if ideal
							if(plantTree(entityItem.worldObj, tileX, tileY, tileZ)){
								break;
							}
						}
					}
				}
			}
			entityItem.setDead();
		}

		return false;
	}
	
	
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz){

		if (side == 1) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(x, y, z, side, itemStack) && player.canPlayerEdit(x, y + 1, z, side, itemStack)) {//Ensure permissions to edit block
				if(plantTree(world, x, y + 1, z)){//Do the planting
					itemStack.stackSize--;
					return true;
				}
			}
		}

		return false;
	}

	public boolean plantTree(World world, int x, int y, int z){

        //Ensure there are no adjacent branches
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			if(TreeHelper.isBranch(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)){
				return false;
			}
		}

		//Ensure planting conditions are right
		if(world.isAirBlock(x, y, z) && isAcceptableSoil(world.getBlock(x, y - 1, z))){
			world.setBlock(x, y, z, getTree().getGrowingBranch(), 0, 3);//set to a single branch with 1 radius
			world.setBlock(x, y - 1, z, getTree().getRootyDirtBlock(), 15, 3);//Set to fully fertilized rooty dirt
			return true;
		}
		
		return false;
	}
	
	public boolean isAcceptableSoil(Block soilBlock){
		return soilBlock == Blocks.dirt || soilBlock == Blocks.grass || soilBlock == Blocks.mycelium || soilBlock == GrowingTrees.blockRootyDirt;
	}
}
