package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

/**
 * Maintaining code between Minecraft versions can be a pain.
 * This is a compatibility layer to abstract the finer points
 * of what has recently changed from MC 1.7.10 to MC 1.12.2
 * 
 * @author ferreusveritas
 *
 */
public class CompatHelper {
	
	//Entities
	
	public static boolean spawnEntity(World world, Entity entity) {
		return world.real().spawnEntityInWorld(entity);
	}

	public static World getEntityWorld(Entity entity) {
		return new World(entity.worldObj);
	}
	
	public static ItemStack getEntityItem(EntityItem entityItem) {
		return entityItem.getEntityItem();
	}
    /**
     * Spawns the given ItemStack as an EntityItem into the World at the given position
     */
    public static void spawnItemStackAsEntity(net.minecraft.world.World world, BlockPos pos, ItemStack stack)  {
        if (!world.isRemote && !world.restoringBlockSnapshots) {// do not drop items while restoring blockstates, prevents item dupe
            double x = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            double y = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            double z = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(world, (double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z, stack);
            entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
        }
    }

    public static void spawnItemStackAsEntity(World world, BlockPos pos, ItemStack stack)  {
    	spawnItemStackAsEntity(world.real(), pos, stack);
    }
	//Biomes
	
	public static boolean biomeHasType(Biome biome, BiomeDictionary.Type type) {
		return BiomeDictionary.isBiomeOfType(biome.getBiomeGenBase(), type);
	}
	
	public static int getBiomeTreesPerChunk(Biome biome) {
		return biome.getBiomeGenBase().theBiomeDecorator.treesPerChunk;
	}
	
	//ItemStacks
	
	public static int getStackCount(ItemStack stack) {
		return isValid(stack) ? stack.stackSize : 0;
	}
	
	public static ItemStack setStackCount(ItemStack stack, int size) {

		if(isValid(stack)) {
			stack.stackSize = size;
			return stack;
		}
		
		return emptyStack();
	}
	
	public static ItemStack growStack(ItemStack stack, int quantity) {
		
		if(isValid(stack)) {
			stack.stackSize += quantity;
			return stack;
		}
		
		return emptyStack();
	}
	
	public static ItemStack shrinkStack(ItemStack stack, int quantity) {
		return growStack(stack, -quantity);
	}
	
	public static ItemStack emptyStack() {
		return null;
	}
	
	public static boolean isValid(ItemStack stack) {
		return stack != null && stack != emptyStack();
	}
	
	public static boolean isStackEmpty(ItemStack stack) {
		return getStackCount(stack) == 0;
	}
	
	public static void consumePlayerItem(EntityPlayer player, EnumHand hand, ItemStack heldItem) {
		if (heldItem.getItem() instanceof IEmptiable) {//A substance deployed from a refillable container
			if(!player.capabilities.isCreativeMode) {
				IEmptiable emptiable = (IEmptiable) heldItem.getItem();
				player.setHeldItem(hand, emptiable.getEmptyContainer());
			}
		}
		else if(heldItem.getItem() == Items.potionitem) {//An actual potion
			if(!player.capabilities.isCreativeMode) {
				player.setHeldItem(hand, new ItemStack(Items.glass_bottle));
			}
		} else {
			CompatHelper.shrinkStack(heldItem, 1); //Just a regular item like bonemeal
		}
	}
	
}
