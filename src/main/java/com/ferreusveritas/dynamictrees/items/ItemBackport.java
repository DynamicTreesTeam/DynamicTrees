package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumActionResult;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.util.IRegisterable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBackport extends Item implements IRegisterable {

	protected String registryName;
	
	@Override
	public void setRegistryName(String regName) {
		registryName = regName;
	}

	@Override
	public String getRegistryName() {
		return registryName;
	}

	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setUnlocalizedName(unlocalName);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, net.minecraft.world.World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return this.onItemUse(stack, player, new World(world), new BlockPos(x, y, z), EnumHand.MAIN_HAND, EnumFacing.getFront(side), hitX, hitY, hitZ).result();
	}
	
	public EnumActionResult onItemUse(ItemStack heldStack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return super.onItemUse(heldStack, player, world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), facing.getIndex(), hitX, hitY, hitZ) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}
	
}
