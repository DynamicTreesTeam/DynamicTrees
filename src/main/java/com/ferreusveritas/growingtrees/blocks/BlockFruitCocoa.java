package com.ferreusveritas.growingtrees.blocks;

import com.ferreusveritas.growingtrees.TreeHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockFruitCocoa extends BlockCocoa {
	/**
	* Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
	*/
	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		int dir = getDirection(world.getBlockMetadata(x, y, z));
		x += Direction.offsetX[dir];
		z += Direction.offsetZ[dir];
		BlockBranch branch = TreeHelper.getBranch(world, x, y, z);
		return branch != null && branch.getRadius(world, x, y, z) == 8 && branch.getTree().canSupportCocoa;
	}

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
