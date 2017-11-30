package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.util.GameRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class DirtBucket extends ItemReg {

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
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, false);

        if (movingobjectposition == null) {
            return stack;
        }
        else {
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos pos = new BlockPos(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
                
                if (!world.canMineBlock(player, pos.getX(), pos.getY(), pos.getZ())) {
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

    	IBlockState blockState = pos.getBlockState(world);
    	boolean replaceable = blockState.getBlock().isReplaceable(world, pos.getX(), pos.getY(), pos.getZ());

    	if(replaceable) {
    		if (!world.isRemote) {
    			world.func_147480_a(pos.getX(), pos.getY(), pos.getZ(), true);
    		}
			
    		String soundevent = "dig.grass";
    		world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), soundevent, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);                    
    		world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.dirt, 0, 3);
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
