package com.ferreusveritas.dynamictrees.api.treedata;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILeavesProperties {
	
	/** The type of tree these leaves connect to */
	ILeavesProperties setTree(TreeFamily tree);
	
	/** This is needed so the {@link BlockDynamicLeaves} knows if it can pull hydro from a branch */
	TreeFamily getTree();
	
	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	IBlockState getPrimitiveLeaves();

	/** cached ItemStack of primitive leaves(what is returned when leaves are sheared) */
	ItemStack getPrimitiveLeavesItemStack();
			
	ILeavesProperties setDynamicLeavesState(IBlockState state);
	
	IBlockState getDynamicLeavesState();

	IBlockState getDynamicLeavesState(int hydro);
	
	int getFlammability();
	
	int getFireSpreadSpeed();
	
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	int getSmotherLeavesMax();
	
	/** Minimum amount of light necessary for a leaves block to be created. [default = 13] **/
	int getLightRequirement();
	
	/** A CellKit for leaves automata */
	ICellKit getCellKit();
	
	@SideOnly(Side.CLIENT)
	int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos);
	
	boolean appearanceChangesWithHydro();
	
	int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius);

	///////////////////////////////////////////
	//BLOCK PAGING SHORTCUTS
	///////////////////////////////////////////
	
	public default ILeavesProperties assign() {
		return assign(null);
	}
	
	public default ILeavesProperties assign(@Nullable String modid) {
		LeavesPaging.getNextLeavesBlock(modid, this);
        return this;
	}
	
	public default ILeavesProperties assign(@Nullable String modid, int seq) {
		LeavesPaging.getLeavesBlockForSequence(modid, seq, this);
        return this;
	}
	
	public default ILeavesProperties skip(@Nullable String modid) {
		LeavesPaging.getNextSequenceNumber(modid);
		return this;
	}
	
}