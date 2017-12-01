package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.ItemBackport;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.util.GameRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class DirtBucket extends ItemBackport {

	public static final String name = "dirtbucket";

	public DirtBucket() {
		this(name);
	}

	public DirtBucket(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedName(name);
		setRegistryName(name);
		setTextureName(DynamicTrees.MODID + ":" + name);
		setMaxStackSize(1);
		setContainerItem(this);
	}

	//[VanillaCopy] ItemBucket member function modified for purpose
	public ItemStack onItemRightClick(ItemStack stack, net.minecraft.world.World _world, EntityPlayer player) {
		World world = new World(_world);
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world.real(), player, false);
	
		if (movingobjectposition == null) {
			return stack;
		}
		else {
			if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				BlockPos pos = new BlockPos(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
				
				if (!world.canMineBlock(player, pos)) {
					return stack;
				}
				
				pos = pos.offset(EnumFacing.getFront(movingobjectposition.sideHit));
				
				if (!player.canPlayerEdit(pos.getX(), pos.getY(), pos.getZ(), movingobjectposition.sideHit, stack)) {
					return stack;
				}
				
				if (this.tryPlaceContainedDirt(player, world, pos) && !player.capabilities.isCreativeMode) {
					return new ItemStack(Items.bucket);
				}
			}
			
			return stack;
		}
	}
	
	public boolean tryPlaceContainedDirt(EntityPlayer player, World world, BlockPos pos) {
		
		IBlockState blockState = world.getBlockState(pos);
		boolean replaceable = blockState.getBlock().isReplaceable(world, pos.getX(), pos.getY(), pos.getZ());
		
		if(replaceable) {
			if (!world.isRemote()) {
				world.real().func_147480_a(pos.getX(), pos.getY(), pos.getZ(), true);
			}
			
			String soundevent = "dig.grass";
			world.playSoundEffect(pos, soundevent, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);                    
			world.setBlockState(pos, new BlockState(Blocks.dirt, 0), 3);
		}
	
		return true;
	}
	
	public DirtBucket registerRecipes() {
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ItemStack(DynamicTrees.dirtBucket), new Object[]{ Blocks.dirt, Items.bucket});
		return this;
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack itemStack) {
		return false;
	}
}
