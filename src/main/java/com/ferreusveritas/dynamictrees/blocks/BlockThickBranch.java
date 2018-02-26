package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockThickBranch extends BlockBranchBasic {

	public static final PropertyBool QBIT = PropertyBool.create("qbit");
	
	public BlockThickBranch(Material material, String name) {
		super(material, name);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = { RADIUS, QBIT };
		return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(RADIUS, (meta & 7) + 1).withProperty(QBIT, (meta & 8) != 0);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(RADIUS) - 1 | (state.getValue(QBIT) ? 8 : 0);
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState retval = (IExtendedBlockState) state;
			int thisRadius = getRadius(state, world, pos);
			
			for (EnumFacing dir : EnumFacing.VALUES) {
				retval = retval.withProperty(CONNECTIONS[dir.getIndex()], getSideConnectionRadius(world, pos, thisRadius, dir));
			}
			return retval;
		}
		
		return state;
	}
	
}
