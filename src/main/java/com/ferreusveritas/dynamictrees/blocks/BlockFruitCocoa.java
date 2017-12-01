package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.util.IRegisterable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockCocoa;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;

public class BlockFruitCocoa extends BlockCocoa implements IRegisterable {

	protected String registryName;
	
	public BlockFruitCocoa() {
		this("fruitcocoa");
	}
	
	public BlockFruitCocoa(String name) {
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}

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
		setBlockName(unlocalName);
	}
	
	/**
	* Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
	*/
	@Override
	public boolean canBlockStay(net.minecraft.world.World world, int x, int y, int z) {
		int dir = getDirection(world.getBlockMetadata(x, y, z));
		x += Direction.offsetX[dir];
		z += Direction.offsetZ[dir];
		BlockPos pos = new BlockPos(x, y, z);
		BlockBranch branch = TreeHelper.getBranch(world, pos);
		return branch != null && branch.getRadius(new World(world), pos) == 8 && branch.getTree().canSupportCocoa;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return Blocks.cocoa.getIcon(side, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getCocoaIcon(int side) {
		return ((BlockCocoa) Blocks.cocoa).getCocoaIcon(side);
	}

}
