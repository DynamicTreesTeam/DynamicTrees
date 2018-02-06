package com.ferreusveritas.dynamictrees.trees;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeCactus extends DynamicTree {
	
	public TreeCactus() {
		super(new ResourceLocation(ModConstants.MODID, "cactus"));
	}
	
	public class speciesCactus extends Species {
		
		public speciesCactus(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.oakLeavesProperties);//FIXME: Obviously not oak leaves.  
		}
	
		@Override
		public float getPrimaryThickness() {
			return 4.0f;
		}

		@Override
		public float getSecondaryThickness() {
			return 4.0f;
		}
		
		@Override
		public boolean handleRot(World world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, boolean rapid) {
			return false;
		}
		
	}
	
}
