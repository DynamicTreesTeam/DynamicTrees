package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
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
		super(new ResourceLocation(DynamicTrees.MODID, wood.toString()));
		
		woodType = wood;
		getCommonLeaves().setTree(this);

		//Setup tree references
		switch (wood){
			case darkoak:
				setPrimitiveLog(Blocks.DARK_OAK_LOG);
				break;
			case acacia:
				setPrimitiveLog(Blocks.ACACIA_LOG);
				break;
			case jungle:
				setPrimitiveLog(Blocks.JUNGLE_LOG);
				break;
			case birch:
				setPrimitiveLog(Blocks.BIRCH_LOG);
				break;
			case spruce:
				setPrimitiveLog(Blocks.SPRUCE_LOG);
				break;
			case oak:
				setPrimitiveLog(Blocks.OAK_LOG);
				break;
		}

		//Setup common species
		getCommonSpecies().generateSeed();
	}
	
	@Override
	public ILeavesProperties getCommonLeaves() {
		return DTRegistries.leaves.get(getName().getPath());
	}

}
