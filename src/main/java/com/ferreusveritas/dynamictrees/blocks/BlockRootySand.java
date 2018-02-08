package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.BlockSand;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootySand extends BlockRooty {

	public static final PropertyEnum MIMIC = PropertyEnum.create("mimic", EnumMimicSandType.class);
	
	static String name = "rootysand";
	
	public BlockRootySand() {
		this(name);
	}
	
	public BlockRootySand(String name) {
		super(name, Material.SAND);
		setSoundType(SoundType.SAND);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{LIFE, MIMIC});
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(MIMIC, getMimicType(worldIn, pos));
	}

	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	public EnumMimicSandType getMimicType(IBlockAccess blockAccess, BlockPos pos) {
		final int dMap[] = {0, -1, 1};
		
		for(int depth: dMap) {
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				IBlockState mimic = blockAccess.getBlockState(pos.offset(dir).down(depth));
				
				for(EnumMimicSandType muse: EnumMimicSandType.values()) {
					if(muse != EnumMimicSandType.SAND) {
						if(mimic == muse.getBlockState()) {
							return muse;
						}
					}
				}
			}
		}
		
		return EnumMimicSandType.SAND;//Default to plain old dirt
	}
	
	
	public static enum EnumMimicSandType implements IStringSerializable {
		
		SAND(Blocks.SAND.getDefaultState(), BlockSand.EnumType.SAND.getName()),
		RED_SAND(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND), BlockSand.EnumType.RED_SAND.getName());
		
		private final IBlockState muse;
		private final String name;
		
		private EnumMimicSandType(IBlockState muse, String name) {
			this.muse = muse;
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		public IBlockState getBlockState() {
			return muse;
		}
		
	}
	
}
