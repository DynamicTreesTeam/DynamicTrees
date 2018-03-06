package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockTrunkShell extends Block {

    public static final PropertyEnum<CoordUtils.Surround> TRUNKDIR = PropertyEnum.<CoordUtils.Surround>create("trunkdir", CoordUtils.Surround.class);
	public static final PropertyBool PBIT = PropertyBool.create("pbit"); 
    
	public BlockTrunkShell(Material materialIn, String name) {
		super(materialIn);
		setSoundType(SoundType.WOOD);
		createBlockState();
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{PBIT, TRUNKDIR}, new IUnlistedProperty[] {MimicProperty.MIMIC});
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TRUNKDIR, CoordUtils.Surround.values()[meta & 7]).withProperty(PBIT, (meta & 8) != 0);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TRUNKDIR).ordinal() | (state.getValue(PBIT) ? 8 : 0);
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).withProperty(MimicProperty.MIMIC, getMimic(access, pos)) : state;
	}
	
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		IBlockState mimic = Blocks.LOG.getDefaultState();//Default to oak log
		return mimic;
	}
	
	Vec3i getHeartwoodOffset(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return blockState.getValue(TRUNKDIR).getOffset();
	}
	
	BlockPos getHeartwoodPos(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return pos.add(getHeartwoodOffset(blockState, blockAccess, pos));
	}
	
	IBlockState getHeartwoodState(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return blockAccess.getBlockState(getHeartwoodPos(blockState, blockAccess, pos));
	}
	
	int getRealRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		
		return 0;
	}
}
