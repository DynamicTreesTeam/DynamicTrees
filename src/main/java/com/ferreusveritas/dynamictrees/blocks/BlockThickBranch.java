package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	@Override
	public int getRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {

		int heartRadius = super.getRawRadius(blockState);
		
		if(blockState.getValue(QBIT)) {
			boolean pBit = false;
			for(CoordUtils.Surround surr: CoordUtils.Surround.values()) {
				IBlockState shellState = blockAccess.getBlockState(pos.add(surr.getOffset()));
				if(shellState instanceof BlockTrunkShell && shellState.getValue(BlockTrunkShell.TRUNKDIR) == surr.getOpposite()) {
					pBit = shellState.getValue(BlockTrunkShell.PBIT);
					break;
				}
			}
			return heartRadius + (pBit ? 16 : 8);
		}
		
		return heartRadius;
	}
	
	@Override
	public void setRadius(World world, BlockPos pos, int radius, EnumFacing dir, int flags) {
		
		radius = MathHelper.clamp(radius, 1, 24) - 1;
		boolean pBit = radius >= 16;
		boolean qBit = radius >= 8;
		int rBits = radius & 7;
		
		world.setBlockState(pos, getDefaultState().withProperty(RADIUS, rBits + 1).withProperty(QBIT, qBit));
		
		for(CoordUtils.Surround surr: CoordUtils.Surround.values()) {
			BlockPos shellPos = pos.add(surr.getOffset());
			IBlockState shellState = world.getBlockState(shellPos);
			if(shellState.getMaterial().isReplaceable()
				|| (shellState instanceof BlockTrunkShell && shellState.getValue(BlockTrunkShell.TRUNKDIR) == surr.getOpposite()) ) {
				setShell(world, pos, surr, pBit, qBit);
			}
		}
		
	}
	
	private void setShell(World world, BlockPos shellPos, CoordUtils.Surround surr, boolean pBit, boolean qBit) {
		//TODO:  Make a proper version of this
		BlockTrunkShell trunkShell = new BlockTrunkShell(Material.WOOD, "trunkshell");
		
		if(qBit) {
			world.setBlockState(shellPos,
				trunkShell.getDefaultState()
				.withProperty(BlockTrunkShell.PBIT, pBit)
				.withProperty(BlockTrunkShell.TRUNKDIR, surr.getOpposite())
			);
		} else {
			world.setBlockToAir(shellPos.add(surr.getOffset()));
		}
	}
	
}


