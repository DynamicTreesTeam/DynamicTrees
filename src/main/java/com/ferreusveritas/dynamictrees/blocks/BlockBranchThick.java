package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.ExtendedBlockState;

public class BlockBranchThick extends BlockBranchBasic {
	
	public static final int RADMAX_THICK = 24;
	
	protected static final PropertyInteger RADIUSNYBBLE = PropertyInteger.create("radius", 0, 15);
	protected final boolean extended;
	protected BlockBranchThick otherBlock;
	
	public BlockBranchThick(Material material, String name) {
		this(material, name, false);
		otherBlock = new BlockBranchThick(material, name + "x", true);
		otherBlock.otherBlock = this;
		
		cacheBranchThickStates();
	}
	
	protected BlockBranchThick(Material material, String name, boolean extended) {
		super(material, name);
		this.extended = extended;
	}
	
	public BlockBranchThick getPairSide(boolean ext) {
		return extended ^ ext ? otherBlock : this; 
	}

	@Override
	public void cacheBranchStates() { }
	
	public void cacheBranchThickStates() {
		if(!extended) {
			branchStates = new IBlockState[RADMAX_THICK + 1];
			otherBlock.branchStates = branchStates;
			
			//Cache the branch blocks states for rapid lookup
			branchStates[0] = Blocks.AIR.getDefaultState();
			
			for(int radius = 1; radius <= RADMAX_THICK; radius++) {
				branchStates[radius] = getPairSide(radius > 16).getDefaultState().withProperty(BlockBranchThick.RADIUSNYBBLE, (radius - 1) & 0x0f);
			}
		}
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = { RADIUSNYBBLE };
		return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(RADIUSNYBBLE, meta);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(RADIUSNYBBLE);
	}
	
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	public int getRadius(IBlockState blockState) {
		return MathHelper.clamp(blockState.getValue(RADIUSNYBBLE) + (((BlockBranchThick)blockState.getBlock()).extended ? 17 : 1), 1, getMaxRadius());
	}
	
	@Override
	public int getMaxRadius() {
		return RADMAX_THICK;
	}
	
}