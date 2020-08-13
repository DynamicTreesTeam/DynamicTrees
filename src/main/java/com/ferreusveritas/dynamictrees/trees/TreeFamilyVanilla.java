package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.util.ResourceLocation;

/**
 * Get your Cheeto fingers off! Only the DynamicTrees mod should use this and only for vanilla trees
 * 
 * @author ferreusveritas
 *
 */
public class TreeFamilyVanilla extends TreeFamily {
	
	public final DynamicTrees.VanillaWoodTypes woodType;
	
	public TreeFamilyVanilla(DynamicTrees.VanillaWoodTypes wood) {
		super(new ResourceLocation(DynamicTrees.MODID, wood.toString().replace("_","")));
		
		woodType = wood;
		getCommonLeaves().setTree(this);
		
		//Setup tree references
//		boolean isOld = wood.getMetadata() < 4;
//		setPrimitiveLog((isOld ? Blocks.LOG : Blocks.LOG2).getDefaultState().withProperty(isOld ? BlockOldLog.VARIANT : BlockNewLog.VARIANT, wood));
				
		//Setup common species
//		getCommonSpecies().generateSeed();
	}
	
	@Override
	public ILeavesProperties getCommonLeaves() {
		return DTRegistries.leaves.get(getName().getPath());
	}

}
