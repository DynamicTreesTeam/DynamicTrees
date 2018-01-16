package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.util.ResourceLocation;

public class TreeCactus extends DynamicTree {
	
	public TreeCactus() {
		super(new ResourceLocation(ModConstants.MODID, "cactus"), -1);
	}
	
	public class speciesCactus extends Species {
		
		public speciesCactus(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
		}
		
	}
	
}
