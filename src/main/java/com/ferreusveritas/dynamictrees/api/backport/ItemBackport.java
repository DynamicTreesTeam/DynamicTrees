package com.ferreusveritas.dynamictrees.api.backport;

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
		return super.onItemUse(heldStack, player, world.real(), pos.getX(), pos.getY(), pos.getZ(), facing.getIndex(), hitX, hitY, hitZ) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}
	
}
