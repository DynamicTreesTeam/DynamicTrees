package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockThickBranch extends BlockBranchBasic {
	
	protected static final PropertyInteger RADIUS = PropertyInteger.create("radius", 1, 24);
	boolean extended = false;
	public BlockThickBranch otherBlock;
	
	public BlockThickBranch(Material material, String name) {
		this(material, name, false);
		otherBlock = new BlockThickBranch(material, name, true);
		otherBlock.otherBlock = this;
	}
	
	protected BlockThickBranch(Material material, String name, boolean extended) {
		super(material, name);
		this.extended = extended;
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = { RADIUS };
		return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(RADIUS, meta + 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(RADIUS) - 1;
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState retval = (IExtendedBlockState) state;
			int thisRadius = getRadius(state);
			
			for (EnumFacing dir : EnumFacing.VALUES) {
				retval = retval.withProperty(CONNECTIONS[dir.getIndex()], getSideConnectionRadius(world, pos, thisRadius, dir));
			}
			return retval;
		}
		
		return state;
	}
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	public int getRadius(IBlockState blockState) {
		return blockState.getBlock() == this ? blockState.getValue(RADIUS) : 0;
	}
	
	@Override
	public void setRadius(World world, BlockPos pos, int radius, EnumFacing dir, int flags) {
		radius = MathHelper.clamp(radius, 1, 16);
		world.setBlockState(pos, getDefaultState().withProperty(RADIUS, radius));
	}
	
}