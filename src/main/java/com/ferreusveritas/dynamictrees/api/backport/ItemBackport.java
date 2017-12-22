package com.ferreusveritas.dynamictrees.api.backport;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBackport extends Item implements IRegisterable {

	protected ResourceLocation name;
	
	public void setRegistryName(String name) {
		ResourceLocation resloc = new ResourceLocation(name);
		ModContainer mc = Loader.instance().activeModContainer();
		String domain = mc.getModId().toLowerCase();
		setRegistryName(new ResourceLocation(domain, resloc.getResourcePath()));
	}
	
	@Override
	public void setRegistryName(ResourceLocation name) {
		this.name = name;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return name;
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
